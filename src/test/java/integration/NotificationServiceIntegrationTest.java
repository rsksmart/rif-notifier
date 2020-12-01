package integration;

import mocked.MockTestData;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.rif.notifier.Application;
import org.rif.notifier.constants.SubscriptionConstants;
import org.rif.notifier.constants.TopicTypes;
import org.rif.notifier.managers.DbManagerFacade;
import org.rif.notifier.managers.datamanagers.NotificationPreferenceManager;
import org.rif.notifier.managers.datamanagers.TopicManager;
import org.rif.notifier.models.entities.*;
import org.rif.notifier.repositories.NotificationPreferenceRepository;
import org.rif.notifier.repositories.NotificationRepository;
import org.rif.notifier.repositories.SubscriptionTypeRepository;
import org.rif.notifier.services.NotificationServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class, IntegrationTestData.class})
@ActiveProfiles("test")
public class NotificationServiceIntegrationTest {
    @Value("${notificationservice.maxretries}") private int maxRetries;

    @Autowired DbManagerFacade dbManagerFacade;

    @Autowired NotificationPreferenceManager notificationPreferenceManager;

    @Autowired NotificationRepository notificationRepository;
    @Autowired NotificationServices notificationServices;
    @Autowired IntegrationTestData integrationTestData;

    private MockTestData mockTestData = new MockTestData();



    @Before
    public void setUp() {
        integrationTestData.setup();
    }

    @Test
    public void canSendAPINotification()    {
        Notification notif = integrationTestData.getNotification();
        notificationServices.sendNotification(notif, maxRetries);
        notif = notificationServices.saveNotification(notif);
        assertTrue(notif.isSent());
    }

    @Test
    public void canSaveNotificationPreferences()  {
        NotificationPreference preference = notificationPreferenceManager.getNotificationPreference(integrationTestData.getActiveSubscription(), integrationTestData.getTopicId(), NotificationServiceType.API);
        String destination = preference != null ? preference.getDestination() : "";
        if (preference == null) {
            preference = integrationTestData.newNotificationPreference();
        }
        //change password to millis and check if it's saved
        String millis = String.valueOf(System.currentTimeMillis());
        preference.getDestinationParams().setPassword(millis);
        NotificationPreference saved = notificationPreferenceManager.saveNotificationPreference(preference);
        assertEquals(millis, saved.getDestinationParams().getPassword());
        //set the password back to original
        saved.getDestinationParams().setPassword(integrationTestData.getPassword());
        saved = notificationPreferenceManager.saveNotificationPreference(saved);
        assertEquals(integrationTestData.getPassword(), saved.getDestinationParams().getPassword());
    }

    @Test
    @Ignore
    public void canSaveNotificationLog()  {
        Notification notif = notificationRepository.findAll().get(0);
        if (notif == null) {
            return;
        }
        List<NotificationPreference> prefs = notificationPreferenceManager.getNotificationPreferences(notif.getSubscription(), notif.getIdTopic());
        if (!prefs.isEmpty() && notif.getNotificationLogs().isEmpty()) {
            List<NotificationLog> l = new ArrayList<>();
            l.add(mockTestData.newMockLog(notif, prefs.get(0)));
            notif.setNotificationLogs(l);
        }
        notif.getNotificationLogs().forEach(l->
                l.setRetryCount(l.getRetryCount() >= 50 ? 45 : 50));
        Notification savedNotif = notificationRepository.save(notif);
        savedNotif.getNotificationLogs().stream().forEach(log->{
            long diff = System.currentTimeMillis() - log.getLastUpdated().getTime();
            //assert if updated in the last one second
            assertTrue(diff < 1000);
        });
    }

    @Test
    public void canGetNotifications()   {
        List<Notification> nots = dbManagerFacade.getUnsentNotifications(maxRetries);
        assertTrue(nots.size() > 0);
    }

    @Test
    @Ignore
    public void canProcessUnsentNotifications()    {
        Set<Notification> unsentNotifications = dbManagerFacade.getUnsentNotificationsWithActiveSubscription(100);
        unsentNotifications.forEach(notification->{
            List<NotificationPreference> notificationPreferences = dbManagerFacade.getNotificationPreferences(notification.getSubscription(), notification.getIdTopic());
            List<NotificationLog> logs = notification.getNotificationLogs();
            List<NotificationPreference> noprefs = notificationPreferences.stream().filter(pref-> logs.stream().noneMatch(log-> log.getNotificationPreference().equals(pref))).collect(Collectors.toList());
            noprefs.forEach(pref -> {
                NotificationLog nl = new NotificationLog();
                nl.setNotification(notification);
                nl.setNotificationPreference(pref);
                notification.getNotificationLogs().add(nl);
            });
        });
    }
}
