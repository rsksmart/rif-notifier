package integration;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.rif.notifier.Application;
import org.rif.notifier.managers.DbManagerFacade;
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
    NotificationPreferenceRepository notificationPreferenceRepository;

    @Autowired
    NotificationRepository notificationRepository;

    @Test
    @Ignore
    public void canSaveNotificationPreferences()  {
        List<Subscription> activeSubs = dbManagerFacade.getAllActiveSubscriptions();
        if (!activeSubs.isEmpty()) {
            Subscription sub = activeSubs.get(0);
            NotificationPreference preference = notificationPreferenceRepository.findBySubscriptionAndIdTopicAndNotificationService(sub, 10, NotificationServiceType.API);
            String destination = preference != null ? preference.getDestination() : "";
            if (preference == null) {
                preference = new NotificationPreference();
                preference.setNotificationService(NotificationServiceType.API);
                int topicId = sub.getTopics().isEmpty() ? 10 : (sub.getTopics().stream().collect(Collectors.toList()).get(0).getId());
                preference.setIdTopic(topicId);
                preference.setSubscription(sub);
                DestinationParams dp = new DestinationParams();
                dp.setApiKey("integrationtest");
                dp.setUsername("integrationtest");
                dp.setPassword("integrationtest");
                preference.setDestinationParams(dp);
            }
            preference.setDestination(String.valueOf(System.currentTimeMillis()));
            NotificationPreference saved = notificationPreferenceRepository.save(preference);
            assertNotEquals(destination, saved.getDestination());
            assertEquals("integrationtest", preference.getDestinationParams().getApiKey());
            assertEquals("integrationtest", preference.getDestinationParams().getUsername());
        }
    }

    @Test
    @Ignore
    public void canReadNotificationPreferences()  {
        List<Subscription> activeSubs = dbManagerFacade.getAllActiveSubscriptions();
        for(Subscription sub : activeSubs){
            List<Notification> notifications = dbManagerFacade.getNotificationByUserAddress(sub.getUserAddress(), null, null, null);
            for(NotificationPreference preference : sub.getNotificationPreferences()){
                System.out.println(preference.getDestination());
            }
        }
    }

    @Test
    public void canSaveNotificationLog()  {
        Notification notif = notificationRepository.findAll().get(0);
        NotificationLog log = new NotificationLog();
        Optional<NotificationPreference> pref = notificationPreferenceRepository.findById(12);
        log.setNotificationPreference(pref.get());
        log.setNotification(notif);
        log.setRetryCount(1);
        log.setSent(false);
        Set<NotificationLog> l = new HashSet<>();
        l.add(log);
        notif.setNotificationLogs(l);
        notificationRepository.save(notif);
    }
}
