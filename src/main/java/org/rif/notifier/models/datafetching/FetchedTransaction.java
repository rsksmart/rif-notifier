package org.rif.notifier.models.datafetching;

import org.web3j.protocol.core.methods.response.Transaction;

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
    public String toString() {
        return "{" +
                "\"transaction\": {" +
                    "\"hash\": \"" + transaction.getHash() + "\"," +
                    "\"nonce\": " + transaction.getNonce() + "," +
                    "\"blockHash\": \"" + transaction.getBlockHash() + "\"," +
                    "\"blockNumber\": " + transaction.getBlockNumber() + "," +
                    "\"transactionIndex\": " + transaction.getTransactionIndex() + "," +
                    "\"from\": \"" + transaction.getFrom() + "\"," +
                    "\"to\": \"" + transaction.getTo() + "\"," +
                    "\"value\": " + transaction.getValue() + "," +
                    "\"gasPrice\": " + transaction.getGasPrice() + "," +
                    "\"gas\": " + transaction.getGas() + "," +
                    "\"r\": \"" + transaction.getR() + "\"," +
                    "\"s\": \"" + transaction.getS() + "\"," +
                    "\"v\": " + transaction.getV() +
                "}," +
                "\"topicId\": " + super.getTopicId() +
                '}';
    }
}
