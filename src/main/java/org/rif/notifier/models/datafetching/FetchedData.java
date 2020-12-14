package org.rif.notifier.models.datafetching;

import java.math.BigInteger;

public abstract class FetchedData {
    private int topicId;

    public FetchedData(){}
    public FetchedData(int topicId){
        this.topicId = topicId;
    }

    public int getTopicId() {
        return topicId;
    }

    public void setTopicId(int topicId) {
        this.topicId = topicId;
    }

    abstract public BigInteger getBlockNumber() ;
}
