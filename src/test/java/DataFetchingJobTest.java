import com.fasterxml.jackson.databind.ObjectMapper;
import mocked.MockTestData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.rif.notifier.constants.TopicTypes;
import org.rif.notifier.helpers.DataFetcherHelper;
import org.rif.notifier.managers.DbManagerFacade;
import org.rif.notifier.models.datafetching.FetchedEvent;
import org.rif.notifier.models.entities.EventRawData;
import org.rif.notifier.models.entities.Subscription;
import org.rif.notifier.models.entities.Topic;
import org.rif.notifier.models.listenable.EthereumBasedListenable;
import org.rif.notifier.scheduled.DataFetchingJob;
import org.rif.notifier.services.blockchain.generic.rootstock.RskBlockchainService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;


@RunWith(MockitoJUnitRunner.class)
public class DataFetchingJobTest {
    @Mock
    private RskBlockchainService rskBlockchainService;

    @InjectMocks
    @Autowired
    private DataFetcherHelper dataFetcherHelper;

    @Mock
    private DbManagerFacade dbManagerFacade;

    @InjectMocks
    @Autowired
    private DataFetchingJob dataFetchingJob;

    private MockTestData mockTestData = new MockTestData();

    @Test
    public void canProcessFetchedEvents() throws Exception {
        List<Subscription> lstSubs = new ArrayList<>();
        Subscription subscription = mockTestData.mockSubscription();
        lstSubs.add(subscription);
        FetchedEvent fetchedEvent = mockTestData.mockFetchedEvent();
        ObjectMapper mapper = new ObjectMapper();
        String rawEvent = mapper.writeValueAsString(fetchedEvent);
        EventRawData rwDt = mapper.readValue(rawEvent, EventRawData.class);
        List<FetchedEvent> fetchedEvents = new ArrayList<>();
        fetchedEvents.add(fetchedEvent);

        doReturn(lstSubs).when(dbManagerFacade).findByContractAddressAndSubscriptionActive(rwDt.getContractAddress());

        dataFetcherHelper.processFetchedEvents(fetchedEvents);

        verify(dbManagerFacade, times(1)).saveRawDataBatch(any());
    }
    @Test
    public void canProcessFetchedEventsWithFilters() throws Exception {
        List<Subscription> lstSubs = new ArrayList<>();
        Subscription subscription = mockTestData.mockSubscriptionWithFilters();
        lstSubs.add(subscription);
        FetchedEvent fetchedEvent = mockTestData.mockFetchedEvent();
        ObjectMapper mapper = new ObjectMapper();
        String rawEvent = mapper.writeValueAsString(fetchedEvent);
        EventRawData rwDt = mapper.readValue(rawEvent, EventRawData.class);
        List<FetchedEvent> fetchedEvents = new ArrayList<>();
        fetchedEvents.add(fetchedEvent);

        doReturn(lstSubs).when(dbManagerFacade).findByContractAddressAndSubscriptionActive(rwDt.getContractAddress());

        dataFetcherHelper.processFetchedEvents(fetchedEvents);

        verify(dbManagerFacade, times(1)).findByContractAddressAndSubscriptionActive(any());
        verify(dbManagerFacade, times(1)).saveRawDataBatch(any());
    }
    @Test
    public void canProcessFetchedEventsWithoutParams() throws Exception {
        List<Subscription> lstSubs = new ArrayList<>();
        Subscription subscription = mockTestData.mockSubscriptionWithTopicWithoutParameters();
        lstSubs.add(subscription);
        FetchedEvent fetchedEvent = mockTestData.mockFetchedEvent();
        ObjectMapper mapper = new ObjectMapper();
        String rawEvent = mapper.writeValueAsString(fetchedEvent);
        EventRawData rwDt = mapper.readValue(rawEvent, EventRawData.class);
        List<FetchedEvent> fetchedEvents = new ArrayList<>();
        fetchedEvents.add(fetchedEvent);

        doReturn(lstSubs).when(dbManagerFacade).findByContractAddressAndSubscriptionActive(rwDt.getContractAddress());

        dataFetcherHelper.processFetchedEvents(fetchedEvents);

        verify(dbManagerFacade, times(1)).findByContractAddressAndSubscriptionActive(any());
        verify(dbManagerFacade, times(1)).saveRawDataBatch(any());
    }
    @Test
    public void canGetListenables() throws Exception {
        Subscription subscription = mockTestData.mockSubscription();
        EthereumBasedListenable toCompare = mockTestData.mockEthereumBasedListeneable();
        doReturn(subscription.getTopics()).when(dbManagerFacade).getAllTopicsWithActiveSubscriptionAndBalance();
        List<EthereumBasedListenable> lstExpected = dataFetcherHelper.getListenablesForTopicsWithActiveSubscription();
        assertEquals(lstExpected.get(0).toString(), toCompare.toString());
    }

    @Test
    public void canGetVaryingContractTopicsAndFilterDuplicateListenables()  throws Exception    {
        Set<Topic> topics = new HashSet<>();
        topics.add(mockTestData.mockTopic());
        Topic logSellArticle1 = mockTestData.mockTopicWithEvent("LogSellArticle", "0x1");
        Topic logSellArticle2 = mockTestData.mockTopicWithEvent("LogSellArticle", "0x2");
        topics.add(logSellArticle1);
        topics.add(logSellArticle2);
        topics.add(mockTestData.mockTopicWithFilters());
        topics.add(mockTestData.mockTopicWithoutParams());
        topics.add(mockTestData.mockInvalidTopic());

        doReturn(topics).when(dbManagerFacade).getAllTopicsWithActiveSubscriptionAndBalance();
        List<EthereumBasedListenable> lstExpected = dataFetcherHelper.getListenablesForTopicsWithActiveSubscription();
        verify(dbManagerFacade).getAllTopicsWithActiveSubscriptionAndBalance();
        assertEquals(lstExpected.size(), 3);
    }

    @Test
    public void canGetMixedListenablesAndFilterDuplicates()  throws Exception    {
        Set<Topic> topics = new HashSet<>();
        topics.add(mockTestData.mockTopicForType(TopicTypes.NEW_BLOCK ));
        topics.add(mockTestData.mockTopicForType(TopicTypes.NEW_BLOCK ));
        topics.add(mockTestData.mockTopicForType(TopicTypes.NEW_TRANSACTIONS));
        topics.add(mockTestData.mockTopicForType(TopicTypes.NEW_TRANSACTIONS));
        topics.add(mockTestData.mockTopic());
        topics.add(mockTestData.mockTopic());
        doReturn(topics).when(dbManagerFacade).getAllTopicsWithActiveSubscriptionAndBalance();
        List<EthereumBasedListenable> lstExpected = dataFetcherHelper.getListenablesForTopicsWithActiveSubscription();
        assertEquals(lstExpected.size(), 3);
    }

    @Test
    public void errorGetListeneablesNoActiveSubscription() throws Exception {
        doReturn(new HashSet<>()).when(dbManagerFacade).getAllTopicsWithActiveSubscriptionAndBalance();

        List<EthereumBasedListenable> lstExpected = dataFetcherHelper.getListenablesForTopicsWithActiveSubscription();

        //verify(dbManagerFacade, times(1)).saveRawDataBatch(any());
        assertEquals(0, lstExpected.size());
    }
    @Test
    public void errorGetListeneablesSubWithNoTopics() throws Exception {
        //List<Subscription> lstSubs = new ArrayList<>();
        Set<Topic> lstSubs = new HashSet<>();
        Subscription subscription = mockTestData.mockSubscription();
        subscription.setTopics(new HashSet<>());
        lstSubs.addAll(subscription.getTopics());

        doReturn(lstSubs).when(dbManagerFacade).getAllTopicsWithActiveSubscriptionAndBalance();

        List<EthereumBasedListenable> lstExpected = dataFetcherHelper.getListenablesForTopicsWithActiveSubscription();

        //verify(dbManagerFacade, times(1)).saveRawDataBatch(any());
        assertEquals(0, lstExpected.size());
    }
    @Test
    public void errorProcessFetchedEventsNoFetchedEvents() throws Exception {
        List<FetchedEvent> fetchedEvents = new ArrayList<>();

        dataFetcherHelper.processFetchedEvents(fetchedEvents);

        verify(dbManagerFacade, times(0)).saveRawDataBatch(any());
        verify(dbManagerFacade, times(0)).findByContractAddressAndSubscriptionActive(any());
    }
    @Test
    public void errorProcessFetchedEventsNoSubscriptionToContract() throws Exception {
        List<Subscription> lstSubs = new ArrayList<>();
        List<FetchedEvent> fetchedEvents = new ArrayList<>();
        FetchedEvent fetchedEvent = mockTestData.mockFetchedEvent();
        fetchedEvents.add(fetchedEvent);
        ObjectMapper mapper = new ObjectMapper();
        String rawEvent = mapper.writeValueAsString(fetchedEvent);
        EventRawData rwDt = mapper.readValue(rawEvent, EventRawData.class);

        doReturn(lstSubs).when(dbManagerFacade).findByContractAddressAndSubscriptionActive(rwDt.getContractAddress());

        dataFetcherHelper.processFetchedEvents(fetchedEvents);

        verify(dbManagerFacade, times(1)).findByContractAddressAndSubscriptionActive(any());
        verify(dbManagerFacade, times(0)).saveRawDataBatch(any());
    }
    @Test
    public void errorProcessFetchedEventsWithWrongFilters() throws Exception {
        List<Subscription> lstSubs = new ArrayList<>();
        Subscription subscription = mockTestData.mockSubscriptionWithFilters();
        lstSubs.add(subscription);
        FetchedEvent fetchedEvent = mockTestData.mockFetchedEventAlternative();
        ObjectMapper mapper = new ObjectMapper();
        String rawEvent = mapper.writeValueAsString(fetchedEvent);
        EventRawData rwDt = mapper.readValue(rawEvent, EventRawData.class);
        List<FetchedEvent> fetchedEvents = new ArrayList<>();
        fetchedEvents.add(fetchedEvent);

        doReturn(lstSubs).when(dbManagerFacade).findByContractAddressAndSubscriptionActive(rwDt.getContractAddress());

        dataFetcherHelper.processFetchedEvents(fetchedEvents);

        verify(dbManagerFacade, times(1)).findByContractAddressAndSubscriptionActive(any());
        verify(dbManagerFacade, times(0)).saveRawDataBatch(any());
    }
    @Test
    public void errorGetListeneablesIncompleteDataForListeneable() throws Exception {
        Set<Topic> lstSubs = new HashSet<>();
        Subscription subscription = mockTestData.mockSubscriptionWithInvalidTopic();
        EthereumBasedListenable toCompare = mockTestData.mockInvalidEthereumBasedListeneable();
        lstSubs.addAll(subscription.getTopics());

        doReturn(lstSubs).when(dbManagerFacade).getAllTopicsWithActiveSubscriptionAndBalance();

        List<EthereumBasedListenable> lstExpected = dataFetcherHelper.getListenablesForTopicsWithActiveSubscription();

        assertEquals(new ArrayList<>(), lstExpected);
    }
}