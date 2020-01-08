package org.rif.notifier.models.dataprocessor;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

public class DataRawProcessed {
    private List<Map<String, String>> values;
    private BigInteger blockNumber;

    private String eventName;

    private String contractAddress;
    public DataRawProcessed(){}
    public DataRawProcessed(
            String eventName, List<Map<String, String>> values, BigInteger blockNumber, String contractAddress) {
        this.eventName = eventName;
        this.values = values;
        this.blockNumber = blockNumber;
        this.contractAddress = contractAddress;
    }

    public List<Map<String, String>> getValues() {
        return values;
    }

    public BigInteger getBlockNumber() {
        return blockNumber;
    }

    public String getEventName() {
        return eventName;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    @Override
    public String toString(){
        return "";
    }
}
