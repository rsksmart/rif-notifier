import mocked.MockTestData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.rif.notifier.managers.DbManagerFacade;
import org.rif.notifier.models.entities.Subscription;
import org.rif.notifier.models.entities.SubscriptionStatus;
import org.rif.notifier.scheduled.SubscriptionProcessorJob;
import org.rif.notifier.services.SubscribeServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.Silent.class)
public class SubscriptionProcessorJobTest {
    @Mock
    private DbManagerFacade dbManagerFacade;
    @Mock
    private SubscribeServices subscribeServices;
    @Autowired
    @InjectMocks
    private SubscriptionProcessorJob subscriptionProcessorJob;

    private MockTestData mockTestData = new MockTestData();

    @Before
    public void setup()    {
        ReflectionTestUtils.setField(subscribeServices, "dbManagerFacade", dbManagerFacade);
    }

    private static Date tomorrow = new Date(System.currentTimeMillis() + 86400000);

    private void prepareTest(Subscription sub)  throws Exception {
        sub.setStatus(SubscriptionStatus.PENDING);
        doCallRealMethod().when(subscribeServices).activateSubscription(any(Subscription.class));
        when(dbManagerFacade.updateSubscription(any(Subscription.class))).thenReturn(sub);
        when(subscribeServices.getExpiredSubscriptionsCount()).thenReturn(1);
        when(subscribeServices.getPendingSubscriptions()).thenReturn(Arrays.asList(sub));
    }

    @Test
    public void canActivatePendingPaidSubscription()    throws Exception {
        Subscription sub = mockTestData.mockPaidSubscription();
        prepareTest(sub);
        subscriptionProcessorJob.run();
        assertNotNull(sub.getExpirationDate());
        assertTrue(sub.getExpirationDate().getTime() > System.currentTimeMillis());
        //subscription should be active as payment is made.
        assertEquals(SubscriptionStatus.ACTIVE, sub.getStatus());
    }

    @Test
    public void failActivateUnpaid()    throws Exception {
        Subscription sub= mockTestData.mockSubscription();
        prepareTest(sub);
        subscriptionProcessorJob.run();
        assertNull(sub.getExpirationDate());
        //subscription should still be pending as payment not made.
        assertEquals(SubscriptionStatus.PENDING, sub.getStatus());
    }

    @Test
    public void failActivateRefunded()    throws Exception {
        Subscription sub= mockTestData.mockPaidSubscription();
        sub.getSubscriptionPayments().add(mockTestData.mockRefund(sub,BigInteger.TEN));
        prepareTest(sub);
        subscriptionProcessorJob.run();
        assertNull(sub.getExpirationDate());
        //subscription should still be pending as payment was refunded.
        assertEquals(SubscriptionStatus.PENDING, sub.getStatus());
    }

    /**
     * Verify activation of subscription when payment amount minus refund amount is >= price subscription
     * @throws Exception
     */
    @Test
    public void canActivateHigherPaid()    throws Exception {
        Subscription sub= mockTestData.mockPaidSubscription();
        sub.getSubscriptionPayments().add(mockTestData.mockRefund(sub,BigInteger.TEN));
        sub.getSubscriptionPayments().add(mockTestData.mockPayment(sub,BigInteger.TEN));
        prepareTest(sub);
        subscriptionProcessorJob.run();
        assertNotNull(sub.getExpirationDate());
        //subscription should be active as difference of subscription payment and refund exceeds or equals to the subscription price
        assertEquals(SubscriptionStatus.ACTIVE, sub.getStatus());
    }

    /**
     * only pending subscriptions can be activated
     * @throws Exception
     */
    @Test
    public void failActivateNonPending()    throws Exception {
        Subscription sub = mockTestData.mockPaidSubscription();
        prepareTest(sub);
        Stream.of(SubscriptionStatus.COMPLETED, SubscriptionStatus.ACTIVE, SubscriptionStatus.EXPIRED).forEach(status->{
            sub.setStatus(status);
            subscriptionProcessorJob.run();
            //subscription should still be expired as only pending subscriptions can be activated
            assertEquals(status, sub.getStatus());
        });
    }
}
