package org.rif.notifier.scheduled;

import org.rif.notifier.exception.RSKBlockChainException;
import org.rif.notifier.managers.DbManagerFacade;
import org.rif.notifier.models.datafetching.FetchedEvent;
import org.rif.notifier.models.listenable.EthereumBasedListenable;
import org.rif.notifier.services.blockchain.generic.rootstock.RskBlockchainService;
import org.rif.notifier.services.blockchain.payment.RskPaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;


@Component
@ConfigurationProperties(prefix="notifier.blocks")
public class PaymentProcessingJob {

    private static final BigInteger DEFAULT_CONFIRMATION_COUNT = BigInteger.valueOf(20);

    private static final Logger logger = LoggerFactory.getLogger(PaymentProcessingJob.class);

    private RskBlockchainService rskBlockchainService;
    private DbManagerFacade dbManagerFacade;
    private RskPaymentService rskPaymentService;

    private boolean startFromRSKLastBlock = false;
    private BigInteger blockConfirmationCount = DEFAULT_CONFIRMATION_COUNT;

    public PaymentProcessingJob(RskBlockchainService rskBlockchainService, DbManagerFacade dbManagerFacade, RskPaymentService rskPaymentService) {
       this.rskBlockchainService = rskBlockchainService;
       this.dbManagerFacade = dbManagerFacade;
       this.rskPaymentService = rskPaymentService;
    }

    /**
     * Creates listeneables, then try to get the blockchain events related.
     * When all the events are fetched try to process the rawdata getted calling methods
     */
    @Scheduled(fixedDelayString = "${notifier.run.fixedDelayFetchingJob}", initialDelayString = "${notifier.run.fixedInitialDelayFetchingJob}")
    public void run() throws RSKBlockChainException {
        // Get latest block for this run
        BigInteger rskLastBlock = rskBlockchainService.getLastConfirmedBlock(blockConfirmationCount);
        BigInteger dbLastBlock = dbManagerFacade.getLastBlockForPayment();
        //fetch upto the last block
        BigInteger to = rskLastBlock;
        BigInteger from;
        BigInteger fromChainAddresses;
        if (startFromRSKLastBlock) {
            //if the rsk last block already fetched in db, don't fetch again - increment to avoid getting fetched again, fetch always for rskblasblock 0
            from = rskLastBlock.equals(dbLastBlock) && !rskLastBlock.equals(BigInteger.ZERO)? rskLastBlock.add(BigInteger.ONE) : rskLastBlock;
            //Saving lastblock so it starts fetching from here
            dbManagerFacade.saveLastBlockPayment(rskLastBlock);
            startFromRSKLastBlock = false;
        } else {
            from = dbLastBlock.add(BigInteger.ONE);
        }
        BigInteger finalFrom = from;
        //ensure fetching 1 or more blocks, if from and to are same 1 block is fetched, and if to is greater 1 or more blocks are fetched
        if(from.compareTo(to) <= 0) {
            //Fetching
            logger.info(Thread.currentThread().getId() + String.format(" - Starting fetching for payment from %s to %s", from, to));

            long start = System.currentTimeMillis();
            List<CompletableFuture<List<FetchedEvent>>> eventTasks = new ArrayList<>();
            List<EthereumBasedListenable> ethereumBasedListenables = rskPaymentService.getPaymentListenables();

            for (EthereumBasedListenable subscriptionChannel : ethereumBasedListenables) {
                try {
                    eventTasks.add(rskBlockchainService.getContractEvents(subscriptionChannel, from, to));
                }
                catch (Exception e) {
                    logger.error("Error during PaymentFetching job: ", e);
                    // throw exception so it can be reprocessed if necessary the data has to be fetched again for all transaction types
                    throw new RSKBlockChainException("Error while getting logs from blockchain - " + e.getMessage(), e);
                }
            }
            dbManagerFacade.saveLastBlockPayment(to);
            rskPaymentService.processEventTasks(eventTasks, start, dbLastBlock);
        }else{
            logger.info(Thread.currentThread().getId() + " - Nothing to fetch yet for payment -");
        }
    }

    public void setStartFromRSKLastBlock(boolean startFromRSKLastBlock) {
        this.startFromRSKLastBlock = startFromRSKLastBlock;
    }

    public void setBlockConfirmationCount(BigInteger blockConfirmationCount) {
        this.blockConfirmationCount = blockConfirmationCount;
    }
}
