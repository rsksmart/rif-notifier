package org.rif.notifier.models.datafetching;
import org.web3j.protocol.core.methods.response.EthBlock.Block;

import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;

public class FetchedBlock extends FetchedData {
    private Block block;

    public FetchedBlock(Block block, int topicId) {
        super(topicId);
        this.block = block;
    }

    public Block getBlock() {
        return block;
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    @Override
    public String toString() {
        StringBuilder transactionJson = new StringBuilder("[");
        if(block.getTransactions() != null && block.getTransactions().size() > 0){
            AtomicInteger counter = new AtomicInteger(1);
            block.getTransactions().forEach(transactionResult -> {
                int index = Integer.parseInt(counter.toString())- 1;
                transactionJson.append("{ \"hash\": \"").append(transactionResult.get().toString()).append("\"}");
                if(counter.get() < block.getTransactions().size())
                    transactionJson.append(",");
                counter.getAndIncrement();
            });
        }
        transactionJson.append("]");
        BigInteger nonce = new BigInteger("0");
        try {
            nonce = block.getNonce();
        }catch (Exception ignored){

        }

        return "{" +
                "\"block\": {" +
                "\"number\": " + block.getNumber() + "," +
                "\"hash\": \"" + block.getHash() + "\"," +
                "\"parentHash\": \"" + block.getParentHash() + "\"," +
                "\"nonce\": " + nonce.toString() + "," +
                "\"sha3Uncles\": \"" + block.getSha3Uncles() + "\"," +
                "\"transactionsRoot\": \"" + block.getTransactionsRoot() + "\"," +
                "\"stateRoot\": \"" + block.getStateRoot() + "\"," +
                "\"receiptsRoot\": \"" + block.getReceiptsRoot() + "\"," +
                "\"miner\": \"" + block.getMiner() + "\"," +
                "\"mixHash\": \"" + block.getMixHash() + "\"," +
                "\"difficulty\": " + block.getDifficulty() + "," +
                "\"totalDificulty\": " + block.getTotalDifficulty() + "," +
                "\"extraData\": \"" + block.getExtraData() + "\"," +
                "\"size\": " + block.getSize() + "," +
                "\"gasLimit\": " + block.getGasLimit() + "," +
                "\"gasUsed\": " + block.getGasUsed() + "," +
                "\"timestamp\": " + block.getTimestamp() + "," +
                "\"transactions\": " + transactionJson +
                "}," +
                "\"topicId\": " + super.getTopicId() +
                '}';
    }
}
