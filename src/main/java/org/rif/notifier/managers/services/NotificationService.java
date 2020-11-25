package org.rif.notifier.managers.services;

import org.rif.notifier.models.entities.NotificationLog;

public interface NotificationService {
    default void sendNotificationAndUpdateLog(NotificationLog log)  {
        sendNotification(log);
        log.incrementRetryCount();
        log.setSent(true);
    }

    void sendNotification(NotificationLog log);
}
