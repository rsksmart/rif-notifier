package org.rif.notifier.repositories;

import org.rif.notifier.models.entities.NotificationLog;
import org.rif.notifier.models.entities.NotificationPreference;
import org.rif.notifier.models.entities.NotificationServiceType;
import org.rif.notifier.models.entities.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Integer> {
    Set<NotificationLog> findAllBySentFalseAndRetryCountLessThan(int retryCount);
    Set<NotificationLog> findAllByNotificationIdAndSentFalse(int notificationId);
    NotificationLog findByNotificationIdAndNotificationPreferenceId(int notificationId, int notificationPreferenceId);
    NotificationLog findByNotificationIdAndNotificationPreferenceIdAndSentFalse(int notificationId, int notificationPreferenceId);
}


