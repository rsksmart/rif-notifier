package integration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.rif.notifier.Application;
import org.rif.notifier.managers.DbManagerFacade;
import org.rif.notifier.managers.datamanagers.NotificationPreferenceManager;
import org.rif.notifier.models.entities.*;
import org.rif.notifier.repositories.NotificationPreferenceRepository;
import org.rif.notifier.repositories.NotificationRepository;
import org.rif.notifier.scheduled.NotificationProcessorJob;
import org.rif.notifier.services.NotificationServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

/**
 * Integration tests for NotificationProcessorJob and NotificationServices
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class, IntegrationTestData.class})
@ActiveProfiles("test")
public class NotificationServiceIntegrationTest {
    @Value("${notificationservice.maxretries}") private int maxRetries;

    @Autowired DbManagerFacade dbManagerFacade;

    @Autowired NotificationPreferenceManager notificationPreferenceManager;

    @Autowired NotificationRepository notificationRepository;
    @Autowired
    NotificationPreferenceRepository notificationPreferenceRepository;
    @Autowired NotificationServices notificationServices;
    @Autowired IntegrationTestData integrationTestData;
    @Autowired NotificationProcessorJob notificationProcessorJob;


    private void saveApiEndpoint(Notification notif, String endpoint)    {
        Subscription activeSubscription = integrationTestData.getSubscription();
        int topicId = integrationTestData.getTopicId();
        NotificationPreference pref = notificationPreferenceRepository.findBySubscriptionAndIdTopicAndNotificationService(activeSubscription, topicId, NotificationServiceType.API);
        pref.setDestination(endpoint);
        notificationPreferenceManager.saveNotificationPreference(pref);
    }

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
    public void errorAPINotification()    {
        Notification notif = integrationTestData.newNotification();
        integrationTestData.populateNotificationLog(notif);
        //use invalid api endpoint to propagate error
        notif.getNotificationLogs().stream().findFirst().get().getNotificationPreference().setDestination(IntegrationTestData.INVALID_API_DESTINATION);
        notificationServices.sendNotification(notif, maxRetries);
        notif = notificationServices.saveNotification(notif);
        assertFalse(notif.isSent());
    }

    @Test
    public void canSaveNotificationPreferences()  {
        NotificationPreference preference = notificationPreferenceManager.getNotificationPreference(integrationTestData.getSubscription(), integrationTestData.getTopicId(), NotificationServiceType.API);
        String destination = preference != null ? preference.getDestination() : "";
        if (preference == null) {
            preference = integrationTestData.newNotificationPreference(NotificationServiceType.API);
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

    /**
     * Verifies by creating new test notification with preference as specified in IntegrationTestData
     * Then runs the notificationProcessorJob.run() to test if the notifications are processed by the
     * method, if the newly created notification is sent successfully, the test passes
     */
    @Test
    public void canProcessUnsentNotifications()    {
        //ensure to set all notifications sent to true so the test can start fresh
        List<Notification> nots = notificationRepository.findAll();
        nots.forEach(n->n.setSent(true));
        notificationRepository.saveAll(nots);
        //now run the actual test
        Notification notif = integrationTestData.newNotification();
        //integrationTestData.populateNotificationLog(notif);
        notif = dbManagerFacade.saveNotification(notif);
        notificationProcessorJob.run();
        notif = notificationRepository.findById(notif.getId()).get();
        assertTrue(notif.isSent());
    }

    /**
     * This needs to use invalid notification prefeence
     */
    @Test
    public void errorProcessingUnsentNotifications()    {
        Notification notif = integrationTestData.newNotification();
        //use invalid endpoint to propagate error
        saveApiEndpoint(notif, IntegrationTestData.INVALID_API_DESTINATION);
        notif = dbManagerFacade.saveNotification(notif);
        notificationProcessorJob.run();
        notificationProcessorJob.run();
        notif = notificationRepository.findById(notif.getId()).get();
        notif.getNotificationLogs().forEach(l->
        {
           assertEquals(2, l.getRetryCount());
        });
        assertFalse(notif.isSent());
        //set it back to valid endpoint
        saveApiEndpoint(notif, integrationTestData.getApiEndpoint());

    }

    /**
     * This needs to use invalid notification prefeence
     */
    @Test
    public void errorProcessingUnsentNotificationsMaxRetries()    {
        Notification notif = integrationTestData.newNotification();
        //use invalid endpoint to propagate error
        saveApiEndpoint(notif, IntegrationTestData.INVALID_API_DESTINATION);
        notif = dbManagerFacade.saveNotification(notif);
        //Max retry is 3
        //Run the notifier job 5 times to verify the retry count does not exceed 3
        IntStream.range(1,5).forEach(n->{
            notificationProcessorJob.run();
        });
        notif = notificationRepository.findById(notif.getId()).get();
        notif.getNotificationLogs().forEach(l->
        {
            assertEquals(3, l.getRetryCount());
        });
        assertFalse(notif.isSent());
        //set it back to valid endpoint
        saveApiEndpoint(notif, integrationTestData.getApiEndpoint());

    }
}
