package org.rif.notifier.models.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import org.rif.notifier.models.entities.SubscriptionStatus;
import org.rif.notifier.models.serializer.BigIntegerSerializer;
import org.web3j.abi.datatypes.Address;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

/**
 * This class is used to store the subscription information for generating
 * the json contract as part of response to SubscriptionBatchController.
 * This object is stored inside SubscriptionContractDTO while generating the
 * contract
 */
@ApiModel(description="Json response to the subscribeToPlan operation")
public class SubscriptionDTO {
    private BigInteger price;
    private String currency;
    private int notificationBalance;
    private Date expirationDate;
    private SubscriptionStatus status;
    private List<TopicDTO> topics;
    private Address providerAddress;
    private String userAddress;
    private String apiKey;

    @JsonSerialize(using= BigIntegerSerializer.class)
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

    public int getNotificationBalance() {
        return notificationBalance;
    }

    public void setNotificationBalance(int notificationBalance) {
        this.notificationBalance = notificationBalance;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public void setStatus(SubscriptionStatus status) {
        this.status = status;
    }

    public List<TopicDTO> getTopics() {
        return topics;
    }

    public void setTopics(List<TopicDTO> topics) {
        this.topics = topics;
    }

    public Address getProviderAddress() {
        return providerAddress;
    }

    public void setProviderAddress(Address providerAddress) {
        this.providerAddress = providerAddress;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
