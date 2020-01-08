package org.rif.notifier.models.entities;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Notification {
    @Id
    @GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    private int id;

    @Column(name = "to_address")
    private String toAddress;

    private String timestamp;

    private boolean sended;

    private String data;

    @Column(name = "id_topic")
    private int idTopic;

    public Notification(){}

    public Notification(String to_address, String timestamp, boolean sended, String data, int idTopic) {
        this.toAddress = to_address;
        this.timestamp = timestamp;
        this.sended = sended;
        this.data = data;
        this.idTopic = idTopic;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTo_address() {
        return toAddress;
    }

    public void setTo_address(String to_address) {
        this.toAddress = to_address;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isSended() {
        return sended;
    }

    public void setSended(boolean sended) {
        this.sended = sended;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getToAddress() {
        return toAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    public int getIdTopic() {
        return idTopic;
    }

    public void setIdTopic(int idTopic) {
        this.idTopic = idTopic;
    }
}
