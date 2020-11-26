package org.rif.notifier.managers.services;

import org.rif.notifier.exception.NotificationException;
import org.rif.notifier.models.entities.NotificationLog;

/**
 * A NotificationService can be SMSService, APIService, or EmailService and so on..
 * Each service must implement the sendNotification method
 */
public interface NotificationService {
    /**
     * Clients of the implementing service will call this method.
     * This method sends the notification and updates the result text and status sent true or false
     * @param log the notificationlog for given notification and notification preference
     * @throws NotificationException
     */
    default void sendNotificationAndUpdateLog(NotificationLog log)  throws NotificationException {
        try {
            log.incrementRetryCount();
            String result = sendNotification(log);
            //if all goes well set sent to true
            log.setSent(true);
            log.setResultText(result);
        }catch(Error | Exception e) {
            //handle any exception and set sent to false
            log.setSent(false);
            log.setResultText(e.getMessage());
            throw e;
        }
    }

    /**
     * sends the notification
     * @param log - a log that has reference to notification, and notification preference
     *
     */
    String sendNotification(NotificationLog log) throws NotificationException;
}
