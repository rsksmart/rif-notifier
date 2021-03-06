package org.rif.notifier.models.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.rif.notifier.util.JsonUtil;

import javax.persistence.*;
import java.util.HashMap;

@Entity
@Table(name = "notif_users")
public class User {

    @Id
    private String address;

    @JsonProperty(access= JsonProperty.Access.WRITE_ONLY)
    @Column(name = "api_key")
    private String apiKey;

    @JsonProperty(access= JsonProperty.Access.WRITE_ONLY)
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
        HashMap<String, String> map = new HashMap<>(1);
        map.put("address", address);
        return JsonUtil.writeValueAsString(map);
    }
}
