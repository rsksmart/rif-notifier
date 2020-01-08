package org.rif.notifier.models.entities;

import java.util.ArrayList;
import java.util.List;

public class EventRawData {
    private int topicId;
    private List<EventRawDataParam> values = new ArrayList<>();
    private int blockNumber;
    private String eventName;
    private String contractAddress;
    public EventRawData(){}

    public int getTopicId() {
        return topicId;
    }

    public void setTopicId(int topicId) {
        this.topicId = topicId;
    }

    public List<EventRawDataParam> getValues() {
        return values;
    }

    public void setValues(List<EventRawDataParam> values) {
        this.values = values;
    }

    public int getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(int blockNumber) {
        this.blockNumber = blockNumber;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    @Override
    public String toString() {
        return "{" +
                "\"topicId\":" + topicId +
                ",\"values\":" + values +
                ",\"blockNumber\":" + blockNumber +
                ",\"eventName\":\"" + eventName + "\"" +
                ",\"contractAddress\":\"" + contractAddress + "\"" +
                '}';
    }
}

