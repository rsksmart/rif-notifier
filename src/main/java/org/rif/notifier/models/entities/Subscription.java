package org.rif.notifier.models.entities;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.util.*;

@Entity
public class Subscription {
    @Id
    @GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    private int id;

    @Column(name = "active_since")
    private Date activeSince;

    private boolean active = true;

    @Column(name = "user_address")
    private String userAddress;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "type")
    private SubscriptionType type;

    private String state;

    //Setted to EAGER, at the start of DataFetchingJob we iterate through Topics, and if it's lazy, it throws errors
    @ManyToMany(mappedBy = "subscriptions", fetch=FetchType.EAGER)
    private Set<Topic> topics = new HashSet<>();

    @OneToMany(mappedBy = "subscription")
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<NotificationPreference> notificationPreferences ;

    @Column(name = "notification_balance")
    private int notificationBalance;

    public Subscription() {}

    public Subscription(Date activeSince, String userAddress, SubscriptionType type, String state) {
        this.activeSince = activeSince;
        this.userAddress = userAddress;
        this.type = type;
        this.state = state;
        this.notificationBalance = type.getNotifications();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getActiveSince() {
        return activeSince;
    }

    public void setActiveSince(Date activeSince) {
        this.activeSince = activeSince;
    }

    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    public SubscriptionType getType() {
        return type;
    }

    public void setType(SubscriptionType type) {
        this.type = type;
    }

    public List<NotificationPreference> getNotificationPreferences() {
        return notificationPreferences;
    }

    public void setNotificationPreferences(List<NotificationPreference> notificationPreferences) {
        this.notificationPreferences = notificationPreferences;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Set<Topic> getTopics() {
        return topics;
    }

    public void setTopics(Set<Topic> topics) {
        this.topics = topics;
    }

    public int getNotificationBalance() {
        return notificationBalance;
    }

    public void setNotificationBalance(int notificationCounter) {
        this.notificationBalance = notificationCounter;
    }

    public void decrementNotificationBalance() {
        this.notificationBalance--;
    }

    @Override
    public String toString() {
        return "{" +
                "\"id\":" + id +
                ", \"activeSince\":" + activeSince +
                ", \"active\":" + active +
                ", \"userAddress\":'" + userAddress + '\'' +
                ", \"type\":" + type.toString() +
                ", \"state\":'" + state + '\'' +
                ", \"notificationPreferences\":" + notificationPreferences +
                ", \"notificationBalance\":" + notificationBalance +
                '}';
    }

    public String toStringInfo() {
        StringBuilder tps = new StringBuilder("[");
        int counter = 1;
        for(Topic tp : topics){
            tps.append(tp.toStringInfo());
            if(counter < topics.size())
                tps.append(",");
            counter++;
        }
        tps.append("]");
        return "{" +
                "\"id\":" + id +
                ", \"activeSince\":\"" + activeSince + "\"" +
                ", \"active\":" + active +
                ", \"userAddress\":\"" + userAddress + '\"' +
                ", \"type\":" + type +
                ", \"state\":\"" + state + '\"' +
                ", \"topics\":" + tps +
                ", \"notificationPreferences\":" + notificationPreferences +
                ", \"notificationBalance\":" + notificationBalance +
                '}';
    }
}
