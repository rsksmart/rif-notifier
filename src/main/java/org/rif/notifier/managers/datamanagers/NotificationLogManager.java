package org.rif.notifier.managers.datamanagers;

import org.rif.notifier.models.entities.Notification;
import org.rif.notifier.models.entities.NotificationLog;
import org.rif.notifier.models.entities.NotificationPreference;
import org.rif.notifier.repositories.NotificationLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class NotificationLogManager {
    @Autowired
    NotificationLogRepository notificationLogRepository;

    public Set<NotificationLog> getAllUnsentNotificationLogs(int maxCount) {
        return notificationLogRepository.findAllBySentFalseAndRetryCountLessThan(maxCount);
    }

    public Set<NotificationLog> getAllUnsentNotificationLogsByNotification(Notification notification){
        return notificationLogRepository.findAllByNotificationAndSentFalse(notification);
    }

    public void logSuccesfulNotification(Notification notification, NotificationPreference notificationPreference, String resultText)    {
        logNotification(notification, notificationPreference, true, resultText);
    }

    public void logFailedNotification(Notification notification, NotificationPreference notificationPreference, String resultText)    {
       logNotification(notification, notificationPreference, false, resultText);
    }

    protected void logNotification(Notification notification, NotificationPreference notificationPreference, boolean sent, String resultText)    {
        NotificationLog log = notificationLogRepository.findByNotificationAndNotificationPreference(notification, notificationPreference);
        if (log != null)    {
            log.setSent(sent);
        }
        else    {
            log = new NotificationLog(notification, notificationPreference, sent, resultText);
        }
        notificationLogRepository.save(log);
    }
}
