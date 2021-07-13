package org.rif.notifier.models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

@Entity
public class Notification implements Serializable  {
    @Id
    @GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name="subscription_id")
    private Subscription subscription;

    private String timestamp;

    private boolean sent;

    private String data;

    @Column(name = "id_topic")
    private int idTopic;

    @JsonIgnore
    @OneToMany(fetch=FetchType.EAGER, mappedBy="notification", cascade=CascadeType.ALL)
    private List<NotificationLog> notificationLogs;

    public Subscription getSubscription()   {
        return subscription;
    }

    public void setSubscription(Subscription sub)   {
        this.subscription = sub;
    }

    public Notification(){}

    public Notification(Subscription subscription, String timestamp, boolean sent, String data, int idTopic) {
        this.subscription = subscription;
        this.timestamp = timestamp;
        this.sent = sent;
        this.data = data;
        this.idTopic = idTopic;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getIdTopic() {
        return idTopic;
    }

    public void setIdTopic(int idTopic) {
        this.idTopic = idTopic;
    }

    public List<NotificationLog> getNotificationLogs() {
        return notificationLogs;
    }

    public void setNotificationLogs(List<NotificationLog> notificationLogs) {
        this.notificationLogs = notificationLogs;
    }

    public boolean areAllNotificationLogsSent()    {
       return notificationLogs.stream().allMatch(log->log.isSent()) ;
    }
}
