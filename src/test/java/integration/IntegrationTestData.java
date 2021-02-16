package integration;

import mocked.MockTestData;
import org.rif.notifier.constants.TopicTypes;
import org.rif.notifier.managers.DbManagerFacade;
import org.rif.notifier.managers.datamanagers.NotificationPreferenceManager;
import org.rif.notifier.managers.datamanagers.TopicManager;
import org.rif.notifier.models.datafetching.FetchedEvent;
import org.rif.notifier.models.entities.*;
import org.rif.notifier.models.entities.Currency;
import org.rif.notifier.repositories.SubscriptionPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.*;

/**
 * Serves to insert data into database for integration testing, only inserts data once for subscription, topic, user, and subscriptiontype
 * for notification table, a new records is always created
 */
@Component
public class IntegrationTestData {

    protected static final String INVALID_API_DESTINATION = "http://localhost:8080/invalidendpoint";

    @Autowired private SubscriptionPlanRepository subTypeRepo;
    @Autowired private TopicManager topicManager;
    @Autowired DbManagerFacade dbManagerFacade;
    @Autowired NotificationPreferenceManager notificationPreferenceManager;

    public static final Address TEST_ADDRESS = new Address("0x5b4Ff44769C1Da53ee4eBeCd641bA7B4926DFdBd");

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

    private SubscriptionPlan subscriptionPlan ;
    private SubscriptionPrice subscriptionPrice;
    private Subscription activeSubscription;
    private Currency currency;
    private int topicId;
    private NotificationPreference apiPreference;
    private NotificationPreference invalidApiPreference;
    private NotificationPreference smsPreference;
    private NotificationPreference emailPreference;
    private Notification notification;
    private MockTestData mockTestData = new MockTestData();

    protected SubscriptionPlan getSubscriptionType() {
        return subscriptionPlan;
    }

    protected Subscription getSubscription() {
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
        preference = destination == null ? notificationPreferenceManager.getNotificationPreference(this.getSubscription(), topicId, type) :
                notificationPreferenceManager.getNotificationPreference(this.getSubscription(), topicId, type, destination);
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
        Notification notification = new Notification(getSubscription(), new Timestamp(new Date().getTime()).toString(), false, data, topicId);
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

    public FetchedEvent paymentEvent(String eventName, Address provider, Subscription subscription){
        List<Type> values = new ArrayList<>();
        values.add(new Utf8String(subscription.getHash()));
        values.add(provider);
        values.add(new Uint256(subscription.getPrice()));
        values.add(subscription.getCurrency().getAddress());
        FetchedEvent fetchedEvent = new FetchedEvent
                (eventName, values, new BigInteger("55"), "0x0", 0);

        return  fetchedEvent;
    }

    private void setupUser()    {
        dbManagerFacade.saveUser(user, userApiKey);
    }

    private void setupCurrency()    {
       currency = dbManagerFacade.getCurrencyByName("RIF").orElseGet(()->{
            Currency c = new Currency("RIF", new Address("0x0")) ;
            dbManagerFacade.saveCurrency(c);
            return c;
       });
    }

    private void setupSubscriptionPlan()       {
        subscriptionPlan  = subTypeRepo.findByNotificationQuantity(10000);
        if (subscriptionPlan == null) {
            subscriptionPlan = new SubscriptionPlan(10000);
            subscriptionPlan.setValidity(100);
            subscriptionPrice = new SubscriptionPrice(new BigInteger("100"), currency);
            subscriptionPlan.setName("test");
            subscriptionPrice.setSubscriptionPlan(subscriptionPlan);
            subscriptionPlan.setSubscriptionPriceList(Collections.singletonList(subscriptionPrice));
            subscriptionPlan.setNotificationPreferences(Collections.singleton(NotificationServiceType.SMS));
            subscriptionPlan = subTypeRepo.save(subscriptionPlan);
        }
        else    {
            subscriptionPrice = subscriptionPlan.getSubscriptionPriceList().stream().findFirst().get();
        }
    }

    private void setupSubscription(SubscriptionStatus status, boolean createAlways)    {
        //10 days
        Date date = new Date(System.currentTimeMillis()+86400*10000);
        activeSubscription = dbManagerFacade.getSubscriptionByAddress(user).stream().findFirst().orElse(null);
        if (createAlways || activeSubscription == null) {
            activeSubscription = dbManagerFacade.createSubscription(date, user, subscriptionPlan, status, subscriptionPrice);
        }
        else    {
            if(activeSubscription.getExpirationDate().getTime() < System.currentTimeMillis())   {
                activeSubscription.setStatus(status);
                activeSubscription.setExpirationDate(date);
                dbManagerFacade.updateSubscription(activeSubscription);
            }
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
       setup(SubscriptionStatus.ACTIVE, false);
    }

    public void setup(SubscriptionStatus status, boolean createAlways) {
        setupUser();
        setupCurrency();
        setupSubscriptionPlan();
        setupSubscription(status, createAlways);
        setupTopic();
        setupNotificationPreferences();
        setupNotification();
        setupNotificationLog();
    }
}
