package org.rif.notifier.scheduled;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.rif.notifier.exception.RSKBlockChainException;
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
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.CompletableFuture;



@Component
@ConfigurationProperties(prefix="notifier.blocks")
public class DataFetchingJob {

    private static final BigInteger DEFAULT_CONFIRMATION_COUNT = BigInteger.valueOf(20);

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

    private boolean startFromRSKLastBlock = false;
    private BigInteger blockConfirmationCount = DEFAULT_CONFIRMATION_COUNT;

    private boolean fetchedTokens = false;

    /**
     * Creates listeneables, then try to get the blockchain events related.
     * When all the events are fetched try to process the rawdata getted calling methods
     */
    @Scheduled(fixedDelayString = "${notifier.run.fixedDelayFetchingJob}", initialDelayString = "${notifier.run.fixedInitialDelayFetchingJob}")
        public void run() throws RSKBlockChainException {
            // Get latest block for this run
            BigInteger rskLastBlock = rskBlockchainService.getLastConfirmedBlock(blockConfirmationCount);
            BigInteger dbLastBlock = dbManagerFacade.getLastBlock();
            //fetch upto the last block
            BigInteger to = rskLastBlock;
            BigInteger from;
            BigInteger fromChainAddresses;
            if (startFromRSKLastBlock) {
                //if the rsk last block already fetched in db, don't fetch again - increment to avoid getting fetched again, fetch always for rskblasblock 0
                from = rskLastBlock.equals(dbLastBlock) && !rskLastBlock.equals(BigInteger.ZERO)? rskLastBlock.add(BigInteger.ONE) : rskLastBlock;
                //Saving lastblock so it starts fetching from here
                dbManagerFacade.saveLastBlock(rskLastBlock);
            startFromRSKLastBlock = false;
        } else {
            from = dbLastBlock.add(BigInteger.ONE);
        }
        BigInteger finalFrom = from;
        //ensure fetching 1 or more blocks, if from and to are same 1 block is fetched, and if to is greater 1 or more blocks are fetched
        if(from.compareTo(to) <= 0) {
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
                    // throw exception so it can be reprocessed if necessary the data has to be fetched again for all transaction types
                    throw new RSKBlockChainException("Error while getting logs from blockchain - " + e.getMessage(), e);
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
