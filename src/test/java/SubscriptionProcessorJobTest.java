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

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    private void prepareTest(Subscription prev, Subscription renewed)  throws Exception {
        prev.setExpirationDate(tomorrow);
        renewed.setStatus(SubscriptionStatus.PENDING);
        renewed.setNotificationBalance(100);
        //renewed.setExpirationDate(tomorrow);
        doCallRealMethod().when(subscribeServices).renewWhenZeroBalance(any(Subscription.class));
        doCallRealMethod().when(subscribeServices).activateSubscription(any(Subscription.class));
        when(dbManagerFacade.getSubscriptionByPreviousSubscription(any(Subscription.class))).thenReturn(renewed);
        when(subscribeServices.getExpiredSubscriptionsCount()).thenReturn(1);
        when(subscribeServices.getZeroBalanceSubscriptions()).thenReturn(Arrays.asList(prev));
        subscriptionProcessorJob.run();
    }

    @Test
    public void canActivatePreviousubscription()    throws Exception {
        Subscription prev = mockTestData.mockSubscription();
        Subscription renewed = mockTestData.mockSubscription();
        prepareTest(prev, renewed);
        prev.setNotificationBalance(0);
        subscriptionProcessorJob.run();
        assertNotNull(renewed.getExpirationDate());
        assertTrue(renewed.getExpirationDate().getTime() > System.currentTimeMillis());
        //current subscription should be active as balance is finished.
        assertEquals(SubscriptionStatus.ACTIVE, renewed.getStatus());
        assertEquals(SubscriptionStatus.COMPLETED, prev.getStatus());
    }

    @Test
    public void failActivatePreviousubscription()    throws Exception {
        Subscription prev = mockTestData.mockSubscription();
        Subscription renewed = mockTestData.mockSubscription();
        prepareTest(prev, renewed);
        prev.setNotificationBalance(1);
        subscriptionProcessorJob.run();
        assertNull(renewed.getExpirationDate());
        //prev subscription should still be active as balance not finished.
        assertEquals(SubscriptionStatus.ACTIVE, prev.getStatus());
        assertEquals(SubscriptionStatus.PENDING, renewed.getStatus());
    }
}
