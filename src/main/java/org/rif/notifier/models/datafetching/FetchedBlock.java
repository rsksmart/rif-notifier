package org.rif.notifier.models.datafetching;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.rif.notifier.util.JsonUtil;
import org.web3j.protocol.core.methods.response.EthBlock.Block;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

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
    public BigInteger getBlockNumber() {
        return Optional.ofNullable(block).orElse(null).getNumber();
    }

    @Override
    public String toString() {
        ArrayList<Object> transactions = new ArrayList<>();
        if(block.getTransactions() != null && block.getTransactions().size() > 0){
            block.getTransactions().forEach(transactionResult -> {
                transactions.add(new ImmutablePair<String, String>("hash",transactionResult.get().toString()));
            });
        }
        BigInteger nonce = new BigInteger("0");
        try {
            nonce = block.getNonce();
        }catch (Exception ignored){
        }
        HashMap<String, Object> map = new HashMap<>(3);
        HashMap<String, Object> blockMap = new HashMap<>(13);
        blockMap.put("number", block.getNumber());
        blockMap.put("hash" , block.getHash());
        blockMap.put("parentHash" , block.getParentHash());
        blockMap.put("nonce", nonce.toString());
        blockMap.put("sha3Uncles" , block.getSha3Uncles());
        blockMap.put("transactionsRoot" , block.getTransactionsRoot());
        blockMap.put("stateRoot", block.getStateRoot());
        blockMap.put("receiptsRoot" , block.getReceiptsRoot());
        blockMap.put("miner" , block.getMiner());
        blockMap.put("mixHash" , block.getMixHash());
        blockMap.put("difficulty", block.getDifficulty());
        blockMap.put("totalDificulty", block.getTotalDifficulty());
        blockMap.put("extraData" , block.getExtraData());
        blockMap.put("size", block.getSize());
        blockMap.put("gasLimit", block.getGasLimit());
        blockMap.put("gasUsed", block.getGasUsed());
        blockMap.put("timestamp", block.getTimestamp());
        blockMap.put("transactions", transactions);
        map.put("block", blockMap);
        map.put("topicId", super.getTopicId());
        return JsonUtil.writeValueAsString(map);
    }
}
