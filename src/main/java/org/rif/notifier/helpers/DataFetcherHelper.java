package org.rif.notifier.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.rif.notifier.datafetcher.Datafetcher;
import org.rif.notifier.exception.InvalidListenableTypeException;
import org.rif.notifier.managers.DbManagerFacade;
import org.rif.notifier.models.datafetching.FetchedBlock;
import org.rif.notifier.models.datafetching.FetchedData;
import org.rif.notifier.models.datafetching.FetchedEvent;
import org.rif.notifier.models.datafetching.FetchedTransaction;
import org.rif.notifier.models.entities.*;
import org.rif.notifier.models.listenable.EthereumBasedListenable;
import org.rif.notifier.models.listenable.EthereumBasedListenableTypes;
import org.rif.notifier.scheduled.DataFetchingJob;
import org.rif.notifier.services.LuminoEventServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.protocol.core.methods.response.EthBlock;

import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static org.rif.notifier.constants.TopicParamTypes.*;
import static org.rif.notifier.constants.TopicTypes.CONTRACT_EVENT;

@Component
public class DataFetcherHelper {
    private static final Logger logger = LoggerFactory.getLogger(DataFetcherHelper.class);

    @Autowired
    DbManagerFacade dbManagerFacade;

    @Autowired
    private LuminoEventServices luminoEventServices;

    /**
     * Gets all topics for active subscriptions an creates a List of distinct listeneables for the library web3
     * In case when topicparams contains invalid value_type field, the exception is logged as warning
     * @return Returns a List<EthereumBasedListeneable> to listen to the blockchain events
     * @throws ClassNotFoundException When trying to parse the web3-type creating the listeneable for contract events
     */
    public List<EthereumBasedListenable> getListenablesForTopicsWithActiveSubscription() {
        Set<EthereumBasedListenable> ethereumBasedListenables = new HashSet<>();
        Set<Topic> activeTopics = dbManagerFacade.getAllTopicsWithActiveSubscriptionAndBalance();
        activeTopics.stream().forEach(topic->{
            try {
                EthereumBasedListenable newListeneable = Datafetcher.getEthereumBasedListenableFromTopic(topic);
                CollectionUtils.addIgnoreNull(ethereumBasedListenables, newListeneable);
            }catch(ClassNotFoundException e){
                logger.warn("Error while adding EthereumBasedListenable for topic: " + topic.getId(), e);
            }
        });
        return new ArrayList<>(ethereumBasedListenables);
    }

    /**
     * Process a list of FetchedBlock or FetchedTransaction
     * @param tasks list of FetchedBlock or FetchedTransaction
     * @param start start time of the scheduler in milliseconds
     * @param lastBlock lastBlock number to be saved in datafetcher
     * @param type NEW_BLOCK or NEW_TRANSACTIONS
     */
    public void processFetchedData(List<CompletableFuture<? extends List<? extends FetchedData>>> tasks, long start, BigInteger lastBlock, EthereumBasedListenableTypes type)  {
        tasks.forEach(listCompletableFuture -> {
            listCompletableFuture.whenComplete((fetchedDetails, throwable) -> {
                long end = System.currentTimeMillis();
                logger.info(Thread.currentThread().getId() + " - End fetching " + type.getType() + " task = " + (end - start));
                logger.info(Thread.currentThread().getId() + " - Completed fetching " + type.getType() +"s, Size: " + fetchedDetails.size());
                if (throwable != null) {
                    dbManagerFacade.saveLastBlock(lastBlock);
                } else {
                    List<RawData> rawTrs = fetchedDetails.stream().map(fetchedDetail-> {
                        RawData rwDt = new RawData(type.toString(), fetchedDetail.toString(), false, fetchedDetail.getBlockNumber(), fetchedDetail.getTopicId());
                        rwDt.setRowhashcode(rwDt.hashCode());
                        if (dbManagerFacade.getRawdataByHashcode(rwDt.getRowhashcode()) == null) {
                            return rwDt;
                        }
                        return null;
                    }).filter(r->r!=null).collect(Collectors.toList());
                    if (!rawTrs.isEmpty()) {
                        dbManagerFacade.saveRawDataBatch(rawTrs);
                    }
                }
            });
        });
    }

    public void processEventTasks(List<CompletableFuture<List<FetchedEvent>>> eventTasks, long start, BigInteger lastBlock, boolean fetchedTokens) {
        eventTasks.forEach(listCompletableFuture -> {
            listCompletableFuture.whenComplete((fetchedEvents, throwable) -> {
                long end = System.currentTimeMillis();
                logger.info(Thread.currentThread().getId() + " - End fetching events task = " + (end - start));
                logger.info(Thread.currentThread().getId() + " - Completed fetching events, size: " + fetchedEvents.size());
                if(throwable != null) {
                    dbManagerFacade.saveLastBlock(lastBlock);
                } else {
                    //Check if tokens were registered we can filter by idTopic -1
                    if (fetchedTokens) {
                        fetchedEvents.stream().filter(item -> item.getTopicId() == -1).forEach(item -> {
                            //if(luminoEventServices.isToken())
                            luminoEventServices.addToken(item.getValues().get(1).getValue().toString());
                        });
                    }
                    processFetchedEvents(fetchedEvents);
                }
            });
        });
    }

    /**
     * Iterates through Subscription given a Contract Address.
     * When iterating, it filters by the contract_address and takes all the topics related to that.
     * Then it checks if the user has filters for that Topic, and in that case, it filters the data, if all correct, saves a new RawData in the DB
     * When no filters apply, it directly saves the rawdata
     * @param fetchedEvents Fetched events from the library
     */
    public void processFetchedEvents(List<FetchedEvent> fetchedEvents){
        ObjectMapper mapper = new ObjectMapper();
        List<RawData> rawEvts = new ArrayList<>();
        fetchedEvents.forEach(fetchedEvent -> {
            try {
                String rawEvent = mapper.writeValueAsString(fetchedEvent);
                EventRawData rwDt = mapper.readValue(rawEvent, EventRawData.class);
                List<Subscription> subs = dbManagerFacade.findByContractAddressAndSubscriptionActive(rwDt.getContractAddress());
                for (Subscription sub : subs) {
                    //Here i'll filter all topics with event name same as rawdata and same contract address
                    sub.getTopics().stream().filter(item ->
                            item.getType().equals(CONTRACT_EVENT)
                                    && item.getTopicParams().stream().anyMatch(param ->
                                    param.getType().equals(EVENT_NAME)
                                            && param.getValue().equals(rwDt.getEventName())
                            )
                                    && item.getTopicParams().stream().anyMatch(param ->
                                    param.getType().equals(CONTRACT_ADDRESS)
                                            && param.getValue().equals(rwDt.getContractAddress())
                            )
                    ).forEach(tp -> {
                        //Iterate through the filtered data
                        rwDt.setTopicId(tp.getId());
                        //One user can have lots of filters for the same event, so we need to check if this subscription has some filters to apply
                        List<TopicParams> filterParams = new ArrayList<>();
                        //Try getting the parameters to be filtered
                        tp.getTopicParams().stream().filter(param ->
                                param.getType().equals(EVENT_PARAM)
                                        && param.getFilter() != null
                                        && !param.getFilter().isEmpty()
                        ).forEach(filterParams::add);
                        RawData newItem = new RawData(EthereumBasedListenableTypes.CONTRACT_EVENT.toString(), rwDt.toString(), false, fetchedEvent.getBlockNumber(), tp.getId());
                        AtomicBoolean tryAdd = new AtomicBoolean(true);
                        if (filterParams.size() > 0) {
                            //Got some filters to apply
                            filterParams.forEach(param -> {
                                //Param with filters
                                EventRawDataParam rawParam = rwDt.getValues().get(param.getOrder());
                                //This need to be checked in a function that checks the types as TYPE_NAME, etc
                                //Hardcoded because when retrieving from the listener the type is equals to string and not Utf8String
                                if (!((rawParam.getTypeAsString().toLowerCase().equals(param.getValueType().toLowerCase())
                                        || (param.getValueType().equals("Utf8String") && rawParam.getTypeAsString().equals(Utf8String.TYPE_NAME)))
                                        && rawParam.getValue().equals(param.getFilter()))
                                ){
                                    //Param is not the same type as the type getted by the listener or not got the same info
                                    tryAdd.set(false);
                                }
                            });
                        }
                        //Checking if the data was filtered
                        if (tryAdd.get()) {
                            if (rawEvts.size() > 0) {
                                //Rawdata was not added and need to be added
                                if (rawEvts.stream().noneMatch(raw -> raw.getBlock().equals(newItem.getBlock()) && raw.getIdTopic() == tp.getId() && raw.getData().equals(newItem.getData()))) {
                                    newItem.setRowhashcode(newItem.hashCode());
                                    if (dbManagerFacade.getRawdataByHashcode(newItem.getRowhashcode()) == null) {
                                        rawEvts.add(newItem);
                                    }
                                }
                            } else {
                                newItem.setRowhashcode(newItem.hashCode());
                                if (dbManagerFacade.getRawdataByHashcode(newItem.getRowhashcode()) == null) {
                                    rawEvts.add(newItem);
                                }
                            }
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        if(!rawEvts.isEmpty()) {
            dbManagerFacade.saveRawDataBatch(rawEvts);
        }
    }
}
