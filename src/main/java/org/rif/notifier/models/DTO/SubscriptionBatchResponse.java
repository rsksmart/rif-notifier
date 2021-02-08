package org.rif.notifier.models.DTO;

import io.swagger.annotations.ApiModel;

@ApiModel(description = "Serves as response to subscribeToPlan endpoint")
public class SubscriptionBatchResponse {
    private String hash;
    private String signature;
    private SubscriptionDTO subscription;

    public SubscriptionBatchResponse(String hash, String signature, SubscriptionDTO subscription) {
        this.hash = hash;
        this.signature = signature;
        this.subscription = subscription;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public SubscriptionDTO getSubscription() {
        return subscription;
    }

    public void setSubscription(SubscriptionDTO subscription) {
        this.subscription = subscription;
    }
}
