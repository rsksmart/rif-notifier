package org.rif.notifier.models.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.rif.notifier.models.entities.Subscription;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigInteger;
import java.util.List;

/**
 * This class is used to store the data to create a subscription with all the details
 * including topics, preferences, user in one single batch process
 */
public class SubscriptionBatchDTO {
    @NotEmpty @Valid
    private List<TopicDTO> topics;
    @NotBlank
    private String userAddress;
    @NotNull @Min(1)
    private BigInteger price;
    @NotBlank
    private String currency;
    @Min(1)
    private int subscriptionPlanId;

    public List<TopicDTO> getTopics() {
        return topics;
    }

    public void setTopics(List<TopicDTO> topics) {
        this.topics = topics;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
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

    public int getSubscriptionPlanId() {
        return subscriptionPlanId;
    }

    public void setSubscriptionPlanId(int subscriptionPlanId) {
        this.subscriptionPlanId = subscriptionPlanId;
    }
}
