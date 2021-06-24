package org.rif.notifier.models.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import org.rif.notifier.models.entities.Currency;
import org.rif.notifier.models.entities.SubscriptionPayment;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubscriptionDTO {
    private Integer id;
    private String hash;
    private String apiKey;
    private Date activeSince;
    private int notificationBalance;
    private SubscriptionStatus status;
    private Date expirationDate;
    private Boolean paid;
    private List<SubscriptionPayment> subscriptionPayments;
    private Integer subscriptionPlanId;
    private BigInteger price;
    private Currency currency;
    private List<TopicDTO> topics;
    private String userAddress;
    private Address providerAddress;
    private SubscriptionDTO previousSubscription;
    private String signature;



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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getActiveSince() {
        return activeSince;
    }

    public void setActiveSince(Date activeSince) {
        this.activeSince = activeSince;
    }

    public Integer getSubscriptionPlanId() {
        return subscriptionPlanId;
    }

    public void setSubscriptionPlanId(Integer subscriptionPlanId) {
        this.subscriptionPlanId = subscriptionPlanId;
    }

    public SubscriptionDTO getPreviousSubscription() {
        return previousSubscription;
    }

    public void setPreviousSubscription(SubscriptionDTO previousSubscription) {
        this.previousSubscription = previousSubscription;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public List<SubscriptionPayment> getSubscriptionPayments() {
        return subscriptionPayments;
    }

    public void setSubscriptionPayments(List<SubscriptionPayment> subscriptionPayments) {
        this.subscriptionPayments = subscriptionPayments;
    }

    public Boolean getPaid() {
        return paid;
    }

    public void setPaid(Boolean paid) {
        this.paid = paid;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
