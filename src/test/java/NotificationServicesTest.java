import mocked.MockTestData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.rif.notifier.exception.NotificationException;
import org.rif.notifier.managers.DbManagerFacade;
import org.rif.notifier.managers.services.NotificationService;
import org.rif.notifier.managers.services.impl.APIService;
import org.rif.notifier.managers.services.impl.EmailService;
import org.rif.notifier.managers.services.impl.SMSService;
import org.rif.notifier.models.entities.*;
import org.rif.notifier.scheduled.NotificationProcessorJob;
import org.rif.notifier.services.NotificationServices;
import org.rif.notifier.services.SubscribeServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import javax.validation.constraints.Email;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class NotificationServicesTest {
    @Mock private DbManagerFacade dbManagerFacade;
    @Mock ApplicationContext applicationContext;
    @Mock SubscribeServices subscribeServices;

    private static Date tomorrow = new Date(System.currentTimeMillis() + 86400000);

    @Autowired @InjectMocks
    private NotificationServices notificationServices;

    @Mock
    private NotificationService service;

    @Mock
    SMSService smsService;

    private MockTestData mockTestData = new MockTestData();

    @Before
    public void setup() {
        ReflectionTestUtils.setField(subscribeServices, "dbManagerFacade", dbManagerFacade);
    }

    private Notification getNotification()  throws Exception  {
        Set<Notification> notifications = mockTestData.mockNotifications().stream().collect(Collectors.toSet());
        Notification notif = notifications.stream().findFirst().get();
        List<NotificationPreference> notificationPreferences = mockTestData.mockNotificationPreferences(10);
        List<NotificationLog> logs = new ArrayList<>();
        notificationPreferences.forEach(p->{logs.add(mockTestData.newMockLog(notif, p));});
        notif.setNotificationLogs(logs);
        return notif;
    }


    @Test
    public void canSendNotification()  throws Exception {
        Notification notif = getNotification();
        doReturn(new SuccessService()).when(applicationContext).getBean(anyString());
        IntStream.range(0,10).forEach(i-> notificationServices.sendNotification(notif,10));
        notif.getNotificationLogs().forEach(log->assertEquals(1, log.getRetryCount()));
    }

    @Test
    public void testSuccessNotification()  throws Exception {
        Notification notif = getNotification();
        doReturn(new SuccessService()).when(applicationContext).getBean(anyString());
        IntStream.range(0,10).forEach(i-> notificationServices.sendNotification(notif,10));
        //for success notiication the retry count should remain as 1
        notif.getNotificationLogs().forEach(log->assertEquals(1, log.getRetryCount()));
        doReturn(null).when(dbManagerFacade).saveNotification(any());
        notificationServices.saveNotification(notif);
        assertTrue(notif.isSent());
    }

    @Test
    public void testFailureNotificationForServices()  throws Exception {
        Notification notif = getNotification();
        SMSService sms = new SMSService(null);
        EmailService email = new EmailService(null);
        APIService api = new APIService();
        Arrays.asList(new NotificationService[]{sms, email, api}).forEach(notificationService -> {
            doReturn(notificationService).when(applicationContext).getBean(anyString());
            IntStream.range(0, 10).forEach(i -> notificationServices.sendNotification(notif, 10));
            notif.getNotificationLogs().forEach(log -> assertEquals(10, log.getRetryCount()));
        });
        assertFalse(notif.isSent());
    }

    @Test
    public void testMaxRetries()  throws Exception {
        Notification notif = getNotification();
        APIService api = new APIService();
        doReturn(api).when(applicationContext).getBean(anyString());
        //even if more than ten attempts the retry count should stay at 10, since that's the limit
        IntStream.range(0, 15).forEach(i -> notificationServices.sendNotification(notif, 10));
        notif.getNotificationLogs().forEach(log -> assertEquals(10, log.getRetryCount()));
    }

    @Test
    public void canGetNotificationsForSubscription()    throws IOException  {
        Subscription sub = mockTestData.mockSubscription();
        Set<Integer> topics = new HashSet();
        List<Notification> notifs = mockTestData.mockNotifications();
        doReturn(notifs).when(dbManagerFacade).getNotificationsBySubscription(sub, 0, 0, topics);
        List<Notification> notifResult = notificationServices.getNotificationsForSubscription(sub, 0, 0, topics);
        assertTrue(notifResult.size() == 10);
    }

    @Test
    public void errorGetNotificationsForSubscription()    throws IOException  {
        Subscription sub = mockTestData.mockSubscription();
        Set<Integer> topics = new HashSet();
        List<Notification> notifs = mockTestData.mockNotifications();
        doReturn(new ArrayList()).when(dbManagerFacade).getNotificationsBySubscription(sub, 0, 0, topics);
        List<Notification> notifResult = notificationServices.getNotificationsForSubscription(sub, 0, 0, topics);
        assertTrue(notifResult.size() == 0);
    }

    private Notification prepareTest(Subscription renewed)  throws Exception    {
        Notification notif = getNotification();
        Subscription prev = mockTestData.mockPaidSubscription();
        prev.setNotificationBalance(0);
        prev.setStatus(SubscriptionStatus.ACTIVE);
        prev.setExpirationDate(tomorrow);
        renewed.setNotificationBalance(100);
        renewed.setStatus(SubscriptionStatus.PENDING);
        notif.setSubscription(prev);
        doCallRealMethod().when(subscribeServices).renewWhenZeroBalance(any(Subscription.class));
        doCallRealMethod().when(subscribeServices).activateSubscription(any(Subscription.class));
        when(dbManagerFacade.getSubscriptionByPreviousSubscription(any(Subscription.class))).thenReturn(renewed);
        doReturn(new SuccessService()).when(applicationContext).getBean(anyString());
        return notif;
    }

    @Test
    public void canRenewZeroBalanceSubscription()  throws Exception {
        Subscription renewed = mockTestData.mockPaidSubscription();
        Notification notif = prepareTest(renewed);
        Subscription prev = notif.getSubscription();
        notificationServices.sendNotification(notif,10);
        assertNotNull(renewed.getExpirationDate());
        assertTrue(renewed.getExpirationDate().getTime() > System.currentTimeMillis());
        //current subscription should be completed as balance is finished.
        assertEquals(SubscriptionStatus.ACTIVE, renewed.getStatus());
        assertEquals(SubscriptionStatus.COMPLETED, prev.getStatus());
    }

    /*
    * This test will fail to renew the new subscription as there is still unsent notifications in old subscription
     */
    @Test
    public void failRenewZeroBalanceSubscriptionWithUnsentNotifications()  throws Exception {
        Subscription renewed = mockTestData.mockPaidSubscription();
        Notification notif = prepareTest(renewed);
        Subscription prev = notif.getSubscription();
        //return unsent notifications in previous subscription
        when(dbManagerFacade.getUnsentNotificationsCount(anyInt(), anyInt())).thenReturn(1);
        notificationServices.sendNotification(notif,10);
        assertNull(renewed.getExpirationDate());
        //current subscription should be active as there are unsent notifications even though balance is zero.
        assertEquals(SubscriptionStatus.PENDING, renewed.getStatus());
        assertEquals(SubscriptionStatus.ACTIVE, prev.getStatus());
    }

    /*
     * This method tries to verify that the activation will not happen when there are unsent notifications
     * from previous subscription
     */
    @Test
    public void failRenewUnsentNotifications()  throws Exception {
        Subscription renewed = mockTestData.mockPaidSubscription();
        Notification notif = prepareTest(renewed);
        //since the retry count is 49 it should be considered unsent
        Subscription prev = notif.getSubscription();
        //return unsent notifications in previous subscription
        doReturn(new FailureService()).when(applicationContext).getBean(anyString());
        notificationServices.sendNotification(notif,10);
        assertNull(renewed.getExpirationDate());
        //current subscription should be active as there are unsent notifications even though balance is zero.
        assertEquals(SubscriptionStatus.PENDING, renewed.getStatus());
        assertEquals(SubscriptionStatus.ACTIVE, prev.getStatus());
    }

    private class SuccessService implements NotificationService   {
        @Override
        public String sendNotification(NotificationLog log) throws NotificationException {
            return "SUCCESS";
        }
    }

    private class FailureService implements NotificationService   {
        @Override
        public String sendNotification(NotificationLog log) throws NotificationException {
            throw new NotificationException(new RuntimeException("failure sending"));
        }
    }
}
