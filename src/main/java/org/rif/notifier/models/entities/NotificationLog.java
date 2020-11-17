package org.rif.notifier.models.entities;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "NOTIFICATION_LOG")
public class NotificationLog {
    @Id
    private int id;

    @Column(name="notification_id")
    private int notificationId;

    @Column(name="notification_preference_id")
    private int notificationPreferenceId;

    private boolean sent;

    @Column(name="result_text")
    private String resultText;

    @Column(name="last_updated")
    private Timestamp lastUpdated;

    @Column(name="retry_count")
    private int retryCount;

    public NotificationLog(int notificationId, int notificationPreferenceId, boolean sent, String resultText) {
        this.notificationId = notificationId;
        this.notificationPreferenceId = notificationPreferenceId;
        this.sent = sent;
        this.resultText = resultText;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    public int getNotificationPreferenceId() {
        return notificationPreferenceId;
    }

    public void setNotificationPreferenceId(int notificationPreferenceId) {
        this.notificationPreferenceId = notificationPreferenceId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        NotificationLog that = (NotificationLog) o;

        return new EqualsBuilder()
                .append(id, that.id)
                .append(notificationId, that.notificationId)
                .append(notificationPreferenceId, that.notificationPreferenceId)
                .append(sent, that.sent)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(notificationId)
                .append(notificationPreferenceId)
                .append(sent)
                .toHashCode();
    }
}
