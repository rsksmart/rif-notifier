package org.rif.notifier.repositories;

import org.rif.notifier.models.entities.Notification;
import org.rif.notifier.models.entities.NotificationLog;
import org.rif.notifier.models.entities.NotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Integer> {
    Set<NotificationLog> findAllBySentFalseAndRetryCountLessThan(int retryCount);
    Set<NotificationLog> findAllByNotificationAndSentFalse(Notification notification);
    NotificationLog findByNotificationAndNotificationPreference(Notification notification, NotificationPreference notificationPreference);
    NotificationLog findByNotificationAndNotificationPreferenceAndSentFalse(Notification notification, NotificationPreference notificationPreference);
}
