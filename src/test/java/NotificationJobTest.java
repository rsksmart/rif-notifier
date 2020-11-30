import mocked.MockTestData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.rif.notifier.exception.NotificationException;
import org.rif.notifier.managers.DbManagerFacade;
import org.rif.notifier.managers.services.NotificationService;
import org.rif.notifier.models.entities.*;
import org.rif.notifier.scheduled.NotificationProcessorJob;
import org.rif.notifier.services.NotificationServices;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;


@RunWith(MockitoJUnitRunner.Silent.class)
public class NotificationJobTest {
    @Mock
    private DbManagerFacade dbManagerFacade;

    @Mock
    private NotificationServices notificationServices;

    @Mock
    private NotificationService service;

    @Autowired @InjectMocks
    private NotificationProcessorJob notificationProcessorJob;

    private MockTestData mockTestData = new MockTestData();


    @Test
    public void canProcessNotifications()  throws Exception {
        Set<Notification> notifications = mockTestData.mockNotifications(10).stream().collect(Collectors.toSet());
        notifications.forEach(n->{ assertEquals(0, n.getNotificationLogs().size()); });
        List<NotificationPreference> notificationPreferences = mockTestData.mockNotificationPreferences(10);
        notificationPreferences = notificationPreferences.stream().filter(p->p.getNotificationService() == NotificationServiceType.SMS ||
                p.getNotificationService() == NotificationServiceType.EMAIL).collect(Collectors.toList());
        assertEquals(2, notificationPreferences.size());
        List<NotificationPreference> defaultPreferences = mockTestData.mockNotificationPreferences(0);

        doReturn(notifications).when(dbManagerFacade).getUnsentNotificationsWithActiveSubscription(anyInt());
        doReturn(notificationPreferences).when(dbManagerFacade).getNotificationPreferences(any(), eq(10));
        doReturn(defaultPreferences).when(dbManagerFacade).getNotificationPreferences(any(), eq(0));
        doAnswer(i->i.getArguments()[0]).when(notificationServices).sendNotification(any(), anyInt());
        doAnswer(i->i.getArguments()[0]).when(notificationServices).saveNotification(any());
        notificationProcessorJob.run();

        //verify that logs for each preference is created for each notification (sms, email, api)
        //verify the merged preferences contain the first 2 for topic id 10 and the last one for default preference
        notifications.forEach(n->{
            assertEquals(3, n.getNotificationLogs().size());
            assertEquals(10, n.getNotificationLogs().get(0).getNotificationPreference().getIdTopic());
            assertEquals(10, n.getNotificationLogs().get(1).getNotificationPreference().getIdTopic());
            assertEquals(0, n.getNotificationLogs().get(2).getNotificationPreference().getIdTopic());
        });

        verify(notificationServices, times(10)).sendNotification(any(), anyInt());
        verify(notificationServices, times(10)).saveNotification(any());
    }

    @Test
    public void errorProcessingNotifications() throws Exception {
        Set<Notification> notifications = mockTestData.mockNotifications().stream().collect(Collectors.toSet());
        doReturn(notifications).when(dbManagerFacade).getUnsentNotificationsWithActiveSubscription(anyInt());
        doThrow(RuntimeException.class).when(notificationServices).sendNotification(any(), anyInt());
        List<NotificationPreference> notificationPreferences = mockTestData.mockNotificationPreferences(10);
        doReturn(notifications).when(dbManagerFacade).getUnsentNotificationsWithActiveSubscription(anyInt());
        doReturn(notificationPreferences).when(dbManagerFacade).getNotificationPreferences(any(), eq(10));
        doReturn(notificationPreferences).when(dbManagerFacade).getNotificationPreferences(any(), eq(0));
        notificationProcessorJob.run();
        notifications.forEach(n->assertTrue(!n.isSent()));
    }
}

