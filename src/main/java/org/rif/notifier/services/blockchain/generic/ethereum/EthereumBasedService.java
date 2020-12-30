package org.rif.notifier.services.blockchain.generic.ethereum;

import org.rif.notifier.datafetcher.BlockEthereumBasedDataFetcher;
import org.rif.notifier.datafetcher.ContractEventEthereumBasedDataFetcher;
import org.rif.notifier.datafetcher.TransactionEthereumBasedDataFetcher;
import org.rif.notifier.exception.RSKBlockChainException;
import org.rif.notifier.models.datafetching.FetchedBlock;
import org.rif.notifier.models.datafetching.FetchedEvent;
import org.rif.notifier.models.datafetching.FetchedTransaction;
import org.rif.notifier.models.listenable.EthereumBasedListenable;
import org.rif.notifier.services.blockchain.generic.BlockchainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.response.EthBlockNumber;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    public CompletableFuture<List<FetchedTransaction>>getTransactions(EthereumBasedListenable listenable, BigInteger from, BigInteger to) {
         return transactionDataFetcher.fetch(listenable,from, to, web3j);
    }

    @Override
    public CompletableFuture<List<FetchedBlock>> getBlocks(EthereumBasedListenable listenable, BigInteger from, BigInteger to) {
        return blockDataFetcher.fetch(listenable, from, to, web3j);
    }

    @Override
    public CompletableFuture<List<FetchedEvent>> getContractEvents(EthereumBasedListenable listenable, BigInteger from, BigInteger to) {
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

    /**
     * Returns the latest block minus the confirmations count
     * @param confirmations the number blocks backward to fetch from latest block
     * @return
     * @throws IOException
     */
    @Override
    public BigInteger getLastConfirmedBlock(BigInteger confirmations) throws RSKBlockChainException {
        BigInteger latest;
        try {
            latest = web3j.ethBlockNumber().sendAsync().get().getBlockNumber();
            return Optional.ofNullable(web3j
                .ethGetBlockByNumber(DefaultBlockParameter.valueOf(latest.subtract(confirmations)), false)
                .send()
                .getBlock()).orElseThrow(()->new RSKBlockChainException("Error fetching block with num confirmations " + confirmations, null)).getNumber();
        } catch(IOException |InterruptedException|ExecutionException e) {
            throw new RSKBlockChainException(e.getMessage(), e);
        }

    }
}
