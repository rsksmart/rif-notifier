package org.rif.notifier.services.blockchain.generic;

import org.rif.notifier.models.datafetching.FetchedBlock;
import org.rif.notifier.models.datafetching.FetchedEvent;
import org.rif.notifier.models.datafetching.FetchedTransaction;
import org.rif.notifier.models.listenable.Listenable;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Component
public abstract class BlockchainService<T extends Listenable> {
    public abstract CompletableFuture<List<FetchedTransaction>> getTransactions (T listenable, BigInteger from, BigInteger to) throws ExecutionException, InterruptedException;
    public abstract CompletableFuture<List<FetchedBlock>> getBlocks (T listenable, BigInteger from, BigInteger to);
    public abstract CompletableFuture<List<FetchedEvent>> getContractEvents (T listenable, BigInteger from, BigInteger to) throws ExecutionException, InterruptedException;


    public abstract BigInteger getLastBlock () throws IOException;



}
