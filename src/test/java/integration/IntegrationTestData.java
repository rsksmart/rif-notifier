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
import java.util.Arrays;
import java.util.Date;

/**
 * Serves to insert data into database for integration testing, only inserts data once for subscription, topic, user, and subscriptiontype
 * for notification table, a new records is always created
 */
@Component
public class IntegrationTestData {

    protected static final String INVALID_API_DESTINATION = "http://localhost:8080/invalidendpoint";

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

    @Value("${notificationservice.integrationtest.smsdestination}") private String smsDestination;
    @Value("${notificationservice.integrationtest.emaildestination}") private String emailDestination;

    private SubscriptionType subscriptionType ;
    private Subscription activeSubscription;
    private int topicId;
    private NotificationPreference apiPreference;
    private NotificationPreference invalidApiPreference;
    private NotificationPreference smsPreference;
    private NotificationPreference emailPreference;
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
    public String getApiEndpoint() { return apiEndpoint; }

    protected NotificationPreference newNotificationPreference(NotificationServiceType type)    {
        NotificationPreference preference = new NotificationPreference();
        preference.setNotificationService(type);
        preference.setIdTopic(topicId);
        preference.setSubscription(activeSubscription);
        preference.setDestination(type == NotificationServiceType.API ? apiEndpoint : (type == NotificationServiceType.EMAIL ? emailDestination : smsDestination));
        if (type == NotificationServiceType.API) {
            DestinationParams dp = new DestinationParams();
            dp.setApiKey(apiKey);
            dp.setUsername(username);
            dp.setUsername(password);
            preference.setDestinationParams(dp);
        }
        return preference;
    }

    private NotificationPreference findOrCreateNotificationPreference(NotificationServiceType type, String destination)   {
        NotificationPreference preference = null;
        preference = destination == null ? notificationPreferenceManager.getNotificationPreference(this.getActiveSubscription(), topicId, type) :
                notificationPreferenceManager.getNotificationPreference(this.getActiveSubscription(), topicId, type, destination);
        if (preference == null) {
            preference = newNotificationPreference(type);
            if (destination != null)    {
                preference.setDestination(destination);
            }
            preference = notificationPreferenceManager.saveNotificationPreference(preference);
        }
        return preference;
    }

    protected Notification newNotification()  {
        String data = "{\"data\":\"integrationtest-millis-" + System.currentTimeMillis() +"\"}";
        Notification notification = new Notification(getActiveSubscription(), new Timestamp(new Date().getTime()).toString(), false, data, topicId);
        notification.setNotificationLogs(new ArrayList<NotificationLog>());
        return notification;
    }

    protected void populateNotificationLog(Notification notif) {
       populateNotificationLog(notif, apiPreference);
    }
    protected void populateInvalidNotificationLog(Notification notif) {
        populateNotificationLog(notif, invalidApiPreference);
    }
    protected void populateNotificationLog(Notification notif, NotificationPreference preference) {
        Arrays.asList(preference/*, smsPreference, emailPreference*/).forEach(p -> {
            NotificationLog notificationLog = new NotificationLog();
            //associate each new log to a notification preference
            notificationLog.setNotificationPreference(p);
            notificationLog.setNotification(notif);
            notif.getNotificationLogs().add(notificationLog);
        });
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
       notification = newNotification();
    }

    private void setupNotificationPreferences()   {
        apiPreference = findOrCreateNotificationPreference(NotificationServiceType.API, apiEndpoint);
        //invalidApiPreference = findOrCreateNotificationPreference(NotificationServiceType.API, INVALID_API_DESTINATION);
        //smsPreference = findOrCreateNotificationPreference(NotificationServiceType.SMS);
        //emailPreference = findOrCreateNotificationPreference(NotificationServiceType.EMAIL);
    }

    private void setupNotificationLog() {
        populateNotificationLog(notification);
    }

    public void setup() {
        setupUser();
        setupSubscriptionType();
        setupSubscription();
        setupTopic();
        setupNotificationPreferences();
        setupNotification();
        setupNotificationLog();
    }
}
