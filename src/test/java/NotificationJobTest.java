import mocked.MockTestData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.rif.notifier.managers.DbManagerFacade;
import org.rif.notifier.managers.services.NotificationService;
import org.rif.notifier.models.entities.Notification;
import org.rif.notifier.models.entities.NotificationPreference;
import org.rif.notifier.models.entities.RawData;
import org.rif.notifier.models.entities.Subscription;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;


@RunWith(MockitoJUnitRunner.class)
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
        Set<Notification> notifications = mockTestData.mockNotifications().stream().collect(Collectors.toSet());
        notifications.forEach(n->{ assertEquals(0, n.getNotificationLogs().size()); });
        Notification notif = notifications.stream().findFirst().get();
        List<NotificationPreference> notificationPreferences = mockTestData.mockNotificationPreferences(10);
        List<NotificationPreference> defaultPreferences = new ArrayList<>();//mockTestData.mockNotificationPreferences(0);

        doReturn(notifications).when(dbManagerFacade).getUnsentNotificationsWithActiveSubscription(anyInt());
        doReturn(notificationPreferences).when(dbManagerFacade).getNotificationPreferences(any(), anyInt());
        //doReturn(defaultPreferences).when(dbManagerFacade).getNotificationPreferences(mockTestData.mockSubscription(), 0);
        doReturn(notif).when(notificationServices).sendNotification(any(), anyInt());

        doReturn(notif).when(notificationServices).saveNotification(any());
        notificationProcessorJob.run();

        //verify that logs for each preference is created for each notification (sms, email, api)
        notifications.forEach(n->{ assertEquals(3, n.getNotificationLogs().size()); });
        verify(notificationServices, times(10)).sendNotification(any(), anyInt());
        verify(notificationServices, times(10)).saveNotification(any());
    }
}

