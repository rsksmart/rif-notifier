package org.rif.notifier.services.blockchain.payment;

import org.rif.notifier.exception.SubscriptionException;
import org.rif.notifier.managers.DbManagerFacade;
import org.rif.notifier.models.SubscriptionPaymentModel;
import org.rif.notifier.models.datafetching.FetchedEvent;
import org.rif.notifier.models.entities.Currency;
import org.rif.notifier.models.entities.*;
import org.rif.notifier.models.listenable.EthereumBasedListenable;
import org.rif.notifier.models.listenable.EthereumBasedListenableTypes;
import org.rif.notifier.models.web3Extensions.RSKTypeReference;
import org.rif.notifier.services.SubscribeServices;
import org.rif.notifier.services.blockchain.generic.rootstock.RskBlockchainService;
import org.rif.notifier.validation.CurrencyValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private static final String EVENT_REFUND = "FundsRefund";
    private static final String EVENT_WITHDRAWAL = "FundsWithdrawn";
    private static final String EVENT_FUNDS_DEPOSIT = "FundsDeposit";

    private static final HashMap<String, SubscriptionPaymentStatus> events = new HashMap(3)
    {{
        put(EVENT_SUBSCRIPTION_CREATED, RECEIVED);
        put(EVENT_WITHDRAWAL, WITHDRAWN);
        put(EVENT_REFUND, REFUNDED);
    }};


    private String notifierSmartContractAddress;
    private Address providerAddress;


    RskBlockchainService rskBlockChainService;
    DbManagerFacade dbManagerFacade;
    SubscribeServices subscribeServices;
    CurrencyValidator currencyValidator;
    Map<SubscriptionPaymentStatus, BiConsumer<SubscriptionPaymentModel, Subscription>> payments = new HashMap<>(3);


    public RskPaymentService(RskBlockchainService rskBlockChainService, DbManagerFacade dbManagerFacade,
                             SubscribeServices subscribeServices, CurrencyValidator currencyValidator,
                             @Qualifier("providerAddress") Address providerAddress) {
        this.rskBlockChainService = rskBlockChainService;
        this.dbManagerFacade = dbManagerFacade;
        this.providerAddress = providerAddress;
        this.subscribeServices = subscribeServices;
        this.currencyValidator = currencyValidator;
        payments.put(RECEIVED, this::saveSubscriptionPayment);
        payments.put(REFUNDED, this::saveRefund);
        payments.put(WITHDRAWN, this::saveWithdrawal);
    }

    /**
     * Returns data required to create smart contract signature for subscription_created, refund, and withdrawal events
     * @return EthereumBasedListenable
     */
    public List<EthereumBasedListenable> getPaymentListenables()    {
        TypeReference hash = RSKTypeReference.createWithIndexed(Bytes32.class, false);
        TypeReference provider = RSKTypeReference.createWithIndexed(Address.class, false);
        TypeReference amount = RSKTypeReference.createWithIndexed(Uint256.class, false);
        TypeReference currency = RSKTypeReference.createWithIndexed(Address.class, false);
        EthereumBasedListenable subscriptionCreated = getContractListenable(EVENT_SUBSCRIPTION_CREATED, hash, provider, currency, amount);
        EthereumBasedListenable refund = getContractListenable(EVENT_REFUND, hash, amount, currency);
        EthereumBasedListenable withdrawal = getContractListenable(EVENT_WITHDRAWAL, hash, amount, currency);
        return Arrays.asList(subscriptionCreated, refund, withdrawal);
    }

    private EthereumBasedListenable getContractListenable(String eventName, TypeReference<?>... params)  {
       return new EthereumBasedListenable(notifierSmartContractAddress,
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
        //get the currency instance associated with the given currency address from db
        Optional<Currency> currency = currencyValidator.validate(paymentModel.getCurrencyAddress());
        paymentModel.setCurrency(currency.orElse(null));
        SubscriptionPaymentStatus type = events.get(fetchedEvent.getEventName());
        //only process payment if provider address matches
        //if (paymentModel.getProvider() == null || providerAddress.equals(paymentModel.getProvider()))    {
        Subscription subscription = dbManagerFacade.getSubscriptionByHash(paymentModel.getHash());
        if(subscription != null)    {
        //find the subscription based on the hash received as part of the payment event
            SubscriptionPayment subPayment = new SubscriptionPayment(paymentModel.getAmount(),
                    subscription, currency.orElse(null), type);
            //set payment reference to the invalid currency address received.
            if(!currency.isPresent()) {
               subPayment.setPaymentReference(paymentModel.getCurrencyAddress().toString());
            }
            Optional<List<SubscriptionPayment>> subPayments = Optional.ofNullable(subscription.getSubscriptionPayments());
            if(!subPayments.map(p->p.add(subPayment)).isPresent())  {
                subscription.setSubscriptionPayments(Stream.of(subPayment).collect(Collectors.toList()));
            }
            //call the corresponding payment method - saveSubscriptionPayment or saveRefund or saveWithdrawal
            payments.get(type).accept(paymentModel, subscription);
        }
    }

    private void saveSubscriptionPayment(SubscriptionPaymentModel paymentModel, Subscription subscription) {

        //accept if the payment amount is greater than or equals to subscription price
        boolean priceMatch = paymentModel.getAmount()!= null &&
                                subscription.getPrice().compareTo(paymentModel.getAmount()) <= 0 &&
                                subscription.getCurrency().equals(paymentModel.getCurrency());
        //save the subscription payment when correct or incorrect without activating
        Subscription sub = dbManagerFacade.updateSubscription(subscription);
        //try activate the subscription if price and currency match the subscription
        // and when there is no active or pending previous subscription
        if(priceMatch && sub.canActivate()) {
            subscribeServices.activateSubscription(sub);
        }
        else {
            logger.warn("Incorrect payment data received. price or currency not the same as in subscription, " +
                    "or the previous subscription is still active or pending.");
        }
    }

    private void saveRefund(SubscriptionPaymentModel paymentModel, Subscription subscription) {
            //deactivate the subscription if price and currency match the subscription;
            subscription.setStatus(SubscriptionStatus.EXPIRED);
            subscription.setExpirationDate(new Date());
            dbManagerFacade.updateSubscription(subscription);
    }

    private void saveWithdrawal(SubscriptionPaymentModel paymentModel, Subscription subscription) {
            dbManagerFacade.updateSubscription(subscription);
    }

    public void setNotifierSmartContractAddress(String notifierSmartContractAddress) {
        this.notifierSmartContractAddress = notifierSmartContractAddress;
    }
}
