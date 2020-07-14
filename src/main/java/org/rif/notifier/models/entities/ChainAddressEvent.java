package org.rif.notifier.models.entities;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import java.math.BigInteger;

@Entity
@Table(name = "chainaddress_event")
public class ChainAddressEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String nodehash;

    @Column(name = "event_name")
    private String eventName;

    private String chain;

    private String address;

    @Column(name = "hashcode")
    private int rowhashcode;

    private BigInteger block;

    public ChainAddressEvent() {}

    public ChainAddressEvent(String nodehash, String eventName, String chain, String address, BigInteger block) {
        this.nodehash = nodehash;
        this.eventName = eventName;
        this.chain = chain;
        this.address = address;
        this.block = block;
    }

    public ChainAddressEvent(String nodehash, String eventName, String chain, String address, int rowhashcode, BigInteger block) {
        this.nodehash = nodehash;
        this.eventName = eventName;
        this.chain = chain;
        this.address = address;
        this.rowhashcode = rowhashcode;
        this.block = block;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNodehash() {
        return nodehash;
    }

    public void setNodehash(String nodehash) {
        this.nodehash = nodehash;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getChain() {
        return chain;
    }

    public void setChain(String chain) {
        this.chain = chain;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getRowhashcode() {
        return rowhashcode;
    }

    public void setRowhashcode(int rowhashcode) {
        this.rowhashcode = rowhashcode;
    }

    public BigInteger getBlock() {
        return block;
    }

    public void setBlock(BigInteger block) {
        this.block = block;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(nodehash)
                .append(eventName)
                .append(chain)
                .append(address)
                .append(block)
                .toHashCode();
    }
}
