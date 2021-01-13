package org.rif.notifier.models.entities;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import java.math.BigInteger;

@Entity
@Table(name = "SUBSCRIPTION_PRICE")
public class SubscriptionPrice {
    @Id
    @GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    private int id;

    @Column(name="price")
    private BigInteger price;

    @Column(name="currency")
    private String currency;

    @ManyToOne
    @JoinColumn(name="subscription_plan_id")
    private SubscriptionPlan subscriptionPlan;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public SubscriptionPlan getSubscriptionPlan() {
        return subscriptionPlan;
    }

    public void setSubscriptionPlan(SubscriptionPlan subscriptionPlan) {
        this.subscriptionPlan = subscriptionPlan;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        SubscriptionPrice that = (SubscriptionPrice) o;

        return new EqualsBuilder()
                .append(id, that.id)
                .append(price, that.price)
                .append(currency, that.currency)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(price)
                .append(currency)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "{" +
                "\"id\":" + id+
                ",\"price\":" + price+
                ",\"currency\":" + "\""+currency+"\""+
                ",\"subscriptionPlan\":" + subscriptionPlan+
                '}';
    }
}
