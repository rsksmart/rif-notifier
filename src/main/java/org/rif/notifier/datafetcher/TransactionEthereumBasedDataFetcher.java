package org.rif.notifier.datafetcher;

import org.rif.notifier.models.datafetching.FetchedTransaction;
import org.rif.notifier.models.listenable.EthereumBasedListenable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.Transaction;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Service
public class TransactionEthereumBasedDataFetcher extends EthereumBasedDataFetcher {

    private static final Logger logger = LoggerFactory.getLogger(TransactionEthereumBasedDataFetcher.class);

    /**
     * Gets all transactions from a block number to another.
     * It returns a list of FetchedTransaction
     */
    @Async
    @Override
    public CompletableFuture<List<FetchedTransaction>> fetch(EthereumBasedListenable ethereumBasedListenable, BigInteger from, BigInteger to, Web3j web3j) {
        long start  = System.currentTimeMillis();
        logger.info(Thread.currentThread().getId() + " - Starting reading transactions for subscription: "+ ethereumBasedListenable);

        return CompletableFuture.supplyAsync(() -> {
            List<FetchedTransaction> transactions = new ArrayList<>();
           Iterable<Transaction> obs = web3j.replayPastTransactionsFlowable(DefaultBlockParameter.valueOf(from), DefaultBlockParameter.valueOf(to)).blockingLatest();
           for(Transaction t : obs){
               FetchedTransaction ft = new FetchedTransaction(t, ethereumBasedListenable.getTopicId());
               transactions.add(ft);
           }
           long end = System.currentTimeMillis();
           logger.info(Thread.currentThread().getId() + " - End Trasaction data fetching time = "+ (end-start));
           return transactions;

        }).exceptionally(throwable -> {
            logger.error(Thread.currentThread().getId() + " - Error fetching transaction data for subscription: "+ ethereumBasedListenable, throwable);
            throw throwable instanceof CompletionException ? (CompletionException)throwable : new CompletionException(throwable);
            //return new ArrayList<>();
        });
    }
}
