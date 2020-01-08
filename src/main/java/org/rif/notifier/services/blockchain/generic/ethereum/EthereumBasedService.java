package org.rif.notifier.services.blockchain.generic.ethereum;

import org.rif.notifier.datafetcher.BlockEthereumBasedDataFetcher;
import org.rif.notifier.datafetcher.ContractEventEthereumBasedDataFetcher;
import org.rif.notifier.datafetcher.TransactionEthereumBasedDataFetcher;
import org.rif.notifier.models.datafetching.FetchedBlock;
import org.rif.notifier.models.datafetching.FetchedEvent;
import org.rif.notifier.models.datafetching.FetchedTransaction;
import org.rif.notifier.models.listenable.EthereumBasedListenable;
import org.rif.notifier.services.blockchain.generic.BlockchainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public abstract class EthereumBasedService extends BlockchainService<EthereumBasedListenable> {

    protected String blockchainEndpoint;

    protected Web3j web3j;


    @Autowired
    private ContractEventEthereumBasedDataFetcher contractEventDataFetcher;
    @Autowired
    private BlockEthereumBasedDataFetcher blockDataFetcher;
    @Autowired
    private TransactionEthereumBasedDataFetcher transactionDataFetcher;

    @PostConstruct
    public abstract void buildWeb3();


    @Override
    public CompletableFuture<List<FetchedTransaction>>getTransactions(EthereumBasedListenable listenable, BigInteger from, BigInteger to) throws ExecutionException, InterruptedException {
         return transactionDataFetcher.fetch(listenable,from, to, web3j);
    }

    @Override
    public CompletableFuture<List<FetchedBlock>> getBlocks(EthereumBasedListenable listenable, BigInteger from, BigInteger to) {
        return blockDataFetcher.fetch(listenable, from, to, web3j);
    }

    @Override
    public CompletableFuture<List<FetchedEvent>> getContractEvents(EthereumBasedListenable listenable, BigInteger from, BigInteger to) throws ExecutionException, InterruptedException {
        return contractEventDataFetcher.fetch(listenable, from, to, web3j);
    }

    @Override
    public BigInteger getLastBlock() throws IOException {

        return web3j
                .ethGetBlockByNumber(DefaultBlockParameterName.LATEST, false)
                .send()
                .getBlock()
                .getNumber();

    }
}
