package org.rif.notifier.models.datafetching;
import org.web3j.abi.datatypes.Type;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

public class FetchedEvent extends FetchedData {
    private List<Type> values;
    private BigInteger blockNumber;

    private String eventName;

    private String contractAddress;

    public FetchedEvent(){}

    public FetchedEvent(
            String eventName, List<Type> values, BigInteger blockNumber, String contractAddress, int topicId) {
        super(topicId);
        this.eventName = eventName;
        this.values = values;
        this.blockNumber = blockNumber;
        this.contractAddress = contractAddress;
    }

    public List<Type> getValues() {
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
    public String toString() {
        return "FetchedEvent{"
                + "values="
                + values.toString()
                + ", blockNumber="
                + blockNumber
                + ", eventName="
                + eventName
                + ", topicId="
                + super.getTopicId()
                + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FetchedEvent fetchedEvent = (FetchedEvent) o;
        // compares by channel id
        return Objects.equals(values.get(0), fetchedEvent.values.get(0))
                && Objects.equals(eventName, fetchedEvent.eventName)
                && Objects.equals(contractAddress, fetchedEvent.contractAddress);
    }

    @Override
    public int hashCode() {
        // channel id hash
        return Objects.hash(values.get(0), contractAddress, eventName) * 17;
    }
}
