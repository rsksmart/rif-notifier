package org.rif.notifier.models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigInteger;
import java.util.Date;

@Entity
public class SubscriptionPayment {
    @Id
    @GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    private int id;

    @Column(name="payment_reference")
    private String paymentReference;

    @Column(name="amount")
    private BigInteger amount;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="payment_date")
    private Date paymentDate;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name="subscription_id")
    private Subscription subscription;

    @ManyToOne
    @JoinColumn(name="currency_id")
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(name="status")
    private SubscriptionPaymentStatus status;

    public SubscriptionPayment()    {}

    public SubscriptionPayment(BigInteger amount, Subscription subscription, Currency currency, SubscriptionPaymentStatus status) {
        this.amount = amount;
        this.subscription = subscription;
        this.status = status;
        this.currency = currency;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public BigInteger getAmount() {
        return amount;
    }

    public void setAmount(BigInteger amount) {
        this.amount = amount;
    }

    public Date getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Date paymentDate) {
        this.paymentDate = paymentDate;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
    }

    public SubscriptionPaymentStatus getStatus() {
        return status;
    }

    public void setStatus(SubscriptionPaymentStatus status) {
        this.status = status;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    /**
     * A payment is refunded when the subscription currency and the payment currency matches, and the status is refunded.
     * This method does not count those refunds made in different currency as refund
     * @return
     */
    public boolean isRefunded() {
        return this.subscription.getCurrency().equals(this.currency) && status == SubscriptionPaymentStatus.REFUNDED;
    }

    /**
     * A payment is received when the subscription currency and the payment currency matches, and the status is received.
     * This method does not count those payments made in different currency as refund
     * @return
     */
    public boolean isReceived() {
        return this.subscription.getCurrency().equals(this.currency) && status == SubscriptionPaymentStatus.RECEIVED;
    }
}
