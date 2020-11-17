package org.rif.notifier.models.entities;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;

@Entity
@Table(name = "notification_preference")
@TypeDef(
        name = "json",
        typeClass = com.vladmihalcea.hibernate.type.json.JsonStringType.class
)
public class NotificationPreference {
    @Id
    @GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    private int id;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_service")
    private NotificationServiceType notificationService;

    //FK to subscription
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="id_subscription")
    private Subscription subscription;

    @Column
    private String destination;

    @Type(type="json")
    @Column(columnDefinition = "json")
    private DestinationParams destinationParams;

    @Column(name="id_topic")
    private int idTopic;

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
    public String toString() {
        return "{" +
                ", \"notificationService\"=\"" + notificationService + '\"' +
                '}';
    }
}
