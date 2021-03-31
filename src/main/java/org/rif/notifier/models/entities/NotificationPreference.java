package org.rif.notifier.models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.rif.notifier.util.JsonUtil;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.HashMap;


@Entity
@Table(name = "notification_preference")
@TypeDef(
        name = "json",
        typeClass = com.vladmihalcea.hibernate.type.json.JsonStringType.class
)
@ApiModel(description="Defines a notification preference for a given subscription and topic")
public class NotificationPreference {
    @Id
    @GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    private int id;

    @ApiModelProperty(notes="Type of notification preference",  allowableValues = "SMS,EMAIL,API", required=true, example="SMS")
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_service")
    private NotificationServiceType notificationService;

    //FK to subscription
    @JsonIgnore
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="id_subscription")
    private Subscription subscription;

    @ApiModelProperty(notes="Destination for ex. +13477777777 or test@test.com or http://apiurl",  required=true, example="test@test.com")
    @NotBlank
    @Column
    private String destination;

    @ApiModelProperty(notes="Destination parameters for API preference type",  required=false)
    @Type(type="json")
    @Column(columnDefinition = "json")
    private DestinationParams destinationParams;

    @Column(name="id_topic")
    private int idTopic;

    public NotificationPreference() {

    }

    public NotificationPreference(Subscription subscription, int idTopic, NotificationServiceType notificationService, String destination, DestinationParams destinationParams) {
        this.notificationService = notificationService;
        this.subscription = subscription;
        this.destination = destination;
        this.destinationParams = destinationParams;
        this.idTopic = idTopic;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public NotificationServiceType getNotificationService() {
        return notificationService;
    }

    public void setNotificationService(NotificationServiceType notificationService) {
        this.notificationService = notificationService;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public DestinationParams getDestinationParams() {
        return destinationParams;
    }

    public void setDestinationParams(DestinationParams destinationParams) {
        this.destinationParams = destinationParams;
    }

    public int getIdTopic() {
        return idTopic;
    }

    public void setIdTopic(int idTopic) {
        this.idTopic = idTopic;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        NotificationPreference that = (NotificationPreference) o;

        return new EqualsBuilder()
                .append(id, that.id)
                .append(idTopic, that.idTopic)
                .append(notificationService, that.notificationService)
                .append(subscription, that.subscription)
                .append(destination, that.destination)
                .append(destinationParams, that.destinationParams)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(notificationService)
                .append(subscription)
                .append(destination)
                .append(destinationParams)
                .append(idTopic)
                .toHashCode();
    }

    @Override
    public String toString() {
        HashMap<String, Object> map = new HashMap<>(1);
        map.put("notificationService", notificationService);
        return JsonUtil.writeValueAsString(map) ;
    }
}
