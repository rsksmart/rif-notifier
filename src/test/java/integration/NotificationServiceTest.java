package integration;

import mocked.MockTestData;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.rif.notifier.Application;
import org.rif.notifier.managers.DbManagerFacade;
import org.rif.notifier.managers.datamanagers.NotificationPreferenceManager;
import org.rif.notifier.models.entities.*;
import org.rif.notifier.repositories.NotificationPreferenceRepository;
import org.rif.notifier.repositories.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@TestPropertySource(locations = "classpath:application.properties")
public class NotificationServiceTest {

    @Autowired
    DbManagerFacade dbManagerFacade;

    @Autowired
    NotificationPreferenceManager notificationPreferenceManager;

    @Autowired
    NotificationRepository notificationRepository;

    private MockTestData mockTestData = new MockTestData();

    private Subscription getActiveSubscription()    {
        List<Subscription> activeSubs = dbManagerFacade.getAllActiveSubscriptions();
        if (!activeSubs.isEmpty()) {
            Subscription sub = activeSubs.get(0);
            return sub;
        }
        return null;
    }

    @Test
    public void canSaveNotificationPreferences()  {
        Subscription sub = getActiveSubscription();
        if(sub != null) {
            NotificationPreference preference = notificationPreferenceManager.getNotificationPreference(sub, sub.getTopics().stream().collect(Collectors.toList()).get(0).getId(), NotificationServiceType.API);
            String destination = preference != null ? preference.getDestination() : "";
            if (preference == null) {
                preference = mockTestData.mockAPINotificationPreference(sub);
            }
            preference.setDestination(String.valueOf(System.currentTimeMillis()));
            NotificationPreference saved = notificationPreferenceManager.saveNotificationPreference(preference);
            assertNotEquals(destination, saved.getDestination());
            assertEquals("integrationtest", preference.getDestinationParams().getApiKey());
            assertEquals("integrationtest", preference.getDestinationParams().getUsername());
        }
    }

    @Test
    public void canSaveNotificationLog()  {
        Notification notif = notificationRepository.findAll().get(0);
        if (notif == null) {
            return;
        }
        List<NotificationPreference> prefs = notificationPreferenceManager.getNotificationPreferences(notif.getSubscription(), notif.getIdTopic());
        notif.getNotificationLogs().stream().forEach(log->{
            log.getRetryCount();
            log.incrementRetryCount();
        });
        if (!prefs.isEmpty() && notif.getNotificationLogs().isEmpty()) {
            List<NotificationLog> l = new ArrayList<>();
            l.add(mockTestData.newMockLog(notif, prefs.get(0)));
            notif.setNotificationLogs(l);
        }
        Notification savedNotif = notificationRepository.save(notif);
        savedNotif.getNotificationLogs().stream().forEach(log->{
            long diff = System.currentTimeMillis() - log.getLastUpdated().getTime();
            //assert if updated in the last one second
            assertTrue(diff < 1000);
        });
    }

    @Test
    public void canGetNotifications()   {
        List<Notification> nots = dbManagerFacade.getUnsentNotifications(100);
        assertTrue(nots.size() > 0);
    }

    @Test
    public void canProcessUnsentNotifications()    {
        List<Notification> unsentNotifications = dbManagerFacade.getUnsentNotificationsWithActiveSubscription(100);
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
