package org.rif.notifier.models.listenable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.rif.notifier.models.entities.Topic;
import org.rif.notifier.util.JsonUtil;
import org.web3j.abi.TypeReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EthereumBasedListenable extends Listenable {

    private List<TypeReference<?>> eventFields;
    private String eventName;
    private EthereumBasedListenableTypes kind;
    private int topicId;

    public EthereumBasedListenable(String address, EthereumBasedListenableTypes kind, List<TypeReference<?>> eventFields, String eventName) {
       this(address, kind, eventFields, eventName, 0);
    }

    public EthereumBasedListenable(String address, EthereumBasedListenableTypes kind, List<TypeReference<?>> eventFields, String eventName, int topicId) {
        super(address);
        this.kind = kind;
        this.eventFields = eventFields;
        this.eventName = eventName;
        this.topicId = topicId;
    }

    public List<TypeReference<?>> getEventFields() {
        return eventFields;
    }



    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public EthereumBasedListenableTypes getKind() {
        return kind;
    }

    public void setKind(EthereumBasedListenableTypes kind) {
        this.kind = kind;
    }

    public void setEventFields(List<TypeReference<?>> eventFields) {
        this.eventFields = eventFields;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public int getTopicId() {
        return topicId;
    }

    public void setTopicId(int topicId) {
        this.topicId = topicId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        EthereumBasedListenable that = (EthereumBasedListenable) o;

        return new EqualsBuilder()
                .append(address, that.address)
                .append(eventName, that.eventName)
                .append(kind, that.kind)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(eventName)
                .append(kind)
                .append(address)
                .toHashCode();
    }

    @Override
    public String toString() {
        ArrayList<String> fields = new ArrayList<>();
        if(eventFields != null) {
            for (TypeReference tr : eventFields) {
                fields.add(tr.getType().getTypeName());
            }
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("eventFields", fields);
        map.put("eventName", eventName);
        map.put("kind", kind);
        map.put("address", address);
        map.put("topicId", topicId);
        return JsonUtil.writeValueAsString(map);
    }
}
