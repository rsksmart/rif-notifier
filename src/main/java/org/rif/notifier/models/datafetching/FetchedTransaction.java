package org.rif.notifier.models.datafetching;

import org.rif.notifier.util.JsonUtil;
import org.web3j.protocol.core.methods.response.Transaction;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Optional;

public class FetchedTransaction extends FetchedData {

    private Transaction transaction;

    public FetchedTransaction(Transaction transaction, int topicId) {
        super(topicId);
        this.transaction = transaction;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    @Override
    public BigInteger getBlockNumber() {
        return Optional.ofNullable(getTransaction()).orElse(null).getBlockNumber();
    }

    @Override
    public String toString() {
        HashMap<String, Object> map = new HashMap<>(2);
        HashMap<String, Object> transactionMap = new HashMap<>(13);
        transactionMap.put("hash", transaction.getHash());
        transactionMap.put("nonce", transaction.getNonce());
        transactionMap.put("blockHash", transaction.getBlockHash());
        transactionMap.put("blockNumber", transaction.getBlockNumber());
        transactionMap.put("transactionIndex", transaction.getTransactionIndex());
        transactionMap.put("from", transaction.getFrom());
        transactionMap.put("to", transaction.getTo());
        transactionMap.put("value", transaction.getValue());
        transactionMap.put("gasPrice", transaction.getGasPrice());
        transactionMap.put("gas", transaction.getGas());
        transactionMap.put("r", transaction.getR());
        transactionMap.put("s", transaction.getS());
        transactionMap.put("v", transaction.getV());
        map.put("transaction", transactionMap);
        map.put("topicId ", super.getTopicId());
        return JsonUtil.writeValueAsString(map);
    }
}
