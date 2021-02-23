package integration;

import mocked.MockTestData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.rif.notifier.Application;
import org.rif.notifier.datafetcher.ContractEventEthereumBasedDataFetcher;
import org.rif.notifier.managers.DbManagerFacade;
import org.rif.notifier.models.datafetching.FetchedEvent;
import org.rif.notifier.models.entities.*;
import org.rif.notifier.models.listenable.EthereumBasedListenable;
import org.rif.notifier.repositories.SubscriptionRepository;
import org.rif.notifier.scheduled.PaymentProcessingJob;
import org.rif.notifier.services.blockchain.generic.rootstock.RskBlockchainService;
import org.rif.notifier.services.blockchain.payment.RskPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.web3j.abi.datatypes.Address;

import java.math.BigInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

/**
 * Integration test for PaymentProcessingJob and RskPaymentService
 * Uses partial mocking to mock receiving events from blockchain
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class, IntegrationTestData.class})
@ActiveProfiles("test")
public class PaymentServiceIntegrationTest {


    private MockTestData mockTestData = new MockTestData();

    @InjectMocks @Autowired private PaymentProcessingJob paymentProcessingJob;

    @SpyBean private RskPaymentService rskPaymentService;
    @SpyBean RskBlockchainService rskBlockchainService;
    @SpyBean ContractEventEthereumBasedDataFetcher contractEventDataFetcher;
    @SpyBean DbManagerFacade dbManagerFacade;

    @Autowired RskPaymentService paymentService;
    @Autowired PaymentProcessingJob paymentJob;

    @Autowired @Qualifier("providerAddress") Address providerAddress;
    @Autowired IntegrationTestData integrationTestData;
    @Autowired SubscriptionRepository subscriptionRepository;

    String eventName;

    @Before
    public void setup() throws Exception {

        doReturn(new BigInteger("9")).when(dbManagerFacade).getLastBlockForPayment();
        doReturn(new BigInteger("10")).when(rskBlockchainService).getLastConfirmedBlock(any(BigInteger.class));
        doReturn(null).when(dbManagerFacade).saveLastBlockPayment(any(BigInteger.class));
    }


    private Subscription preparePaymentTest(String dbLastBlock, String rskLastBlock)    throws Exception    {
        //create subscription
        integrationTestData.setup(SubscriptionStatus.PENDING, true );
        Subscription sub = integrationTestData.getSubscription();
        return preparePaymentTest(dbLastBlock, rskLastBlock, sub);
    }

    private Subscription preparePaymentTest(String dbLastBlock, String rskLastBlock, Subscription sub) throws Exception    {
        FetchedEvent event = integrationTestData.paymentEvent(eventName, providerAddress, sub);
        doReturn(mockTestData.mockFutureEvent(event).stream().findFirst().get())
                .when(rskBlockchainService).getContractEvents(any(EthereumBasedListenable.class), any(BigInteger.class), any(BigInteger.class));
        return sub;
    }

    public void deleteSubscription(Subscription sub)    {
        subscriptionRepository.delete(sub);
    }

    private void successTest(Consumer<Subscription> subscriptionConsumer)  throws Exception    {
        integrationTestData.setup(SubscriptionStatus.PENDING, true );
        Subscription sub = integrationTestData.getSubscription();
        subscriptionConsumer.accept(sub);
        eventName = "SubscriptionCreated";
        preparePaymentTest("9","10", sub);
        paymentProcessingJob.run();
        sub = dbManagerFacade.getSubscriptionByHash(integrationTestData.getSubscription().getHash());
        assertEquals(SubscriptionStatus.ACTIVE, sub.getStatus());
        assertTrue(sub.getSubscriptionPayments().size() > 0);
        assertTrue(sub.getSubscriptionPayments().stream().findFirst().get().isReceived());
        deleteSubscription(sub);
    }

    @Test
    public void canActivateOnCorrectPayment()    throws Exception {
        successTest(sub->{});
    }

    @Test
    public void canActivateOnMorePayment()    throws Exception {
        successTest(sub->sub.setPrice(new BigInteger("100")));
    }

    private void failureTest(Consumer<Subscription> subscriptionConsumer,
                             Consumer<SubscriptionPayment> paymentConsumer,
                             SubscriptionStatus expectedStatus)  throws Exception    {
        integrationTestData.setup(SubscriptionStatus.PENDING, true );
        Subscription sub = integrationTestData.getSubscription();
        subscriptionConsumer.accept(sub);
        preparePaymentTest("9","10", sub);
        paymentProcessingJob.run();
        sub = dbManagerFacade.getSubscriptionByHash(integrationTestData.getSubscription().getHash());
        if(expectedStatus != null)
            assertEquals(expectedStatus, sub.getStatus());
        assertTrue(sub.getSubscriptionPayments().size() > 0);
        paymentConsumer.accept(sub.getSubscriptionPayments().stream().findFirst().get());
        //assertTrue(sub.getSubscriptionPayments().stream().findFirst().get().isReceived());
        deleteSubscription(sub);
    }

    @Test
    public void failActivateOnLessPayment()    throws Exception {
        //subscription price is 10 but event received amount is one
        //sub.setPrice(BigInteger.ONE);
        eventName = "SubscriptionCreated";
        failureTest(sub->sub.setPrice(BigInteger.ONE), payment->payment.isReceived(), SubscriptionStatus.PENDING);
    }

    @Test
    public void failActivateOnWrongCurrency()    throws Exception {
        //sub.to wrong currency
        eventName = "SubscriptionCreated";
        Currency cur = new Currency("Test", new Address("0x100"));
        failureTest(sub->sub.setCurrency(cur), payment->assertNull(payment.getCurrency()), SubscriptionStatus.PENDING);
    }

    @Test
    public void failActivateOnRefund()    throws Exception {
        //sub.to wrong currency
        failureTest(sub->{eventName="FundsRefund";}, payment->assertTrue(payment.isRefunded()), SubscriptionStatus.EXPIRED);
    }

    @Test
    public void canWithdraw()    throws Exception {
        //sub.to wrong currency
        failureTest(sub->{eventName="FundsWithdrawn";},
                payment->assertEquals(SubscriptionPaymentStatus.WITHDRAWN, payment.getStatus()), null);
    }

}
