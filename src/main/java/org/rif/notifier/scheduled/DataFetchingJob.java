package org.rif.notifier.scheduled;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.rif.notifier.datafetcher.Datafetcher;
import org.rif.notifier.managers.DbManagerFacade;
import org.rif.notifier.models.datafetching.FetchedBlock;
import org.rif.notifier.models.datafetching.FetchedEvent;
import org.rif.notifier.models.datafetching.FetchedTransaction;
import org.rif.notifier.models.entities.*;
import org.rif.notifier.models.listenable.EthereumBasedListenable;
import org.rif.notifier.models.listenable.EthereumBasedListenableTypes;
import org.rif.notifier.services.LuminoEventServices;
import org.rif.notifier.services.blockchain.generic.rootstock.RskBlockchainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.web3j.abi.TypeReference;
import org.web3j.utils.Numeric;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Bytes4;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static org.rif.notifier.constants.CommonConstants.RSK_SLIP_ADDRESS;
import static org.rif.notifier.constants.TopicParamTypes.*;
import static org.rif.notifier.constants.TopicTypes.*;


@Component
public class DataFetchingJob {

    private static final Logger logger = LoggerFactory.getLogger(DataFetchingJob.class);

    @Autowired
    private RskBlockchainService rskBlockchainService;

    @Autowired
    private DbManagerFacade dbManagerFacade;

    @Autowired
    private LuminoEventServices luminoEventServices;

    @Value("${rsk.blockchain.tokennetworkregistry}")
    private String tokenNetworkRegistry;

    @Value("${rsk.blockchain.multichaincontract}")
    private String multiChainContract;

    private boolean fetchedTokens = false;

    @Value("${notifier.blocks.startFromLast}")
    private boolean onInit;

    /**
     * Creates listeneables, then try to get the blockchain events related.
     * When all the events are fetched try to process the rawdata getted calling methods
     */
    @Scheduled(fixedRateString = "${notifier.run.fixedRateFetchingJob}", initialDelayString = "${notifier.run.fixedDelayFetchingJob}")
    public void run() throws Exception {
        // Get latest block for this run
        BigInteger to = rskBlockchainService.getLastBlock();
        BigInteger from;
        BigInteger fromChainAddresses;
        if (onInit) {
            //Saving lastblock so it starts fetching from here
            dbManagerFacade.saveLastBlock(to);
            from = to;
            onInit = false;
        } else {
            from = dbManagerFacade.getLastBlock();
            from = from.add(new BigInteger("1"));
        }
        if(from.compareTo(to) < 0) {
            //Fetching
            logger.info(Thread.currentThread().getId() + String.format(" - Starting fetching from %s to %s", from, to));

            long start = System.currentTimeMillis();
            List<CompletableFuture<List<FetchedBlock>>> blockTasks = new ArrayList<>();
            List<CompletableFuture<List<FetchedTransaction>>> transactionTasks = new ArrayList<>();
            List<CompletableFuture<List<FetchedEvent>>> eventTasks = new ArrayList<>();
            List<EthereumBasedListenable> ethereumBasedListenables = getListeneables();
            if (fetchedTokens) {
                ethereumBasedListenables.add(getTokensNetwork());
            }
            for (EthereumBasedListenable subscriptionChannel : ethereumBasedListenables) {
                try {
                    switch (subscriptionChannel.getKind()) {
                        case NEW_BLOCK:
                            blockTasks.add(rskBlockchainService.getBlocks(subscriptionChannel, from, to));
                            break;
                        case CONTRACT_EVENT:
                            eventTasks.add(rskBlockchainService.getContractEvents(subscriptionChannel, from, to));
                            break;
                        case NEW_TRANSACTIONS:
                            transactionTasks.add(rskBlockchainService.getTransactions(subscriptionChannel, from, to));
                            break;
                    }
                } catch (Exception e) {
                    logger.error("Error during DataFetching job: ", e);
                }
            }
            dbManagerFacade.saveLastBlock(to);


            blockTasks.forEach(listCompletableFuture -> {
                listCompletableFuture.whenComplete((fetchedBlocks, throwable) -> {
                    long end = System.currentTimeMillis();
                    logger.info(Thread.currentThread().getId() + " - End fetching blocks task = " + (end - start));
                    logger.info(Thread.currentThread().getId() + " - Completed fetching blocks, Size: " + fetchedBlocks.size());
                    fetchedBlocks.forEach(fetchedBlock -> {
                        if (fetchedBlock.getBlock().getTransactions() != null && fetchedBlock.getBlock().getTransactions().size() > 0) {
                            RawData rwDt = new RawData(EthereumBasedListenableTypes.NEW_BLOCK.toString(), fetchedBlock.toString(), false, fetchedBlock.getBlock().getNumber(), fetchedBlock.getTopicId());
                        }
                    });
                    List<RawData> rawTrs = fetchedBlocks.stream().map(fetchedBlock -> new RawData(EthereumBasedListenableTypes.NEW_BLOCK.toString(), fetchedBlock.toString(), false, fetchedBlock.getBlock().getNumber(), fetchedBlock.getTopicId())).
                            collect(Collectors.toList());
                    if (!rawTrs.isEmpty()) {
                        dbManagerFacade.saveRawDataBatch(rawTrs);
                    }
                });
            });

            transactionTasks.forEach(listCompletableFuture -> {
                listCompletableFuture.whenComplete((fetchedTransactions, throwable) -> {
                    long end = System.currentTimeMillis();
                    logger.info(Thread.currentThread().getId() + " - End fetching transactions task = " + (end - start));
                    logger.info(Thread.currentThread().getId() + " - Completed fetching transactions, size: " + fetchedTransactions.size());
                    List<RawData> rawTrs = fetchedTransactions.stream().map(fetchedTransaction -> new RawData(EthereumBasedListenableTypes.NEW_TRANSACTIONS.toString(), fetchedTransaction.toString(), false, fetchedTransaction.getTransaction().getBlockNumber(), fetchedTransaction.getTopicId())).
                            collect(Collectors.toList());
                    if (!rawTrs.isEmpty()) {
                        dbManagerFacade.saveRawDataBatch(rawTrs);
                    }
                });
            });

            eventTasks.forEach(listCompletableFuture -> {
                listCompletableFuture.whenComplete((fetchedEvents, throwable) -> {
                    long end = System.currentTimeMillis();
                    logger.info(Thread.currentThread().getId() + " - End fetching events task = " + (end - start));
                    logger.info(Thread.currentThread().getId() + " - Completed fetching events, size: " + fetchedEvents.size());
                    //Check if tokens were registered we can filter by idTopic -1
                    if (fetchedTokens) {
                        fetchedEvents.stream().filter(item -> item.getTopicId() == -1).forEach(item -> {
                            //if(luminoEventServices.isToken())
                            luminoEventServices.addToken(item.getValues().get(1).getValue().toString());
                        });
                    }
                    processFetchedEvents(fetchedEvents);
                });
            });
            ///////////////////////////////////Token Registry extract///////////////////////////////////////////
            if (!fetchedTokens) {
                List<CompletableFuture<List<FetchedEvent>>> tokenTasks = new ArrayList<>();
                tokenTasks.add(rskBlockchainService.getContractEvents(getTokensNetwork(), new BigInteger("0"), to));

                tokenTasks.forEach(listCompletableFuture -> {
                    listCompletableFuture.whenComplete((fetchedEvents, throwable) -> {
                        long end = System.currentTimeMillis();
                        logger.info(Thread.currentThread().getId() + " - End fetching tokens = " + (end - start));
                        logger.info(Thread.currentThread().getId() + " - Completed fetching tokens: " + fetchedEvents);
                        for (FetchedEvent fetchedEvent : fetchedEvents) {
                            luminoEventServices.addToken(fetchedEvent.getValues().get(1).getValue().toString());
                        }
                    });
                });
                if (luminoEventServices.getTokens().size() > 0) {
                    fetchedTokens = true;
                }
            }
        }else{
            logger.info(Thread.currentThread().getId() + " - Nothing to fetch yet -");
        }
    }

    @Scheduled(fixedRateString = "${notifier.run.fixedRateFetchingChainAddresses}", initialDelayString = "${notifier.run.fixedDelayFetchingChainAddresses}")
    public void runChainAddresses() throws Exception {
        // Get latest block for this run
        BigInteger to = rskBlockchainService.getLastBlock();
        BigInteger fromChainAddresses;
        fromChainAddresses = dbManagerFacade.getLastBlockForChainAddresses();
        fromChainAddresses = fromChainAddresses.add(new BigInteger("1"));
        long start = System.currentTimeMillis();
        List<CompletableFuture<List<FetchedEvent>>> chainAddresses = new ArrayList<>();
        chainAddresses.add(rskBlockchainService.getContractEvents(getAddrChanged(), fromChainAddresses, to));
        chainAddresses.add(rskBlockchainService.getContractEvents(getChainAddrChanged(), fromChainAddresses, to));

        chainAddresses.forEach(listCompletableFuture -> {
            listCompletableFuture.whenComplete((fetchedEvents, throwable) -> {
                List<ChainAddressEvent> chainsEvents = new ArrayList<>();
                long end = System.currentTimeMillis();
                logger.info(Thread.currentThread().getId() + " - End fetching chainaddres = " + (end - start));
                logger.info(Thread.currentThread().getId() + " - Completed fetching chainaddresses: " + fetchedEvents.size());
                // I need to filter by topics, cause here we have chainaddresses events for RSK and other chains that has different params in the event
                fetchedEvents.stream().filter(item -> item.getTopicId() == -2).forEach(item -> {
                    // RSK AddrChanged event
                    String nodehash = Numeric.toHexString((byte[]) item.getValues().get(0).getValue());
                    String eventName = "AddrChanged";
                    String address = item.getValues().get(1).getValue().toString();
                    ChainAddressEvent rskChain = new ChainAddressEvent(nodehash, eventName, RSK_SLIP_ADDRESS, address);
                    chainsEvents.add(rskChain);
                });
                fetchedEvents.stream().filter(item -> item.getTopicId() == -3).forEach(item -> {
                    // ChainAddrChanged event
                    String nodehash = Numeric.toHexString((byte[]) item.getValues().get(0).getValue());
                    String chain = Numeric.toHexString((byte[]) item.getValues().get(1).getValue());
                    String address = item.getValues().get(2).getValue().toString();
                    String eventName = "ChainAddrChanged";
                    ChainAddressEvent chainAddr = new ChainAddressEvent(nodehash, eventName, chain, address);
                    chainsEvents.add(chainAddr);
                });

                if (!chainsEvents.isEmpty()) {
                    dbManagerFacade.saveChainAddressesEvents(chainsEvents);
                }
            });
        });
        dbManagerFacade.saveLastBlockChainAdddresses(to);
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
                if(fetchedEvent.getValues().get(2).getValue().equals("0x2f548d54b2f32498638759caee36e6a51fac2ae8"))
                    logger.info(Thread.currentThread().getId() + " - Debug -");
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
                                if (rawEvts.stream().noneMatch(raw -> raw.getBlock().equals(newItem.getBlock()) && raw.getIdTopic() == tp.getId() && raw.getData().equals(newItem.getData())))
                                    rawEvts.add(newItem);
                            } else {
                                rawEvts.add(newItem);
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

    /**
     * Gets all topics for active subscriptions an creates a List of listeneables for the library web3
     * It performs checks if the topic was not already added to the list
     * @return Returns a List<EthereumBasedListeneable> to listen to the blockchain events
     * @throws ClassNotFoundException When trying to parse the web3-type creating the listeneable for contract events
     */
    public List<EthereumBasedListenable> getListeneables() throws ClassNotFoundException {
        List<EthereumBasedListenable> ethereumBasedListenables = new ArrayList<>();
        List<Subscription> activeSubs = dbManagerFacade.getAllActiveSubscriptionsWithBalance();
        for(Subscription sub : activeSubs){
            Set<Topic> subTopics = sub.getTopics();
            for(Topic tp : subTopics){
                EthereumBasedListenable newListeneable = Datafetcher.getEthereumBasedListenableFromTopic(tp);
                if(newListeneable != null) {
                    //Performing some checks to not insert when its already in the list
                    if (newListeneable.getKind().equals(EthereumBasedListenableTypes.CONTRACT_EVENT)) {
                        if (ethereumBasedListenables.stream().noneMatch(item ->
                                item.getKind().equals(EthereumBasedListenableTypes.CONTRACT_EVENT)
                                        && item.getAddress().equals(newListeneable.getAddress())
                                        && item.getEventName().equals(newListeneable.getEventName())))
                            ethereumBasedListenables.add(newListeneable);
                    } else if (newListeneable.getKind().equals(EthereumBasedListenableTypes.NEW_TRANSACTIONS)) {
                        if (ethereumBasedListenables.stream().noneMatch(item ->
                                item.getKind().equals(EthereumBasedListenableTypes.NEW_TRANSACTIONS)))
                            ethereumBasedListenables.add(newListeneable);
                    } else {
                        if (ethereumBasedListenables.stream().noneMatch(item ->
                                item.getKind().equals(EthereumBasedListenableTypes.NEW_BLOCK)))
                            ethereumBasedListenables.add(newListeneable);
                    }
                }
            }
        }
        return ethereumBasedListenables;
    }

    /**
     * Creates a EthereumBasedListeneable to get all tokens registered in the blockchain and returns that object
     * @return EthereumBasedListenable to get tokens
     */
    private EthereumBasedListenable getTokensNetwork(){
        return new EthereumBasedListenable(tokenNetworkRegistry, EthereumBasedListenableTypes.CONTRACT_EVENT, Arrays.asList(
                new TypeReference<Address>(true) {},
                new TypeReference<Address>(true) {}
        ), "TokenNetworkCreated", -1);
    }

    /**
     * Creates a EthereumBasedListeneable to get all events AddrChanged
     * @return EthereumBasedListenable to get event AddrChanged
     */
    private EthereumBasedListenable getAddrChanged(){
        return new EthereumBasedListenable(multiChainContract, EthereumBasedListenableTypes.CONTRACT_EVENT, Arrays.asList(
                new TypeReference<Bytes32>(true) {},
                new TypeReference<Address>(false) {}
        ), "AddrChanged", -2);
    }

    /**
     * Creates a EthereumBasedListeneable to get all events chainAddrChanged
     * @return EthereumBasedListenable to get event chainAddrChanged
     */
    private EthereumBasedListenable getChainAddrChanged(){
        return new EthereumBasedListenable(multiChainContract, EthereumBasedListenableTypes.CONTRACT_EVENT, Arrays.asList(
                new TypeReference<Bytes32>(true) {},
                new TypeReference<Bytes4>(false) {},
                new TypeReference<Utf8String>(false) {}
        ), "ChainAddrChanged", -3);
    }
}
