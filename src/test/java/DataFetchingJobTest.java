import com.fasterxml.jackson.databind.ObjectMapper;
import mocked.MockTestData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.rif.notifier.managers.DbManagerFacade;
import org.rif.notifier.models.datafetching.FetchedEvent;
import org.rif.notifier.models.entities.EventRawData;
import org.rif.notifier.models.entities.Subscription;
import org.rif.notifier.models.listenable.EthereumBasedListenable;
import org.rif.notifier.scheduled.DataFetchingJob;
import org.rif.notifier.services.blockchain.generic.rootstock.RskBlockchainService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;


@RunWith(MockitoJUnitRunner.class)
public class DataFetchingJobTest {
    @Mock
    private RskBlockchainService rskBlockchainService;

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

        dataFetchingJob.processFetchedEvents(fetchedEvents);

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

        dataFetchingJob.processFetchedEvents(fetchedEvents);

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

        dataFetchingJob.processFetchedEvents(fetchedEvents);

        verify(dbManagerFacade, times(1)).findByContractAddressAndSubscriptionActive(any());
        verify(dbManagerFacade, times(1)).saveRawDataBatch(any());
    }
    @Test
    public void canGetListeneables() throws Exception {
        List<Subscription> lstSubs = new ArrayList<>();
        Subscription subscription = mockTestData.mockSubscription();
        EthereumBasedListenable toCompare = mockTestData.mockEthereumBasedListeneable();
        lstSubs.add(subscription);


        doReturn(lstSubs).when(dbManagerFacade).getAllActiveSubscriptionsWithBalance();

        List<EthereumBasedListenable> lstExpected = dataFetchingJob.getListeneables();


        //verify(dbManagerFacade, times(1)).saveRawDataBatch(any());
        assertEquals(lstExpected.get(0).toString(), toCompare.toString());
    }
    @Test
    public void errorGetListeneablesNoActiveSubscription() throws Exception {
        doReturn(new ArrayList<>()).when(dbManagerFacade).getAllActiveSubscriptionsWithBalance();

        List<EthereumBasedListenable> lstExpected = dataFetchingJob.getListeneables();

        //verify(dbManagerFacade, times(1)).saveRawDataBatch(any());
        assertEquals(0, lstExpected.size());
    }
    @Test
    public void errorGetListeneablesSubWithNoTopics() throws Exception {
        List<Subscription> lstSubs = new ArrayList<>();
        Subscription subscription = mockTestData.mockSubscription();
        subscription.setTopics(new HashSet<>());
        lstSubs.add(subscription);

        doReturn(lstSubs).when(dbManagerFacade).getAllActiveSubscriptionsWithBalance();

        List<EthereumBasedListenable> lstExpected = dataFetchingJob.getListeneables();

        //verify(dbManagerFacade, times(1)).saveRawDataBatch(any());
        assertEquals(0, lstExpected.size());
    }
    @Test
    public void errorProcessFetchedEventsNoFetchedEvents() throws Exception {
        List<FetchedEvent> fetchedEvents = new ArrayList<>();

        dataFetchingJob.processFetchedEvents(fetchedEvents);

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

        dataFetchingJob.processFetchedEvents(fetchedEvents);

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

        dataFetchingJob.processFetchedEvents(fetchedEvents);

        verify(dbManagerFacade, times(1)).findByContractAddressAndSubscriptionActive(any());
        verify(dbManagerFacade, times(0)).saveRawDataBatch(any());
    }
    @Test
    public void errorGetListeneablesIncompleteDataForListeneable() throws Exception {
        List<Subscription> lstSubs = new ArrayList<>();
        Subscription subscription = mockTestData.mockSubscriptionWithInvalidTopic();
        EthereumBasedListenable toCompare = mockTestData.mockInvalidEthereumBasedListeneable();
        lstSubs.add(subscription);

        doReturn(lstSubs).when(dbManagerFacade).getAllActiveSubscriptionsWithBalance();

        List<EthereumBasedListenable> lstExpected = dataFetchingJob.getListeneables();

        assertEquals(new ArrayList<>(), lstExpected);
    }
}