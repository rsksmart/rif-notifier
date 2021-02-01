package org.rif.notifier.services.blockchain.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.rif.notifier.exception.SubscriptionException;
import org.rif.notifier.exception.ValidationException;
import org.rif.notifier.managers.DbManagerFacade;
import org.rif.notifier.models.SubscriptionPaymentModel;
import org.rif.notifier.models.datafetching.FetchedEvent;
import org.rif.notifier.models.entities.*;
import org.rif.notifier.models.listenable.EthereumBasedListenable;
import org.rif.notifier.models.listenable.EthereumBasedListenableTypes;
import org.rif.notifier.models.web3Extensions.RSKTypeReference;
import org.rif.notifier.services.blockchain.generic.rootstock.RskBlockchainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.time.LocalDate.now;
import static org.rif.notifier.models.entities.SubscriptionPaymentStatus.*;

/**
 * This class provides the following services
 * 1. Prepare event parameters required to listen to smart contract
 * 2. 
 */
@Service
@ConfigurationProperties(prefix="rsk.blockchain")
public class RskPaymentService {

    private static final Logger logger = LoggerFactory.getLogger(RskPaymentService.class);

    private static final String EVENT_SUBSCRIPTION_CREATED = "SubscriptionCreated";
    private static final String EVENT_REFUND = "Refund";
    private static final String EVENT_WITHDRAWAL = "Withdrawal";

    private static final HashMap<String, SubscriptionPaymentStatus> events = new HashMap()
    {{
        put(EVENT_SUBSCRIPTION_CREATED, RECEIVED);
        put(EVENT_WITHDRAWAL, WITHDRAWN);
        put(EVENT_REFUND, REFUNDED);
    }};


    private String paymentSmartContractAddress;
    private String withdrawalSmartContractAddress;
    private String refundSmartContractAddress;
    private String providerAddress;


    RskBlockchainService rskBlockChainService;
    DbManagerFacade dbManagerFacade;
    Map<SubscriptionPaymentStatus, BiConsumer<SubscriptionPaymentModel, Subscription>> payments = new HashMap<>();


    public RskPaymentService(RskBlockchainService rskBlockChainService, DbManagerFacade dbManagerFacade, @Qualifier("providerAddress")String providerAddress) {
        this.rskBlockChainService = rskBlockChainService;
        this.dbManagerFacade = dbManagerFacade;
        this.providerAddress = providerAddress;
        payments.put(RECEIVED, this::saveSubscriptionPayment);
        payments.put(REFUNDED, this::saveRefund);
        payments.put(WITHDRAWN, this::saveWithdrawal);
    }

    /**
     * Returns data required to create smart contract signature for subscription_created, refund, and withdrawal events
     * @return EthereumBasedListenable
     */
    public List<EthereumBasedListenable> getPaymentListenables()    {
        TypeReference hash = RSKTypeReference.createWithIndexed(Address.class, true);
        TypeReference provider = RSKTypeReference.createWithIndexed(Address.class, true);
        TypeReference amount = RSKTypeReference.createWithIndexed(Uint256.class, false);
        TypeReference currency = RSKTypeReference.createWithIndexed(Utf8String.class, false);
        EthereumBasedListenable subscriptionCreated = getContractListenable(EVENT_SUBSCRIPTION_CREATED, hash, provider, amount, currency);
        EthereumBasedListenable refund = getContractListenable(EVENT_REFUND, hash, provider, amount, currency);
        EthereumBasedListenable withdrawal = getContractListenable(EVENT_WITHDRAWAL, hash, provider, amount, currency);
        return Arrays.asList(subscriptionCreated, refund, withdrawal);
    }

    private EthereumBasedListenable getContractListenable(String eventName, TypeReference<?>... params)  {
       return new EthereumBasedListenable(paymentSmartContractAddress,
                EthereumBasedListenableTypes.CONTRACT_EVENT, Arrays.asList(params), eventName);
    }

    /**
     * Processes subscription created, refund and withdrawal events in the blockchain
     * @param eventTasks
     * @param start
     * @param lastBlock
     */
    public void processEventTasks(List<CompletableFuture<List<FetchedEvent>>> eventTasks, long start, BigInteger lastBlock) {
        eventTasks.forEach(listCompletableFuture -> {
            listCompletableFuture.whenComplete((fetchedEvents, throwable) -> {
                long end = System.currentTimeMillis();
                if(throwable != null) {
                    //set the database block to the original starting block for failure
                    dbManagerFacade.saveLastBlockPayment(lastBlock);
                } else {
                    logger.info(Thread.currentThread().getId() + " - Completed fetching payments, size: " + fetchedEvents.size());
                    try {
                        fetchedEvents.forEach(this::savePayment);
                    } catch(Exception e) {
                        logger.warn(e.getMessage(), e);
                    }
                }
            });
        });
    }


    /*
    * Save a given payment
    */
    private void savePayment(FetchedEvent fetchedEvent)  {
        SubscriptionPaymentModel paymentModel = SubscriptionPaymentModel.fromEventValues(fetchedEvent.getValues());
        SubscriptionPaymentStatus type = events.get(fetchedEvent.getEventName());
        //only process payment if provider address matches
        if (paymentModel.getProvider().equals(new Address(providerAddress)))    {
            //find the subscription based on the hash received as part of the payment event
            Subscription subscription = Optional.ofNullable(dbManagerFacade.getSubscriptionByHash(paymentModel.getHash())).orElseThrow(
                    ()->new SubscriptionException("Subscription not found for the given hash"));
            SubscriptionPayment subPayment = new SubscriptionPayment(paymentModel.getAmount(),
                    subscription, type);
            Optional<List<SubscriptionPayment>> subPayments = Optional.ofNullable(subscription.getSubscriptionPayments());
            if(!subPayments.map(p->p.add(subPayment)).isPresent())  {
                subscription.setSubscriptionPayments(Stream.of(subPayment).collect(Collectors.toList()));
            }
            //call the corresponding payment method - saveSubscriptionPayment or saveRefund or saveWithdrawal
            payments.get(type).accept(paymentModel, subscription);
        }
        else    {
            throw new ValidationException("Invalid provider address received as part of payment event");
        }
    }

    private void saveSubscriptionPayment(SubscriptionPaymentModel paymentModel, Subscription subscription) {

           boolean priceMatch = subscription.getPrice().equals(paymentModel.getAmount()) &&
                                subscription.getCurrency().equals(paymentModel.getCurrency());
           //activate the subscription if price and currency match the subscription;
           if(priceMatch) {
               subscription.setActiveSince(new Date());
               subscription.setExpirationDate(java.sql.Date.valueOf(now().plusDays(subscription.getSubscriptionPlan().getValidity())));
               subscription.setStatus(SubscriptionStatus.ACTIVE);
               dbManagerFacade.updateSubscription(subscription);
           }
           else {
               logger.warn("Incorrect payment data received. price or currency not the same as in subscription");
           }
    }

    private void saveRefund(SubscriptionPaymentModel paymentModel, Subscription subscription) {
            //deactivate the subscription if price and currency match the subscription;
            subscription.setStatus(SubscriptionStatus.EXPIRED);
            subscription.setExpirationDate(new Date());
            dbManagerFacade.updateSubscription(subscription);
    }

    private void saveWithdrawal(SubscriptionPaymentModel paymentModel, Subscription subscription) {
            //subscription.setStatus(SubscriptionStatus.COMPLETED);
            dbManagerFacade.updateSubscription(subscription);
    }

    public void setPaymentSmartContractAddress(String paymentSmartContractAddress) {
        this.paymentSmartContractAddress = paymentSmartContractAddress;
    }

    public void setWithdrawalSmartContractAddress(String withdrawalSmartContractAddress) {
        this.withdrawalSmartContractAddress = withdrawalSmartContractAddress;
    }

    public void setRefundSmartContractAddress(String refundSmartContractAddress) {
        this.refundSmartContractAddress = refundSmartContractAddress;
    }
}
