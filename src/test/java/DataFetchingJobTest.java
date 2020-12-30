import com.fasterxml.jackson.databind.ObjectMapper;
import mocked.MockTestData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.rif.notifier.exception.RSKBlockChainException;
import org.rif.notifier.helpers.DataFetcherHelper;
import org.rif.notifier.helpers.LuminoDataFetcherHelper;
import org.rif.notifier.managers.DbManagerFacade;
import org.rif.notifier.models.datafetching.FetchedBlock;
import org.rif.notifier.models.datafetching.FetchedData;
import org.rif.notifier.models.datafetching.FetchedEvent;
import org.rif.notifier.models.datafetching.FetchedTransaction;
import org.rif.notifier.models.entities.EventRawData;
import org.rif.notifier.models.entities.RawData;
import org.rif.notifier.models.entities.Subscription;
import org.rif.notifier.models.entities.Topic;
import org.rif.notifier.models.listenable.EthereumBasedListenable;
import org.rif.notifier.models.listenable.EthereumBasedListenableTypes;
import org.rif.notifier.scheduled.DataFetchingJob;
import org.rif.notifier.services.blockchain.generic.rootstock.RskBlockchainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;


@RunWith(MockitoJUnitRunner.class)
public class DataFetchingJobTest {

    private static final String START_FROM_RSK_LAST_BLOCK = "startFromRSKLastBlock";

    @Mock
    private RskBlockchainService rskBlockchainService;

    @InjectMocks
    @Autowired
    private DataFetcherHelper dataFetcherHelperInject;

    @Mock
    private DataFetcherHelper dataFetcherHelper;

    @Mock
    private DbManagerFacade dbManagerFacade;

    @Mock
    private DbManagerFacade dbManagerFacadeHelper;

    @Mock
    private LuminoDataFetcherHelper luminoDataFetcherHelper;

    @InjectMocks
    @Autowired
    private DataFetchingJob dataFetchingJob;

   private MockTestData mockTestData = new MockTestData();

    public void prepareDataFetcherTest(String dbLastBlock, String rskLastBlock) throws Exception    {
        ReflectionTestUtils.setField(dataFetcherHelper, "dbManagerFacade", dbManagerFacadeHelper);
        doReturn(new BigInteger(dbLastBlock)).when(dbManagerFacade).getLastBlock();
        doReturn(new BigInteger(rskLastBlock)).when(rskBlockchainService).getLastConfirmedBlock(any(BigInteger.class));
        doReturn(mockTestData.mockMixedTopics()).when(dbManagerFacadeHelper).getAllTopicsWithActiveSubscriptionAndBalance();
        doCallRealMethod().when(dataFetcherHelper).getListenablesForTopicsWithActiveSubscription();
        doReturn(CompletableFuture.completedFuture(new ArrayList<>())).when(rskBlockchainService).getBlocks(any(), any(), any());
        doReturn(CompletableFuture.completedFuture(new ArrayList<>())).when(rskBlockchainService).getTransactions(any(), any(), any());
        doReturn(CompletableFuture.completedFuture(new ArrayList<>())).when(rskBlockchainService).getContractEvents(any(), any(), any());
        when(luminoDataFetcherHelper.fetchTokens(anyLong(), any(BigInteger.class))).thenReturn(false);
    }

    /*
     * Verify either success or failure case depending on successMode parameter
     * For failure case, none of the methods should be executed except saveLastBlock depending on the invocation count
     */
    private void verifyDataFetcherRun(int saveLastBlockInvocationCount, boolean successMode) throws Exception    {
        verify(rskBlockchainService, successMode ? times(1) : never()).getBlocks(any(), any(BigInteger.class), any(BigInteger.class));
        verify(rskBlockchainService, successMode ? times(1) : never()).getTransactions(any(), any(BigInteger.class), any(BigInteger.class));
        verify(rskBlockchainService, successMode ? times(1) : never()).getContractEvents(any(), any(BigInteger.class), any(BigInteger.class));
        verify(dbManagerFacade, times(saveLastBlockInvocationCount)).saveLastBlock(any());
        verify(dataFetcherHelper, successMode ? times(2) : never()).processFetchedData(anyList(), anyLong(), any(BigInteger.class), any());
        verify(dataFetcherHelper, successMode ? times(1): never()).processEventTasks(anyList(), anyLong(), any(BigInteger.class), anyBoolean());
    }

    /**
     * This is the when startFromRSKLastBlock is false
     * @throws Exception
     */
    @Test
    public void skipFetchingWhenDBAndRSKAreSameLastBlock()  throws Exception    {
        prepareDataFetcherTest("10","10");
        dataFetchingJob.run();
        verifyDataFetcherRun(0, false);
        Mockito.reset(dataFetcherHelper);
    }

    @Test
    public void skipFetchingWhenDBBlockGreatherThanRSKLastBlock()  throws Exception    {
        prepareDataFetcherTest("11","10");
        dataFetchingJob.run();
        verifyDataFetcherRun(0, false);
        Mockito.reset(dataFetcherHelper);
    }

    @Test
    public void canFetchDataFromDBLastBlock()  throws Exception    {
        prepareDataFetcherTest("9","10");
        dataFetchingJob.run();
        verifyDataFetcherRun(1, true);
        Mockito.reset(dataFetcherHelper);
    }

    @Test
    public void canFetchDataFromRSKLastBlock()  throws Exception    {
        ReflectionTestUtils.setField(dataFetchingJob, START_FROM_RSK_LAST_BLOCK, true);
        //using random for db block since this is ignored in startFromRSKLastBlock
        prepareDataFetcherTest(String.valueOf(new Random().nextInt()),"10");
        dataFetchingJob.run();
        verifyDataFetcherRun(2, true);
        Mockito.reset(dataFetcherHelper);
    }

    /**
     * This is the when startFromRSKLastBlock is true
     * @throws Exception
     */
    @Test
    public void skipFetchingWhenSameBlockForRSKAndDB()  throws Exception    {
        ReflectionTestUtils.setField(dataFetchingJob, "startFromRSKLastBlock", true);
        prepareDataFetcherTest("10","10");
        dataFetchingJob.run();
        verifyDataFetcherRun(1, false);
        Mockito.reset(dataFetcherHelper);
    }

    @Test
    public void canFetchWhenZerothRskBlock()  throws Exception    {
        ReflectionTestUtils.setField(dataFetchingJob, START_FROM_RSK_LAST_BLOCK, true);
        //using random for db block since this is ignored in startFromRSKLastBlock
        prepareDataFetcherTest(String.valueOf(new Random().nextInt()),"0");
        dataFetchingJob.run();
        verifyDataFetcherRun(2, true);
        Mockito.reset(dataFetcherHelper);
    }

    @Test
    public void canProcessFetchedEvents() throws Exception {
        List<Subscription> lstSubs = new ArrayList<>();
        Subscription subscription = mockTestData.mockSubscription();
        lstSubs.add(subscription);
        FetchedEvent fetchedEvent = mockTestData.mockFetchedEvent();
        FetchedEvent fetchedEvent2 = new FetchedEvent
                ("LogSellArticle", fetchedEvent.getValues(), new BigInteger("54"), "0x0", 0);
        fetchedEvent2.setTopicId(-1);
        ObjectMapper mapper = new ObjectMapper();
        String rawEvent = mapper.writeValueAsString(fetchedEvent);
        EventRawData rwDt = mapper.readValue(rawEvent, EventRawData.class);
        List<FetchedEvent> fetchedEvents = new ArrayList<>();
        fetchedEvents.add(fetchedEvent);
        fetchedEvents.add(fetchedEvent2);

        doReturn(lstSubs).when(dbManagerFacade).findByContractAddressAndSubscriptionActive(rwDt.getContractAddress());

        dataFetcherHelperInject.processFetchedEvents(fetchedEvents);

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

        dataFetcherHelperInject.processFetchedEvents(fetchedEvents);

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

        dataFetcherHelperInject.processFetchedEvents(fetchedEvents);

        verify(dbManagerFacade, times(1)).findByContractAddressAndSubscriptionActive(any());
        verify(dbManagerFacade, times(1)).saveRawDataBatch(any());
    }
    @Test
    public void canGetListenables() throws Exception {
        Subscription subscription = mockTestData.mockSubscription();
        EthereumBasedListenable toCompare = mockTestData.mockEthereumBasedListeneable();
        doReturn(subscription.getTopics()).when(dbManagerFacade).getAllTopicsWithActiveSubscriptionAndBalance();
        List<EthereumBasedListenable> lstExpected = dataFetcherHelperInject.getListenablesForTopicsWithActiveSubscription();
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
        List<EthereumBasedListenable> lstExpected = dataFetcherHelperInject.getListenablesForTopicsWithActiveSubscription();
        verify(dbManagerFacade).getAllTopicsWithActiveSubscriptionAndBalance();
        assertEquals(lstExpected.size(), 3);
    }

    @Test
    public void canGetMixedListenablesAndFilterDuplicates()  throws Exception    {
        doReturn(mockTestData.mockMixedTopics()).when(dbManagerFacade).getAllTopicsWithActiveSubscriptionAndBalance();
        List<EthereumBasedListenable> lstExpected = dataFetcherHelperInject.getListenablesForTopicsWithActiveSubscription();
        assertEquals(lstExpected.size(), 3);
    }

    @Test
    public void errorGetListeneablesNoActiveSubscription() throws Exception {
        doReturn(new HashSet<>()).when(dbManagerFacade).getAllTopicsWithActiveSubscriptionAndBalance();

        List<EthereumBasedListenable> lstExpected = dataFetcherHelperInject.getListenablesForTopicsWithActiveSubscription();

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

        List<EthereumBasedListenable> lstExpected = dataFetcherHelperInject.getListenablesForTopicsWithActiveSubscription();

        //verify(dbManagerFacade, times(1)).saveRawDataBatch(any());
        assertEquals(0, lstExpected.size());
    }
    @Test
    public void errorProcessFetchedEventsNoFetchedEvents() throws Exception {
        List<FetchedEvent> fetchedEvents = new ArrayList<>();

        dataFetcherHelperInject.processFetchedEvents(fetchedEvents);

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

        dataFetcherHelperInject.processFetchedEvents(fetchedEvents);

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

        dataFetcherHelperInject.processFetchedEvents(fetchedEvents);

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

        List<EthereumBasedListenable> lstExpected = dataFetcherHelperInject.getListenablesForTopicsWithActiveSubscription();

        assertEquals(new ArrayList<>(), lstExpected);
    }

    @Test
    public void canProcessFetchedTransaction() {
        FetchedTransaction trans = mockTestData.mockFetchedTransaction();
        doReturn("0x0").when(trans).toString();
        List<FetchedTransaction> list = Arrays.asList(new FetchedTransaction[]{trans});
        CompletableFuture<List<FetchedTransaction>> futureTrans = CompletableFuture.completedFuture(list);
        List<CompletableFuture<? extends List<? extends FetchedData>>> futures = new ArrayList<>();
        futures.add(futureTrans);
        dataFetcherHelperInject.processFetchedData(futures, 0, new BigInteger("10"), EthereumBasedListenableTypes.NEW_TRANSACTIONS);
    }

    @Test
    public void canProcessFetchedTransactionNoExistingData() {
        FetchedTransaction trans = mockTestData.mockFetchedTransaction();
        doReturn(Mockito.mock(RawData.class)).when(dbManagerFacade).getRawdataByHashcode(anyInt());
        doReturn("0x0").when(trans).toString();
        List<FetchedTransaction> list = Arrays.asList(new FetchedTransaction[]{trans});
        CompletableFuture<List<FetchedTransaction>> futureTrans = CompletableFuture.completedFuture(list);
        List<CompletableFuture<? extends List<? extends FetchedData>>> futures = new ArrayList<>();
        futures.add(futureTrans);
        dataFetcherHelperInject.processFetchedData(futures, 0, new BigInteger("10"), EthereumBasedListenableTypes.NEW_TRANSACTIONS);
    }

    @Test
    public void canProcessFetchedBlock() {
        FetchedBlock block = mockTestData.mockFetchedBlock();
        doReturn("0x0").when(block).toString();
        List<FetchedBlock> list = Arrays.asList(new FetchedBlock[]{block});
        CompletableFuture<List<FetchedBlock>> futureBlock = CompletableFuture.completedFuture(list);
        List<CompletableFuture<? extends List<? extends FetchedData>>> futures = new ArrayList<>();
        futures.add(futureBlock);
        dataFetcherHelperInject.processFetchedData(futures, 0, new BigInteger("10"), EthereumBasedListenableTypes.NEW_BLOCK);
    }

    @Test
    public void canProcessEventTasks() {
        FetchedEvent event = mockTestData.mockFetchedEvent();
        List<FetchedEvent> list = Arrays.asList(new FetchedEvent[]{event});
        CompletableFuture<List<FetchedEvent>> futureEvent = CompletableFuture.completedFuture(list);
        List<CompletableFuture<List<FetchedEvent>>> futures = new ArrayList<>();
        futures.add(futureEvent);
        dataFetcherHelperInject.processEventTasks(futures, 0, new BigInteger("10"), false);
    }

    @Test(expected=RSKBlockChainException.class)
    public void errorFetchLastConfirmedBlock()    throws Exception {
        prepareDataFetcherTest("9", "10");
        doThrow(RuntimeException.class).when(rskBlockchainService).getBlocks(any(), any(BigInteger.class), any(BigInteger.class));
        dataFetchingJob.run();
    }
}