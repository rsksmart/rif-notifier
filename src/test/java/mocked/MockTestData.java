package mocked;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.Mockito;
import org.rif.notifier.constants.TopicParamTypes;
import org.rif.notifier.constants.TopicTypes;
import org.rif.notifier.models.DTO.SubscriptionBatchDTO;
import org.rif.notifier.models.DTO.SubscriptionDTO;
import org.rif.notifier.models.DTO.TopicDTO;
import org.rif.notifier.models.datafetching.FetchedBlock;
import org.rif.notifier.models.datafetching.FetchedEvent;
import org.rif.notifier.models.datafetching.FetchedTransaction;
import org.rif.notifier.models.entities.Currency;
import org.rif.notifier.models.entities.*;
import org.rif.notifier.models.listenable.EthereumBasedListenable;
import org.rif.notifier.models.listenable.EthereumBasedListenableTypes;
import org.rif.notifier.models.web3Extensions.RSKTypeReference;
import org.rif.notifier.util.Utils;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.rif.notifier.constants.TopicParamTypes.*;

public class MockTestData {

    private static final String PATH_TO_TYPES = "org.web3j.abi.datatypes.";

    public static final String PRIVATE_KEY = "a392604efc2fad9c0b3da43b5f698a2e3f270f170d859912be0d54742275c5f6";

    private ObjectMapper mapper = new ObjectMapper();

    public Topic mockTopic() throws IOException {
        return mockTopicWithEvent("LogSellArticle", "0x0");
    }

    public Topic mockTopicForType(TopicTypes type)    throws IOException  {
        Topic t = new Topic();
        t.setId((int)Math.random());
        t.setType(type);
        return t;
    }

    public Topic mockTopicWithEvent(String eventName, String address)   throws IOException  {
        String sTp = "{" +
                "\"type\": \"CONTRACT_EVENT\"," +
                "\"topicParams\":[" +
                "{" +
                "\"type\": \"CONTRACT_ADDRESS\"," +
                "\"value\": \"" + address + "\"," +
                "\"valueType\": \"string\"," +
                "\"indexed\": 0" +
                "}," +
                "{" +
                "\"type\": \"EVENT_NAME\"," +
                "\"value\": \""+eventName + "\"," +
                "\"valueType\": \"string\"," +
                "\"indexed\": 0" +
                "}," +
                "{" +
                "\"type\": \"EVENT_PARAM\"," +
                "\"value\": \"seller\"," +
                "\"order\": 0," +
                "\"valueType\": \"Address\"," +
                "\"indexed\": 1" +
                "}," +
                "{" +
                "\"type\": \"EVENT_PARAM\"," +
                "\"value\": \"article\"," +
                "\"order\": 1," +
                "\"valueType\": \"Utf8String\"," +
                "\"indexed\": 0" +
                "}," +
                "{" +
                "\"type\": \"EVENT_PARAM\"," +
                "\"value\": \"price\"," +
                "\"order\": 2," +
                "\"valueType\": \"Uint256\"," +
                "\"indexed\": 0" +
                "}" +
                "]" +
                "}";
        return mapper.readValue(sTp, Topic.class);
    }

    public Topic mockTopicWithFilters() throws IOException {
        String sTp = "{" +
                "\"type\": \"CONTRACT_EVENT\"," +
                "\"topicParams\":[" +
                "{" +
                "\"type\": \"CONTRACT_ADDRESS\"," +
                "\"value\": \"0x0\"," +
                "\"valueType\": \"string\"," +
                "\"indexed\": 0" +
                "}," +
                "{" +
                "\"type\": \"EVENT_NAME\"," +
                "\"value\": \"LogSellArticle\"," +
                "\"valueType\": \"string\"," +
                "\"indexed\": 0" +
                "}," +
                "{" +
                "\"type\": \"EVENT_PARAM\"," +
                "\"value\": \"seller\"," +
                "\"order\": 0," +
                "\"valueType\": \"Address\"," +
                "\"indexed\": 1," +
                "\"filter\": \"0x913eebc253aeb9d6a42b45b66b690f9c4619fa14\"" +
                "}," +
                "{" +
                "\"type\": \"EVENT_PARAM\"," +
                "\"value\": \"article\"," +
                "\"order\": 1," +
                "\"valueType\": \"Utf8String\"," +
                "\"indexed\": 0," +
                "\"filter\": \"Article 1\"" +
                "}," +
                "{" +
                "\"type\": \"EVENT_PARAM\"," +
                "\"value\": \"price\"," +
                "\"order\": 2," +
                "\"valueType\": \"Uint256\"," +
                "\"indexed\": 0" +
                "}" +
                "]" +
                "}";
        return mapper.readValue(sTp, Topic.class);
    }
    public Topic mockTopicWithoutParams() throws IOException {
        String sTp = "{" +
                "\"type\": \"CONTRACT_EVENT\"," +
                "\"topicParams\":[" +
                "{" +
                "\"type\": \"CONTRACT_ADDRESS\"," +
                "\"value\": \"0x0\"," +
                "\"valueType\": \"string\"," +
                "\"indexed\": 0" +
                "}," +
                "{" +
                "\"type\": \"EVENT_NAME\"," +
                "\"value\": \"LogSellArticle\"," +
                "\"valueType\": \"string\"," +
                "\"indexed\": 0" +
                "}" +
                "]" +
                "}";
        return mapper.readValue(sTp, Topic.class);
    }
    public Topic mockTopicOpenChannelWithoutFilters() {
        Topic topic = new Topic();
        topic.setType(TopicTypes.CONTRACT_EVENT);
        List<TopicParams> params = new ArrayList<>();
        TopicParams param = new TopicParams(null, TopicParamTypes.CONTRACT_ADDRESS, "123456789", 0, null, false, null);
        params.add(param);
        param = new TopicParams(null, TopicParamTypes.EVENT_NAME, "ChannelOpened", 0, null, false, null);
        params.add(param);
        param = new TopicParams(null, TopicParamTypes.EVENT_PARAM, "channel_identifier", 0, "Uint256", true, null);
        params.add(param);
        param = new TopicParams(null, TopicParamTypes.EVENT_PARAM, "participant1", 1, "Address", true, null);
        params.add(param);
        param = new TopicParams(null, TopicParamTypes.EVENT_PARAM, "participant2", 2, "Address", true,  null);
        params.add(param);
        param = new TopicParams(null, TopicParamTypes.EVENT_PARAM, "settle_timeout", 3, "Uint256", false, null);
        params.add(param);
        topic.setTopicParams(params);
        return topic;
    }
    public Topic mockInvalidTopic() throws IOException {
        String sTp = "{" +
                "\"type\": \"CONTRACT_EVENT\"," +
                "\"topicParams\":[" +
                "{" +
                "\"type\": \"EVENT_NAME\"," +
                "\"value\": \"LogSellArticle\"," +
                "\"valueType\": \"string\"," +
                "\"indexed\": 0" +
                "}," +
                "{" +
                "\"type\": \"EVENT_PARAM\"," +
                "\"value\": \"seller\"," +
                "\"order\": 0," +
                "\"valueType\": \"Address\"," +
                "\"indexed\": 1" +
                "}," +
                "{" +
                "\"type\": \"EVENT_PARAM\"," +
                "\"value\": \"article\"," +
                "\"order\": 1," +
                "\"valueType\": \"Utf8String\"," +
                "\"indexed\": 0" +
                "}," +
                "{" +
                "\"type\": \"EVENT_PARAM\"," +
                "\"value\": \"price\"," +
                "\"order\": 2," +
                "\"valueType\": \"Uint256\"," +
                "\"indexed\": 0" +
                "}" +
                "]" +
                "}";
        return mapper.readValue(sTp, Topic.class);
    }

    public Set<Topic> mockMixedTopics()    throws IOException  {
        Set<Topic> topics = new HashSet<>();
        topics.add(mockTopicForType(TopicTypes.NEW_BLOCK ));
        topics.add(mockTopicForType(TopicTypes.NEW_BLOCK ));
        topics.add(mockTopicForType(TopicTypes.NEW_TRANSACTIONS));
        topics.add(mockTopicForType(TopicTypes.NEW_TRANSACTIONS));
        topics.add(mockTopic());
        topics.add(mockTopic());
        return topics;
    }

    public List<Notification> mockNotifications() throws IOException    {
        return mockNotifications(0);
    }
    public List<Notification> mockNotifications(int topicId) throws IOException {
        List<Notification> retLst = new ArrayList<>();
        Date date = new Date();
        for(int i=0;i<10;i++) {
            Notification notif = new Notification(mockSubscription(), new Timestamp(date.getTime()).toString(), false, "{id: " + i + ", counter: " + i + "}", topicId);
            notif.setNotificationLogs(new ArrayList<NotificationLog>());
            retLst.add(notif);
        }
        return retLst;
    }
    public Subscription mockSubscription() throws IOException {
        SubscriptionPlan type = this.mockSubscriptionPlan();
        User user = this.mockUser();
        Subscription sub = new Subscription(new Date(), user.getAddress(), type, SubscriptionStatus.ACTIVE);
        Topic topic = this.mockTopic();
        Set<Topic> topics = new HashSet<>();
        topics.add(topic);
        sub.setTopics(topics);
        sub.setCurrency(mockCurrency().get());
        return sub;
    }
    public SubscriptionPayment mockPayment(Subscription sub)    {
        return mockPayment(sub, BigInteger.TEN);
    }
    public SubscriptionPayment mockPayment(Subscription sub, BigInteger paymentAmount)    {
        SubscriptionPayment payment = new SubscriptionPayment(paymentAmount, sub, mockCurrency().get(), SubscriptionPaymentStatus.RECEIVED);
        return payment;
    }
    public SubscriptionPayment mockRefund(Subscription sub, BigInteger refundAmount)    {
        SubscriptionPayment payment = new SubscriptionPayment(refundAmount, sub, mockCurrency().get(), SubscriptionPaymentStatus.REFUNDED);
        return payment;
    }
    public Subscription mockPaidSubscription() throws IOException {
        SubscriptionPlan type = this.mockSubscriptionPlan();
        User user = this.mockUser();
        Subscription sub = new Subscription(new Date(), user.getAddress(), type, SubscriptionStatus.PENDING);
        sub.setPrice(BigInteger.TEN);
        sub.setCurrency(mockCurrency().get());
        sub.setSubscriptionPayments(Stream.of(mockPayment(sub)).collect(Collectors.toList()));
        Topic topic = this.mockTopic();
        Set<Topic> topics = new HashSet<>();
        topics.add(topic);
        sub.setTopics(topics);
        return sub;
    }
    public Subscription mockSubscriptionWithInvalidTopic() throws IOException {
        SubscriptionPlan type = this.mockSubscriptionPlan();
        User user = this.mockUser();
        Subscription sub = new Subscription(new Date(), user.getAddress(), type, SubscriptionStatus.ACTIVE);
        Topic topic = this.mockInvalidTopic();
        Set<Topic> topics = new HashSet<>();
        topics.add(topic);
        sub.setTopics(topics);
        sub.setCurrency(mockCurrency().get());
        return sub;
    }
    public Subscription mockSubscriptionWithFilters() throws IOException {
        SubscriptionPlan type = this.mockSubscriptionPlan();
        User user = this.mockUser();
        Subscription sub = new Subscription(new Date(), user.getAddress(), type, SubscriptionStatus.ACTIVE);
        Topic topic = this.mockTopicWithFilters();
        Set<Topic> topics = new HashSet<>();
        topics.add(topic);
        sub.setTopics(topics);
        sub.setCurrency(mockCurrency().get());
        return sub;
    }
    public Subscription mockSubscriptionWithTopicWithoutParameters() throws IOException {
        SubscriptionPlan type = this.mockSubscriptionPlan();
        User user = this.mockUser();
        Subscription sub = new Subscription(new Date(), user.getAddress(), type, SubscriptionStatus.ACTIVE);
        Topic topic = this.mockTopicWithoutParams();
        Set<Topic> topics = new HashSet<>();
        topics.add(topic);
        sub.setTopics(topics);
        sub.setCurrency(mockCurrency().get());
        return sub;
    }
    public Subscription mockInactiveSubscription(){
        SubscriptionPlan type = this.mockSubscriptionPlan();
        User user = this.mockUser();
        Subscription sub = new Subscription(new Date(), user.getAddress(), type, SubscriptionStatus.PENDING);
        sub.setStatus(SubscriptionStatus.PENDING);
        sub.setPrice(BigInteger.TEN);
        sub.setSubscriptionPayments(Arrays.asList(mockPayment(sub)));
        sub.setCurrency(mockCurrency().get());
        return sub;
    }
    public User mockUser(){
        return new User("0x7bDB21b2d21EE4b30FB4Bb791781F7D17f465309", "123456789");
    }
    public SubscriptionPlan mockSubscriptionPlan(){
        SubscriptionPlan plan = new SubscriptionPlan(1000);
        plan.setId(1);
        plan.setValidity(1);
        return plan;
    }
    public SubscriptionPrice mockSubscriptionPrice()   {
        Currency c = new Currency("RSK", new Address("0x0"));
        SubscriptionPrice p = new SubscriptionPrice(new BigInteger("20"), c);
        p.setSubscriptionPlan(mockSubscriptionPlan());
        return p;
    }
    public List<Subscription> mockListActiveSubs() throws IOException {
        List<Subscription> lstSubs = new ArrayList<>();
        Set<Topic> lstTopics = new HashSet<>();
        Subscription subscription = mockSubscription();
        Topic topic = mockTopic();
        lstTopics.add(topic);
        subscription.setTopics(lstTopics);
        lstSubs.add(subscription);
        return lstSubs;
    }
    public EthereumBasedListenable mockEthereumBasedListeneable() throws IOException, ClassNotFoundException {
        List<TypeReference<?>> params = new ArrayList<>();
        Topic tp = mockTopic();
        String address = tp.getTopicParams().stream()
                .filter(item -> item.getType().equals(CONTRACT_ADDRESS)).findFirst().get().getValue();
        String eventName = tp.getTopicParams().stream()
                .filter(item -> item.getType().equals(EVENT_NAME)).findFirst().get().getValue();
        List<TopicParams> topicParams = tp.getTopicParams().stream()
                .filter(item -> item.getType().equals(EVENT_PARAM))
                .collect(Collectors.toList());
        for(TopicParams param : topicParams){
            String value = param.getValueType();
            boolean indexed = param.getIndexed();
            Class myClass;
            //Get the reflection of the datatype
            if(Utils.isClass(PATH_TO_TYPES + value)){
                myClass = Class.forName(PATH_TO_TYPES + value);
            }else{
                myClass = Class.forName(PATH_TO_TYPES + "generated." + value);
            }

            TypeReference paramReference = RSKTypeReference.createWithIndexed(myClass, indexed);

            params.add(paramReference);
        }
        return new EthereumBasedListenable(address, EthereumBasedListenableTypes.CONTRACT_EVENT, params, eventName, tp.getId());
    }
    public EthereumBasedListenable mockInvalidEthereumBasedListeneable(){
        return new EthereumBasedListenable("0x0", EthereumBasedListenableTypes.CONTRACT_EVENT, new ArrayList<>(), "InvalidName", 0);
    }

    public FetchedTransaction mockFetchedTransaction()  {
        Transaction t = new Transaction();
        t.setBlockNumber("0x0");
        FetchedTransaction trans = Mockito.mock(FetchedTransaction.class);
        trans.setTransaction(t);
        trans.setTopicId(10);
        return trans;
    }

    public FetchedBlock mockFetchedBlock()  {
        EthBlock.Block b = new EthBlock.Block();
        b.setNumber("0x0");
        FetchedBlock block = Mockito.mock(FetchedBlock.class);
        block.setBlock(b);
        block.setTopicId(10);
        return block;
    }
    public FetchedEvent mockFetchedEvent(){
        List<Type > values = new ArrayList<>();
        Address address = new Address("0x913eebc253aeb9d6a42b45b66b690f9c4619fa14");
        Utf8String article = new Utf8String("Article 1");
        Uint256 price = new Uint256(100000);
        values.add(address);
        values.add(article);
        values.add(price);
        FetchedEvent fetchedEvent = new FetchedEvent
                ("LogSellArticle", values, new BigInteger("55"), "0x0", 0);

        return  fetchedEvent;
    }
    public FetchedEvent mockFetchedEventAlternative(){
        List<Type > values = new ArrayList<>();
        Address address = new Address("0x1");
        Utf8String article = new Utf8String("Article 2");
        Uint256 price = new Uint256(10000);
        values.add(address);
        values.add(article);
        values.add(price);
        FetchedEvent fetchedEvent = new FetchedEvent
                ("LogUpdateArticle", values, new BigInteger("80"), "0x0", 1);

        return  fetchedEvent;
    }
    public List<RawData> mockRawData(){
        List<RawData> rtnLst = new ArrayList<>();
        RawData rwDt = new RawData("0","CONTRACT_EVENT", mockFetchedEvent().toString(), false, new BigInteger("55"), mockFetchedEvent().getTopicId());
        rtnLst.add(rwDt);
        rwDt = new RawData("1", "CONTRACT_EVENT", mockFetchedEventAlternative().toString(), false, new BigInteger("60"), mockFetchedEventAlternative().getTopicId());
        rtnLst.add(rwDt);
        return rtnLst;
    }

    public NotificationPreference mockNotificationPreference(Subscription subscription)  {
        NotificationPreference pref = new NotificationPreference();
        pref.setId(1);
        pref.setDestination("test@test.com");
        pref.setNotificationService(NotificationServiceType.EMAIL);
        pref.setIdTopic(0);
        pref.setSubscription(subscription);
        return pref;
    }

    public NotificationPreference mockAPINotificationPreference(Subscription sub) {
        NotificationPreference preference = new NotificationPreference();
        preference.setNotificationService(NotificationServiceType.API);
        int topicId = sub.getTopics().isEmpty() ? 12 : (sub.getTopics().stream().collect(Collectors.toList()).get(0).getId());
        preference.setIdTopic(topicId);
        preference.setSubscription(sub);
        DestinationParams dp = new DestinationParams();
        dp.setApiKey("integrationtest");
        dp.setUsername("integrationtest");
        dp.setPassword("integrationtest");
        preference.setDestinationParams(dp);
        return preference;
    }

    public NotificationLog newMockLog(Notification notif, NotificationPreference pref)    {
        NotificationLog log = new NotificationLog();
        log.setNotificationPreference(pref);
        log.setNotification(notif);
        log.setRetryCount(0);
        log.setSent(false);
        return log;
    }

    public List<NotificationPreference> mockNotificationPreferences(int topicId) throws Exception  {
        List<NotificationPreference> notificationPreferences = new ArrayList<>();
        Subscription sub = mockSubscription();
        Arrays.asList(NotificationServiceType.values()).forEach(t->{
            NotificationPreference pref = new NotificationPreference();
            pref.setIdTopic(topicId);
            pref.setSubscription(sub);
            pref.setNotificationService(t);
            pref.setDestination("mockdestination");
            pref.setDestinationParams(new DestinationParams());
            notificationPreferences.add(pref);
        });
        return notificationPreferences;
    }

    public List<TopicDTO> mockTopics()  throws IOException {
        NotificationPreference pref = mockAPINotificationPreference(mockSubscription());
        pref.setDestination("http://test");
        TopicDTO dto = new TopicDTO();
        dto.setType(TopicTypes.NEW_BLOCK);
        dto.setNotificationPreferences(Arrays.asList(pref));
        return Arrays.asList(dto);
    }

    public SubscriptionBatchDTO mockSubscriptionBatch() throws IOException  {
        SubscriptionBatchDTO mock = new SubscriptionBatchDTO();
        mock.setPrice(BigInteger.TEN);
        mock.setCurrency("RIF");
        mock.setTopics(mockTopics());
        mock.setUserAddress("0x0");
        mock.setSubscriptionPlanId(1);
        return mock;
    }

    public SubscriptionDTO mockSubscriptionDTO()    throws IOException {
       SubscriptionDTO subscriptionDTO = new SubscriptionDTO();
       subscriptionDTO.setPrice(BigInteger.TEN);
       subscriptionDTO.setCurrency(new Currency("RIF", new Address("0x0")));
       subscriptionDTO.setUserAddress("0x0");
       subscriptionDTO.setProviderAddress(new Address("0x0"));
       subscriptionDTO.setNotificationBalance(10000);
       subscriptionDTO.setStatus(SubscriptionStatus.PENDING);
       subscriptionDTO.setTopics(mockTopics());
       return subscriptionDTO;
    }

    public FetchedEvent mockPaymentEvent(String eventName)  {
        return mockPaymentEvent(eventName,new Address("0x0"));
    }

    public FetchedEvent mockPaymentEvent(String eventName, Address provider){
        List<Type > values = new ArrayList<>();
        Bytes32 hash = new Bytes32(Numeric.hexStringToByteArray(Utils.generateHash("testhash")));
        Address currencyAddress = new Address("0x0");
        Uint256 price = new Uint256(100000);
        if (eventName.equals("SubscriptionCreated")) {
            values.add(hash);
            values.add(provider);
            values.add(currencyAddress);
            values.add(price);
        }
        else {
            values.add(provider);
            values.add(hash);
            values.add(price);
            values.add(currencyAddress);
        }
        FetchedEvent fetchedEvent = new FetchedEvent
                (eventName, values, new BigInteger("55"), "0x0", 0);

        return  fetchedEvent;
    }

    public List<CompletableFuture<List<FetchedEvent>>> mockFutureEvent(FetchedEvent event)   {
        List<FetchedEvent> list = Arrays.asList(new FetchedEvent[]{event});
        CompletableFuture<List<FetchedEvent>> futureEvent = CompletableFuture.completedFuture(list);
        List<CompletableFuture<List<FetchedEvent>>> futures = new ArrayList<>();
        futures.add(futureEvent);
        return futures;
    }

    public Optional<Currency> mockCurrency()  {
        return Optional.of(new Currency("RIF", new Address("0x0")));
    }
}
