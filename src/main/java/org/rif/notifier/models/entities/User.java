package org.rif.notifier.models.entities;

import javax.persistence.*;

@Entity
@Table(name = "notif_users")
public class User {

    @Id
    private String address;

    @Column(name = "api_key")
    private String apiKey;

    @Transient
    private String plainTextKey;

    public User() {}

    public User(String address, String apiKey) {
        this.address = address;
        this.apiKey = apiKey;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getPlainTextKey() {
        return plainTextKey;
    }

    public void setPlainTextKey(String plainTextKey) {
        this.plainTextKey = plainTextKey;
    }

    @Override
    public String toString() {
        return "{" +
                "address=" + address +
                '}';
    }
}
