package org.rif.notifier.managers.datamanagers;

import org.rif.notifier.models.entities.NotificationLog;
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

    public Set<NotificationLog> getAllUnsentNotificationLogsByNotificationId(int notificationId){
        return notificationLogRepository.findAllByNotificationIdAndSentFalse(notificationId);
    }

    public void logSuccesfulNotification(int notificationId, int notificationPreferenceId, String resultText)    {
        logNotification(notificationId, notificationPreferenceId, true, resultText);
    }

    public void logFailedNotification(int notificationId, int notificationPreferenceId, String resultText)    {
       logNotification(notificationId, notificationPreferenceId, false, resultText);
    }

    protected void logNotification(int notificationId, int notificationPreferenceId, boolean sent, String resultText)    {
        NotificationLog log = notificationLogRepository.findByNotificationIdAndNotificationPreferenceId(notificationId, notificationPreferenceId);
        if (log != null)    {
            log.setSent(sent);
        }
        else    {
            log = new NotificationLog(notificationId, notificationPreferenceId, sent, resultText);
        }
        notificationLogRepository.save(log);
    }
}
