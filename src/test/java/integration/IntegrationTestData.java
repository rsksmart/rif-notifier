package integration;

import mocked.MockTestData;
import org.rif.notifier.constants.SubscriptionConstants;
import org.rif.notifier.constants.TopicTypes;
import org.rif.notifier.managers.DbManagerFacade;
import org.rif.notifier.managers.datamanagers.NotificationPreferenceManager;
import org.rif.notifier.managers.datamanagers.TopicManager;
import org.rif.notifier.models.entities.*;
import org.rif.notifier.repositories.SubscriptionTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

/**
 * Serves to insert data into database for integration testing, only inserts data once for subscription, topic, user, and subscriptiontype
 * for notification table, a new records is always created
 */
@Component
public class IntegrationTestData {

    @Autowired private SubscriptionTypeRepository subTypeRepo;
    @Autowired private TopicManager topicManager;
    @Autowired DbManagerFacade dbManagerFacade;
    @Autowired NotificationPreferenceManager notificationPreferenceManager;

    //subscription user
    @Value("${notificationservice.integrationtest.user}") private String user;
    //subscription user apikey
    @Value("${notificationservice.integrationtest.user.apikey}") private String userApiKey;

    @Value("${notificationservice.integrationtest.apiendpoint}") private String apiEndpoint;
    @Value("${notificationservice.integrationtest.apiendpoint.apikey}") private String apiKey;
    @Value("${notificationservice.integrationtest.apiendpoint.user}") private String username;
    @Value("${notificationservice.integrationtest.apiendpoint.password}") private String password;

    private SubscriptionType subscriptionType ;
    private Subscription activeSubscription;
    private int topicId;
    private NotificationPreference preference;
    private Notification notification;
    private MockTestData mockTestData = new MockTestData();

    protected SubscriptionType getSubscriptionType() {
        return subscriptionType;
    }

    protected Subscription getActiveSubscription() {
        return activeSubscription;
    }

    protected int getTopicId() {
        return topicId;
    }

    protected Notification getNotification()    {
        return notification;
    }

    protected String getUser()    {
        return user;
    }
    protected String getUserName()    {
        return username;
    }
    protected String getPassword()    {
        return password;
    }

    private void setupUser()    {
        dbManagerFacade.saveUser(user, userApiKey);
    }
    private void setupSubscriptionType()       {
        subscriptionType  = subTypeRepo.findByNotifications(10000);
        if (subscriptionType == null) {
            subscriptionType = new SubscriptionType();
            subscriptionType.setNotifications(10000);
            subscriptionType = subTypeRepo.save(subscriptionType);
        }
    }
    private void setupSubscription()    {
        Date date = new Date();
        activeSubscription = dbManagerFacade.getSubscriptionByAddress(user);
        if (activeSubscription == null) {
            activeSubscription = dbManagerFacade.createSubscription(date, user, subscriptionType, SubscriptionConstants.PAYED_PAYMENT);
        }
    }
    private void setupTopic()   {
        int hashCode = "integrationtesthash".hashCode();
        Topic topic = topicManager.getTopicByHashCode(hashCode);
        if (topic == null)  {
            topic = topicManager.insert(TopicTypes.NEW_BLOCK, String.valueOf(hashCode), activeSubscription);
        }
        topicId = topic.getId();
    }
    private void setupNotification()   {
        String data = "{\"data\":\"integrationtest-millis-" + System.currentTimeMillis() +"\"}";
        notification = new Notification(getActiveSubscription(), new Timestamp(new Date().getTime()).toString(), false, data, topicId);
        notification.setNotificationLogs(new ArrayList<NotificationLog>());
    }

    private void setupNotificationPreference()   {
        preference = notificationPreferenceManager.getNotificationPreference(this.getActiveSubscription(), topicId, NotificationServiceType.API);
        if (preference == null) {
            preference = newNotificationPreference();
            //save the preference
            preference = notificationPreferenceManager.saveNotificationPreference(preference);
        }
    }

    protected NotificationPreference newNotificationPreference()  {
        NotificationPreference preference = new NotificationPreference();
        preference.setNotificationService(NotificationServiceType.API);
        preference.setIdTopic(topicId);
        preference.setSubscription(activeSubscription);
        preference.setDestination(apiEndpoint);
        DestinationParams dp = new DestinationParams();
        dp.setApiKey(apiKey);
        dp.setUsername(username);
        dp.setUsername(password);
        preference.setDestinationParams(dp);
        return preference;
    }

    private void setupNotificationLog() {
        NotificationLog notificationLog = new NotificationLog();
        //associate each new log to a notification preference
        notificationLog.setNotificationPreference(preference);
        notificationLog.setNotification(notification);
        notification.getNotificationLogs().add(notificationLog);
    }

    public void setup() {
        setupUser();
        setupSubscriptionType();
        setupSubscription();
        setupTopic();
        setupNotificationPreference();
        setupNotification();
        setupNotificationLog();
    }
}
