package org.rif.notifier.scheduled;

import org.rif.notifier.constants.TopicTypes;
import org.rif.notifier.managers.DbManagerFacade;
import org.rif.notifier.models.entities.*;
import org.rif.notifier.services.NotificationServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
public class NotificationProcessorJob {
    private static final Logger logger = LoggerFactory.getLogger(NotificationProcessorJob.class);

    @Autowired
    private DbManagerFacade dbManagerFacade;

    @Autowired
    private NotificationServices notificationServices;


    /**
     * Gets all Unsent notifications, and send the unsent notifications.
     */
    @Scheduled(fixedDelayString = "${notifier.run.fixedDelayProcessJob}", initialDelayString = "${notifier.run.fixedInitialDelayProcessJob}")
    public void run() {
        //find all unsent notifications with active subscription and retry count less than maximum retry count
        List<Notification> unsentNotifications = dbManagerFacade.getUnsentNotificationsWithActiveSubscription(100);
        List<CompletableFuture> futureNotifications = new ArrayList<>();
        //process each unsent notification by sending the notification using all notification preferences for ex. sms, api, email
        // in case of successful sending of notification for all preferences, the sent flag is set to true, if any of them fail, the sent flag is set to false, so they can be reproceses
        unsentNotifications.forEach(notification->{
            List<NotificationPreference> notificationPreferences = dbManagerFacade.getNotificationPreferences(notification.getSubscription(), notification.getIdTopic());
            //only get those logs that are not already sent
            List<NotificationLog> logs = notification.getNotificationLogs().stream().filter(log->!log.isSent()).collect(Collectors.toList());
            //find all those preferences for which notification log entry is not created
            List<NotificationPreference> missingPreferences = notificationPreferences.stream().filter(pref-> logs.stream().noneMatch(log-> log.getNotificationPreference().equals(pref))).collect(Collectors.toList());
            missingPreferences.forEach(pref -> {
                NotificationLog notificationLog = new NotificationLog();
                notificationLog.setNotificationPreference(pref);
                notificationLog.setNotification(notification);
                notification.getNotificationLogs().add(notificationLog);
            });
            //send the notification and get the completable future
            CompletableFuture<Notification> future = CompletableFuture.supplyAsync(()->notificationServices.sendNotification(notification))
                    .handle((notif,e) ->{
                        if(e != null)  {
                            logger.error("Error sending notification " + notif, e);
                            return null;
                        }
                        else
                            return notificationServices.saveNotification(notif);
                    }).exceptionally(e -> {
                        logger.error("Error saving notification " , e);
                        return null;
                    });
        });
        CompletableFuture.allOf(futureNotifications.toArray(new CompletableFuture[futureNotifications.size()]))
        .whenComplete((t,e)->{
            if(e == null)   {
                logger.info("Completed processing notifications");
            }
        });
    }
}
