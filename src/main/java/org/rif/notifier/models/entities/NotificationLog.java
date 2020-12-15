package org.rif.notifier.models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "NOTIFICATION_LOG")
public class NotificationLog {
    @Id
    @GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    private int id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name="notification_id")
    private Notification notification;

    @ManyToOne
    @JoinColumn(name="notification_preference_id")
    private NotificationPreference notificationPreference;

    private boolean sent;

    @Column(name="result_text")
    private String resultText;

    @UpdateTimestamp
    @Column(name="last_updated")
    private Timestamp lastUpdated;

    @Column(name="retry_count")
    private int retryCount;

    public NotificationLog()    {

    }

    public NotificationLog(Notification notification, NotificationPreference notificationPreference, boolean sent, String resultText) {
        this.notification = notification;
        this.notificationPreference = notificationPreference;
        this.sent = sent;
        this.resultText = resultText;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Notification getNotification() {
        return notification;
    }

    public void setNotification(Notification notification) {
        this.notification = notification;
    }

    public NotificationPreference getNotificationPreference() {
        return notificationPreference;
    }

    public void setNotificationPreference(NotificationPreference notificationPreference) {
        this.notificationPreference = notificationPreference;
    }

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public String getResultText() {
        return resultText;
    }

    public void setResultText(String resultText) {
        this.resultText = resultText;
    }

    public Timestamp getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Timestamp lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public void incrementRetryCount()   {
        this.retryCount++;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        NotificationLog that = (NotificationLog) o;

        return new EqualsBuilder()
                .append(id, that.id)
                .append(notification, that.notification)
                .append(notificationPreference, that.notificationPreference)
                .append(sent, that.sent)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(notification)
                .append(notificationPreference)
                .append(sent)
                .toHashCode();
    }
}
