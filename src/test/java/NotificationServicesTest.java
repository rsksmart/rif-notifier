import mocked.MockTestData;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

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
    @Mock
    private DbManagerFacade dbManagerFacade;

    @Mock
    ApplicationContext applicationContext;

    @Autowired @InjectMocks
    private NotificationServices notificationServices;

    @Mock
    private NotificationService service;

    @Mock
    SMSService smsService;

    private MockTestData mockTestData = new MockTestData();

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

    private class SuccessService implements NotificationService   {
        @Override
        public String sendNotification(NotificationLog log) throws NotificationException {
            return "SUCCESS";
        }
    }
}
