package org.rif.notifier.models.entities;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "subscription_type")
public class SubscriptionType {
    @Id
    int id;

    @OneToMany(mappedBy = "type")
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<Subscription> subscription;

    private int notifications;

    public SubscriptionType(){}

    public SubscriptionType(int notifications){
        this.notifications = notifications;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNotifications() {
        return notifications;
    }

    public void setNotifications(int notifications) {
        this.notifications = notifications;
    }

    public List<Subscription> getSubscription() {
        return subscription;
    }

    public void setSubscription(List<Subscription> subscription) {
        this.subscription = subscription;
    }

    @Override
    public String toString() {
        return "{" +
                "\"id\": " + id +
                ", \"notifications\": " + notifications +
                '}';
    }
}
