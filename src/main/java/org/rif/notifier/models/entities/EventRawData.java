package org.rif.notifier.models.entities;

import org.rif.notifier.util.JsonUtil;

import java.util.ArrayList;
import java.util.HashMap;
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
        HashMap<String, Object> map = new HashMap<>(5);
        map.put("topicId", topicId);
        map.put("value", values);
        map.put("blockNumber", blockNumber);
        map.put("eventName", eventName);
        map.put("contractAddress", contractAddress);
        return JsonUtil.writeValueAsString(map);
    }
}

