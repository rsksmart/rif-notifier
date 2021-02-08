package org.rif.notifier.models.entities;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.*;

@Entity
public class Subscription implements Serializable {
    @Id
    @GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    private int id;

    @Column(name = "active_since")
    private Date activeSince;

    @Enumerated(EnumType.STRING)
    @Column(name="status")
    private SubscriptionStatus status;

    @ManyToOne
    @JoinColumn(name="subscription_plan_id")
    private SubscriptionPlan subscriptionPlan;

    @Column(name="price")
    private BigInteger price;

    @Column(name="currency")
    private String currency;

    @OneToOne
    @JoinColumn(name="previous_subscription_id")
    private Subscription previousSubscription;

    @Column(name="hash")
    private String hash;

    @Column(name="expiration_date")
    private Date expirationDate;

    @UpdateTimestamp
    @Column(name="last_updated")
    private Timestamp last_updated;

    @Column(name = "user_address")
    private String userAddress;

    //Setted to EAGER, at the start of DataFetchingJob we iterate through Topics, and if it's lazy, it throws errors
    @ManyToMany(mappedBy = "subscriptions", fetch=FetchType.EAGER)
    private Set<Topic> topics = new HashSet<>();

    @OneToMany(mappedBy = "subscription")
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<NotificationPreference> notificationPreferences ;

    @Column(name = "notification_balance")
    private int notificationBalance;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(mappedBy="subscription", cascade = CascadeType.ALL)
    private List<SubscriptionPayment> subscriptionPayments;

    public Subscription() {}

    public Subscription(Date activeSince, String userAddress, SubscriptionPlan subscriptionPlan, SubscriptionStatus status) {
        this.activeSince = activeSince;
        this.userAddress = userAddress;
        this.status = status;
        this.notificationBalance = subscriptionPlan.getNotificationQuantity();
        this.subscriptionPlan = subscriptionPlan;
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

    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public void setStatus(SubscriptionStatus status) {
        this.status = status;
    }

    public SubscriptionPlan getSubscriptionPlan() {
        return subscriptionPlan;
    }

    public void setSubscriptionPlan(SubscriptionPlan subscriptionPlan) {
        this.subscriptionPlan = subscriptionPlan;
    }

    public BigInteger getPrice() {
        return price;
    }

    public void setPrice(BigInteger price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Subscription getPreviousSubscription() {
        return previousSubscription;
    }

    public void setPreviousSubscription(Subscription previousSubscription) {
        this.previousSubscription = previousSubscription;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Timestamp getLast_updated() {
        return last_updated;
    }

    public void setLast_updated(Timestamp last_updated) {
        this.last_updated = last_updated;
    }

    public List<NotificationPreference> getNotificationPreferences() {
        return notificationPreferences;
    }

    public void setNotificationPreferences(List<NotificationPreference> notificationPreferences) {
        this.notificationPreferences = notificationPreferences;
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

    public boolean isActive()  {
        return this.status == SubscriptionStatus.ACTIVE;
    }

    public boolean isPending()  {
        return this.status == SubscriptionStatus.PENDING;
    }

    /**
     * A subscription is renewable if it's previous subscription is in complete or expired state
     * @return
     */
    public boolean canActivate()    {
        return previousSubscription == null || !(previousSubscription.isActive() || previousSubscription.isPending());
    }

    /**
     * Checks if the total of amount received minus the total of amount refunded is not less than the subscription price.
     * @return true if the difference of amount received and refunded is not less than the subscription price
     */
    public boolean isPaid() {
        if (!CollectionUtils.isEmpty(subscriptionPayments)) {
            Optional<BigInteger> receivedTotal = subscriptionPayments.stream().filter(p -> p.isReceived()).map(p -> p.getAmount()).reduce((p1, p2) -> p1.add(p2));
            Optional<BigInteger> refundedTotal = subscriptionPayments.stream().filter(p -> p.isRefunded()).map(p -> p.getAmount()).reduce((p1, p2) -> p1.add(p2));
            return receivedTotal.map(received -> {
                //if there are refunds, subtract the refund amount
                BigInteger paid = refundedTotal.map(refunded -> received.subtract(refunded)).orElse(received);
                //check total paid is not less than the subscription price
                return paid.compareTo(price) != -1;
            }).orElse(false);
        }
        //payment not received so return false
        return false;
    }

    public List<SubscriptionPayment> getSubscriptionPayments() {
        return subscriptionPayments;
    }

    public void setSubscriptionPayments(List<SubscriptionPayment> subscriptionPayments) {
        this.subscriptionPayments = subscriptionPayments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Subscription that = (Subscription) o;

        return new EqualsBuilder()
                .append(id, that.id)
                .append(status, that.status)
                .append(notificationBalance, that.notificationBalance)
                .append(activeSince, that.activeSince)
                .append(userAddress, that.userAddress)
                .append(subscriptionPlan, that.subscriptionPlan)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(activeSince)
                .append(status)
                .append(userAddress)
                .append(subscriptionPlan)
                .append(notificationBalance)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "{" +
                "\"id\":" + id +
                ", \"activeSince\":" + activeSince +
                ", \"status\":" + status +
                ", \"userAddress\":'" + userAddress + '\'' +
                ", \"type\":" + subscriptionPlan.toString() +
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
                ", \"status\":" + status+
                ", \"userAddress\":\"" + userAddress + '\"' +
                ", \"type\":" + subscriptionPlan+
                ", \"topics\":" + tps +
                ", \"notificationPreferences\":" + notificationPreferences +
                ", \"notificationBalance\":" + notificationBalance +
                '}';
    }
}
