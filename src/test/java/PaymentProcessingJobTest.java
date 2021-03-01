import mocked.MockTestData;
import org.apache.commons.lang3.mutable.MutableInt;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.verification.VerificationMode;
import org.rif.notifier.managers.DbManagerFacade;
import org.rif.notifier.models.datafetching.FetchedEvent;
import org.rif.notifier.models.entities.Currency;
import org.rif.notifier.models.entities.Subscription;
import org.rif.notifier.models.entities.SubscriptionStatus;
import org.rif.notifier.models.listenable.EthereumBasedListenable;
import org.rif.notifier.scheduled.PaymentProcessingJob;
import org.rif.notifier.services.SubscribeServices;
import org.rif.notifier.services.blockchain.generic.rootstock.RskBlockchainService;
import org.rif.notifier.services.blockchain.payment.RskPaymentService;
import org.rif.notifier.validation.CurrencyValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import org.web3j.abi.datatypes.Address;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PaymentProcessingJobTest {
    @Mock private DbManagerFacade dbManagerFacade;
    @Mock private RskPaymentService rskPaymentService;
    @Mock private RskBlockchainService rskBlockchainService;
    @Mock private SubscribeServices subscribeServices;
    @Mock private CurrencyValidator currencyValidator;

    @InjectMocks @Autowired private PaymentProcessingJob paymentJob;
    @InjectMocks @Autowired private RskPaymentService rskPaymentServiceInject;

    private MockTestData mockTestData = new MockTestData();

    @Before
    public void setup() {
       ReflectionTestUtils.setField(subscribeServices, "dbManagerFacade", dbManagerFacade);
    }

    public void prepareDataFetcherTest(String dbLastBlock, String rskLastBlock) throws Exception    {
        doReturn(new BigInteger(dbLastBlock)).when(dbManagerFacade).getLastBlockForPayment();
        doReturn(new BigInteger(rskLastBlock)).when(rskBlockchainService).getLastConfirmedBlock(any(BigInteger.class));
    }

    private void paymentTest(String eventName, int expected, Subscription sub)   throws Exception {
        paymentTest(eventName, expected, sub, "0x0", atLeastOnce());
    }

    private void paymentTest(String eventName, int expected, Subscription sub, String providerAddress, VerificationMode validHashVerification)   throws Exception {
        paymentTest(eventName, "100000", "RIF", expected, sub, providerAddress, validHashVerification) ;
    }

    private void paymentTest(String eventName, String price, String cur, int expected, Subscription sub, String providerAddress, VerificationMode validHashVerification)   throws Exception {
        FetchedEvent event = mockTestData.mockPaymentEvent(eventName);
        ReflectionTestUtils.setField(rskPaymentServiceInject, "providerAddress", new Address(providerAddress));
        if(sub != null) {
            sub.setStatus(SubscriptionStatus.PENDING);
            sub.setPrice(new BigInteger(price));
            Currency c = new Currency(cur, new Address("0x0"));
            sub.setCurrency(c);
        }
        when(dbManagerFacade.getSubscriptionByHash(anyString())).thenReturn(sub);
        rskPaymentServiceInject.processEventTasks(mockTestData.mockFutureEvent(event), 100, BigInteger.ONE);
        verify(dbManagerFacade, validHashVerification).getSubscriptionByHash(anyString());
        if(sub != null && sub.getSubscriptionPayments() != null)
            assertEquals(expected, sub.getSubscriptionPayments().size());
    }

    @Test
    public void canRunPaymentProcessingJob() throws Exception   {
        prepareDataFetcherTest("9", "10");
        FetchedEvent event = mockTestData.mockFetchedEvent();
        doReturn(Arrays.asList(mockTestData.mockEthereumBasedListeneable())).when(rskPaymentService).getPaymentListenables();
        when(rskBlockchainService.getContractEvents(any(EthereumBasedListenable.class), any(BigInteger.class), any(BigInteger.class)))
                                .thenReturn(mockTestData.mockFutureEvent(event).get(0));
        paymentJob.run();
        verify(rskBlockchainService, times(1)).getContractEvents(any(), any(BigInteger.class), any(BigInteger.class));
        verify(dbManagerFacade, times(1)).saveLastBlockPayment(any(BigInteger.class));
        verify(rskPaymentService, times(1)).processEventTasks(any(), any(Long.class), any(BigInteger.class));
    }

    @Test(expected = Exception.class)
    public void errorRunPaymentProcessingJob() throws Exception {
        prepareDataFetcherTest("9", "10");
        FetchedEvent event = mockTestData.mockFetchedEvent();
        doReturn(Arrays.asList(mockTestData.mockEthereumBasedListeneable())).when(rskPaymentService).getPaymentListenables();
        doThrow(RuntimeException.class).when(rskBlockchainService).getContractEvents(any(EthereumBasedListenable.class), any(BigInteger.class), any(BigInteger.class));
        paymentJob.run();
    }

    @Test
    public void errorProcessInvalidProviderAddress() throws Exception {
        Subscription sub = mockTestData.mockSubscription();
        paymentTest("SubscriptionCreated", 1, sub, "0x1", never());
        assertEquals(SubscriptionStatus.PENDING, sub.getStatus());
    }

    @Test
    public void errorHashNotFound() throws Exception {
        paymentTest("SubscriptionCreated", 1, null, "0x0", atLeastOnce());
        verify(dbManagerFacade, never()).updateSubscription(any(Subscription.class));
    }

    @Test
    public void errorCurrencyNotMatching() throws Exception {
        Subscription sub = mockTestData.mockSubscription();
        paymentTest("SubscriptionCreated", "100000", "RIF2", 1, sub, "0x0", atLeastOnce());
        assertNotEquals(SubscriptionStatus.ACTIVE, sub.getStatus());
    }

    @Test
    public void errorAmountNotMatching() throws Exception {
        Subscription sub = mockTestData.mockSubscription();
        paymentTest("SubscriptionCreated", "1000", "RIF", 1, sub, "0x0", atLeastOnce());
        assertNotEquals(SubscriptionStatus.ACTIVE, sub.getStatus());
    }

    @Test
    public void canActivateSubscription() throws Exception   {
        Subscription sub = mockTestData.mockSubscription();
        doCallRealMethod().when(subscribeServices).activateSubscription(any(Subscription.class));
        when(currencyValidator.validate(any(Address.class))).thenReturn(mockTestData.mockCurrency());
        when(dbManagerFacade.updateSubscription(any(Subscription.class))).thenReturn(sub);
        paymentTest("SubscriptionCreated", 1, sub);
        assertEquals(SubscriptionStatus.ACTIVE, sub.getStatus());
    }

    @Test
    public void canRefundSubscription() throws Exception   {
        Subscription sub = mockTestData.mockSubscription();
        paymentTest("FundsRefund", 1, sub);
        assertEquals(SubscriptionStatus.EXPIRED, sub.getStatus());
    }

    @Test
    public void canWithdrawSubscriptionPayment() throws Exception   {
        Subscription sub = mockTestData.mockSubscription();
        //payment test sets the status to expired
        paymentTest("FundsWithdrawn",1, sub);
        //withdrawal will not change the status
        assertEquals(SubscriptionStatus.PENDING, sub.getStatus());
    }

    @Test
    public void canHaveMultiplePayments()   throws Exception    {
        Subscription sub = mockTestData.mockSubscription();
        paymentTest("FundsWithdrawn",1, sub);
        paymentTest("FundsRefund",2, sub);
        paymentTest("SubscriptionCreated",3, sub);
    }

    /*
    * Verify activation not successful when
     */
    @Test
    public void failActivateNewWhenCurrentlyActive() throws Exception   {
        Subscription sub = mockTestData.mockPaidSubscription();
        Subscription prev = mockTestData.mockPaidSubscription();
        when(dbManagerFacade.updateSubscription(any(Subscription.class))).thenReturn(sub);
        prev.setStatus(SubscriptionStatus.ACTIVE);
        sub.setPreviousSubscription(prev);
        paymentTest("SubscriptionCreated", 2, sub);
        //the subscription should not be active as there is a previously active subscription
        assertEquals(SubscriptionStatus.PENDING, sub.getStatus());
    }

    /*
     * Verify activation when previous is not active
     */
    @Test
    public void canActivateNewWhenPreviousNotActive() throws Exception   {
        Subscription sub = mockTestData.mockPaidSubscription();
        Subscription prev = mockTestData.mockPaidSubscription();
        doCallRealMethod().when(subscribeServices).activateSubscription(any(Subscription.class));
        when(currencyValidator.validate(any(Address.class))).thenReturn(mockTestData.mockCurrency());
        when(dbManagerFacade.updateSubscription(any(Subscription.class))).thenReturn(sub);
        MutableInt expected = new MutableInt(1);
        //if the previous subscription sttus is not pending or active, then activate the new subscription
        Stream.of(SubscriptionStatus.EXPIRED, SubscriptionStatus.COMPLETED).forEach(status->{
            prev.setStatus(status);
            sub.setPreviousSubscription(prev);
            try {
                paymentTest("SubscriptionCreated", expected.incrementAndGet(), sub);
            } catch(Exception e)    {
                throw new RuntimeException(e);
            }
            //the subscription should be active as there is no previously active  or pending subscription
            assertEquals(SubscriptionStatus.ACTIVE, sub.getStatus());
            sub.setStatus(SubscriptionStatus.PENDING);
        });

    }

    /*
     * Verify activation when previous is not active
     */
    @Test
    public void errorActivateNewWhenPreviousPending() throws Exception   {
        Subscription sub = mockTestData.mockPaidSubscription();
        Subscription prev = mockTestData.mockPaidSubscription();
        MutableInt expected = new MutableInt(1);
        //if the previous subscription sttus is still pending, then don't activate the new subscription
        prev.setStatus(SubscriptionStatus.PENDING);
        sub.setPreviousSubscription(prev);
        paymentTest("SubscriptionCreated", expected.incrementAndGet(), sub);
        //the subscription should not be active as there is a previously active subscription
        assertEquals(SubscriptionStatus.PENDING, sub.getStatus());

    }
}
