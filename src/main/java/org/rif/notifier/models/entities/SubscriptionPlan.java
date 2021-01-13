package org.rif.notifier.models.entities;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import javax.validation.constraints.Min;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "SUBSCRIPTION_PLAN")
public class SubscriptionPlan {
    @Id
    @GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    private int id;

    @Column(name="name")
    private String name;

    @Column(name="validity")
    private int validity;

    @Column(name="notification_preferences")
    private String notificationPreferences;

    @Column(name="notification_amount")
    private int notificationAmount;

    @Column(name="status")
    private boolean status;

    @OneToMany(mappedBy = "subscriptionPlan", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<SubscriptionPrice> subscriptionPriceList;

    public SubscriptionPlan()   {

    }

    public SubscriptionPlan(String name, int validity, String notificationPreferences, int notificationAmount, boolean status, List<SubscriptionPrice> subscriptionPriceList) {
        this.name = name;
        this.validity = validity;
        this.notificationPreferences = notificationPreferences;
        this.notificationAmount = notificationAmount;
        this.status = status;
        this.subscriptionPriceList = subscriptionPriceList;
    }

    public SubscriptionPlan(int notificationAmount) {
        this.notificationAmount = notificationAmount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getValidity() {
        return validity;
    }

    public void setValidity(int validity) {
        this.validity = validity;
    }

    public Set getNotificationPreferences() {
        Set preferences  = new HashSet();
        Optional.ofNullable(this.notificationPreferences).ifPresent( p-> {
                    preferences.addAll(Arrays.asList(notificationPreferences.split(",")).stream().collect(Collectors.toSet()));
                });
        return preferences;
    }

    public void setNotificationPreferences(Set notificationPreferences) {
        List<String> preferences = new ArrayList<>();
        notificationPreferences.forEach(n->{
            preferences.add(n.toString());
        });
        this.notificationPreferences = String.join(",", preferences);
    }

    public int getNotificationAmount() {
        return notificationAmount;
    }

    public void setNotificationAmount(int notificationAmount) {
        this.notificationAmount = notificationAmount;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public List<SubscriptionPrice> getSubscriptionPriceList() {
        return subscriptionPriceList;
    }

    public void setSubscriptionPriceList(List<SubscriptionPrice> subscriptionPriceList) {
        this.subscriptionPriceList = subscriptionPriceList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        SubscriptionPlan that = (SubscriptionPlan) o;

        return new EqualsBuilder()
                .append(id, that.id)
                .append(validity, that.validity)
                .append(notificationAmount, that.notificationAmount)
                .append(status, that.status)
                .append(name, that.name)
                .append(notificationPreferences, that.notificationPreferences)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(name)
                .append(validity)
                .append(notificationPreferences)
                .append(notificationAmount)
                .append(status)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "{" +
                "\"id\":" + id+
                ",\"name\":" + "\""+name+"\""+
                ",\"validity\":" + validity+
                ",\"notificationAmount\":\"" + notificationAmount+ "\"" +
                ",\"status\":\"" + status+ "\"" +
                '}';
    }
}
