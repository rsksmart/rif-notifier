package org.rif.notifier.scheduled;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.rif.notifier.helpers.DataFetcherHelper;
import org.rif.notifier.helpers.LuminoDataFetcherHelper;
import org.rif.notifier.managers.DbManagerFacade;
import org.rif.notifier.models.datafetching.FetchedData;
import org.rif.notifier.models.datafetching.FetchedEvent;
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

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

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

    @Autowired
    private DataFetcherHelper dataFetcherHelper;

    @Autowired
    private LuminoDataFetcherHelper luminoHelper;



    private boolean fetchedTokens = false;

    @Value("${notifier.blocks.startFromLast}")
    private boolean onInit;

    /**
     * Creates listeneables, then try to get the blockchain events related.
     * When all the events are fetched try to process the rawdata getted calling methods
     */
    @Scheduled(fixedDelayString = "${notifier.run.fixedDelayFetchingJob}", initialDelayString = "${notifier.run.fixedInitialDelayFetchingJob}")
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
        BigInteger finalFrom = from;
        if(from.compareTo(to) < 0) {
            //Fetching
            logger.info(Thread.currentThread().getId() + String.format(" - Starting fetching from %s to %s", from, to));

            long start = System.currentTimeMillis();
            List<CompletableFuture<? extends List<? extends FetchedData>>> blockTasks = new ArrayList<>();
            List<CompletableFuture<? extends List<? extends FetchedData>>> transactionTasks = new ArrayList<>();
            List<CompletableFuture<List<FetchedEvent>>> eventTasks = new ArrayList<>();
            List<EthereumBasedListenable> ethereumBasedListenables = dataFetcherHelper.getListenablesForTopicsWithActiveSubscription();
            if (fetchedTokens) {
                ethereumBasedListenables.add(luminoHelper.getTokensNetwork());
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



            dataFetcherHelper.processFetchedData(blockTasks, start, finalFrom, EthereumBasedListenableTypes.NEW_BLOCK);
            dataFetcherHelper.processFetchedData(transactionTasks, start, finalFrom,EthereumBasedListenableTypes.NEW_TRANSACTIONS);
            dataFetcherHelper.processEventTasks(eventTasks, start, finalFrom, fetchedTokens);

            ///////////////////////////////////Token Registry extract///////////////////////////////////////////
            if (!fetchedTokens) {
                fetchedTokens = luminoHelper.fetchTokens(start, to);
            }
        }else{
            logger.info(Thread.currentThread().getId() + " - Nothing to fetch yet -");
        }
    }



}
