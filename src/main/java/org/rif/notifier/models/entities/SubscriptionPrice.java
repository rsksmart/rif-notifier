package org.rif.notifier.models.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.rif.notifier.models.serializer.BigIntegerSerializer;
import org.rif.notifier.util.JsonUtil;

import javax.persistence.*;
import java.math.BigInteger;
import java.util.HashMap;

@Entity
@Table(name = "SUBSCRIPTION_PRICE")
public class SubscriptionPrice {
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Id
    @GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    private int id;

    @Column(name="price")
    private BigInteger price;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name="currency_id")
    private Currency currency;

    @JsonProperty(access= JsonProperty.Access.WRITE_ONLY)
    @ManyToOne
    @JoinColumn(name="subscription_plan_id")
    private SubscriptionPlan subscriptionPlan;

    public SubscriptionPrice() {}

    public SubscriptionPrice(BigInteger price, Currency currency) {
        this.price = price;
        this.currency = currency;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @JsonSerialize(using= BigIntegerSerializer.class)
    public BigInteger getPrice() {
        return price;
    }

    public void setPrice(BigInteger price) {
        this.price = price;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
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
                .append(price, that.price)
                .append(currency, that.currency)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(price)
                .append(currency)
                .toHashCode();
    }

    @Override
    public String toString() {
        HashMap<String, Object> map = new HashMap<>(4);
        map.put("id", id);
        map.put("price", price);
        map.put("currency", currency);
        map.put("subscriptionPlan", subscriptionPlan);
        return JsonUtil.writeValueAsString(map);
    }
}
