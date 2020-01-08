package org.rif.notifier.datafetcher;

import org.rif.notifier.models.datafetching.FetchedBlock;
import org.rif.notifier.models.listenable.EthereumBasedListenable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthBlock;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class BlockEthereumBasedDataFetcher extends EthereumBasedDataFetcher {

    private static final Logger logger = LoggerFactory.getLogger(BlockEthereumBasedDataFetcher.class);

    /***
     * Generates a list of fetched blocks from the blockchain, using the getLogs method
     * @param ethereumBasedListenable
     * @param from Block number from, to fetch
     * @param to Block number to, to fetch
     * @param web3j Library
     * @return When finished it returns fetched blocks from the blockchain
     */
    @Async
    @Override
    public CompletableFuture<List<FetchedBlock>> fetch(EthereumBasedListenable ethereumBasedListenable, BigInteger from, BigInteger to, Web3j web3j) {
        long start = System.currentTimeMillis();
        logger.info(Thread.currentThread().getId() + " - Starting block events for subscription: " + ethereumBasedListenable);
        return CompletableFuture.supplyAsync(() -> {
            List<FetchedBlock> blocks = new ArrayList<>();
            try {
                for(BigInteger i = from; i.compareTo(to) <= 0; i=i.add(BigInteger.ONE)){
                    blocks.add(new FetchedBlock(web3j.ethGetBlockByNumber(DefaultBlockParameter.valueOf(i), false).send().getBlock(), ethereumBasedListenable.getTopicId()));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            long end = System.currentTimeMillis();
            logger.info(Thread.currentThread().getId() + " - End block events for subscription = " + (end - start));
            return blocks;
        }).exceptionally(throwable -> {
            logger.error(Thread.currentThread().getId() + " - Error fetching blocks data for subscription: "+ ethereumBasedListenable, throwable);
            return new ArrayList<>();
        });
    }


}
