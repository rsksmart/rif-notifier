package org.rif.notifier.services;

import org.rif.notifier.exception.NotificationException;
import org.rif.notifier.managers.DbManagerFacade;
import org.rif.notifier.managers.services.NotificationService;
import org.rif.notifier.models.entities.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class NotificationServices {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServices.class);

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    private DbManagerFacade dbManagerFacade;

    /**
     * Given an Address gets all the notifications
     *
     * @param subscription Address of a user to be notified
     * @return List<Notification> with all the notifications of a user address
     */
    public List<Notification> getNotificationsForSubscription(Subscription subscription, Integer id, Integer lastRows, Set<Integer> idTopic) {
        List<Notification> lst = new ArrayList<>();
            //This will need to be migrated by topic
        Optional.ofNullable(subscription).ifPresent((sub) -> {
            if (sub.isActive())
                lst.addAll(dbManagerFacade.getNotificationsBySubscription(sub, id, lastRows, idTopic));
            });
        return lst;
    }

    /**
     * First implementation of notifyUsers method, that will be called to send all notification by preferences indicated previously by the end-user
     //TODO Remove this method
    public void notifyUsers() {
        List<Subscription> activeSubs = dbManagerFacade.getAllActiveSubscriptions();
        for (Subscription sub : activeSubs) {
            List<Notification> notifications = dbManagerFacade.getNotificationsBySubscription(sub, null, null, null);
            for (NotificationPreference preference : sub.getNotificationPreferences()) {
                //TODO: write notification code
            }
        }
    }
     */

    /**
     * Sends a given notification for each preference (sms, api, email etc.) and saves the result of
     * sent or not in the containing list of notification log for each preference
     * @param notification
     * @return
     */
    public Notification sendNotification(Notification notification, int maxRetries) {
        //only get those logs that are not already sent and whose retry count not exceeded the maxRetries parameter
        List<NotificationLog> logs = notification.getNotificationLogs().stream().filter(log->!log.isSent() && log.getRetryCount()<maxRetries).collect(Collectors.toList());
        logs.forEach(log -> {
            NotificationPreference preference = log.getNotificationPreference();
            try {
                NotificationService service = (NotificationService) applicationContext.getBean(
                        log.getNotificationPreference().getNotificationService().getClassName());
                service.sendNotificationAndUpdateLog(log);
            }catch(Error | Exception e) {
                logger.warn("Error sending notification for " + log.getNotificationPreference().getNotificationService() + " " + notification.getId(), e);
            }
        });
        return notification;
    }

    /**
     * Saves the notification, and the containing notification logs to the database. When all containing
     * notification log for each notification preference are successfully sent,
     * a notification is set to the status of sent true.
     * @param notification
     * @return
     */
    public Notification saveNotification(Notification notification) {
        notification.setSent(notification.areAllNotificationLogsSent());
        return dbManagerFacade.saveNotification(notification);
    }
}
