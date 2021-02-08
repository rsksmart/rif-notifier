package org.rif.notifier.models.entities;

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

    @Column(name="payment_date")
    private Date paymentDate;

    @ManyToOne
    @JoinColumn(name="subscription_id")
    private Subscription subscription;

    @Enumerated(EnumType.STRING)
    @Column(name="status")
    private SubscriptionPaymentStatus status;

    public SubscriptionPayment(BigInteger amount, Subscription subscription, SubscriptionPaymentStatus status) {
        this.amount = amount;
        this.subscription = subscription;
        this.status = status;
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

    public boolean isRefunded() {
        return status == SubscriptionPaymentStatus.REFUNDED;
    }

    public boolean isReceived() {
        return status == SubscriptionPaymentStatus.RECEIVED;
    }
}
