package org.rif.notifier.scheduled;

import org.rif.notifier.constants.TopicTypes;
import org.rif.notifier.managers.DbManagerFacade;
import org.rif.notifier.models.entities.*;
import org.rif.notifier.services.NotificationServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
public class NotificationProcessorJob {
    private static final Logger logger = LoggerFactory.getLogger(NotificationProcessorJob.class);

    @Autowired
    private DbManagerFacade dbManagerFacade;

    @Autowired
    private NotificationServices notificationServices;


    @Value("${notificationservice.maxretries}")
    private int maxRetries;


    /**
     * Sends all Unsent notifications with active subscription (based on topicid, and user_address) for given notification.
     * The notification for each notification preference will be retried in case of failure, until the max_retries are reached.
     * For each notification preference  for a given notification a log will be maintained in NotificationLog to manage number
     * of retries and the response message
     * delay parameters below are configurable thru application.properties
     */
    @Scheduled(fixedDelayString = "${notifier.run.fixedDelayNotificationJob}", initialDelayString = "${notifier.run.fixedInitialDelayNotificationJob}")
    public void run() {
        //Find all unsent notifications with active subscription and retry count less than maximum retry count. Ignore those notifications for which there is no
        //active subscription, or if the maximum retries value is reached
        long start = System.currentTimeMillis();
        Set<Notification> unsentNotifications = dbManagerFacade.getUnsentNotificationsWithActiveSubscription(maxRetries);
        logger.info("Processing unsent notifications count " + unsentNotifications.size());
        List<CompletableFuture> futureNotifications = new ArrayList<>();
        //process each unsent notification by sending the notification using all notification preferences for ex. sms, api, email
        // in case of successful sending of notification for ll preferences, the sent flag is set to true, if any of them fail, the sent flag is set to false, so they can be reproceses
        unsentNotifications.forEach(notification->{
            //find all notification preferences for given subscription and topic id
            List<NotificationPreference> notificationPreferences = dbManagerFacade.getNotificationPreferences(notification.getSubscription(), notification.getIdTopic());
            //notification preference with topic id 0 is default preference
            List<NotificationPreference> defaultPreferences = dbManagerFacade.getNotificationPreferences(notification.getSubscription(), 0);
            List<NotificationPreference> filtered = defaultPreferences.stream().filter(p1-> notificationPreferences
                                        .stream().noneMatch(p2 -> p2.getNotificationService() == p1.getNotificationService())).collect(Collectors.toList());
            //add any default preference for which there is no corresponding notification service associated for given subscription and topic
            notificationPreferences.addAll(filtered);
            List<NotificationLog> logs = notification.getNotificationLogs();
            //find all those preferences for which notification log entry is not created, and add those notificationlogs to notification
            List<NotificationPreference> missingPreferences = notificationPreferences.stream().filter(pref-> logs.stream().noneMatch(log-> log.getNotificationPreference().equals(pref))).collect(Collectors.toList());
            missingPreferences.forEach(pref -> {
                NotificationLog notificationLog = new NotificationLog();
                //associate each new log to a notification preference
                notificationLog.setNotificationPreference(pref);
                notificationLog.setNotification(notification);
                notification.getNotificationLogs().add(notificationLog);
            });
            //send the notification and get the completable future, in case of no error, proceed to save the notification and its containing logs
            CompletableFuture<Notification> future = CompletableFuture.supplyAsync(()->notificationServices.sendNotification(notification, maxRetries))
                    .handle((notif,e) ->{
                        if(e != null) {
                            logger.warn("Error sending notification " + notif, e);
                        }
                        //save notification even for failure so retry count is maintained
                        return notif != null ? notificationServices.saveNotification(notif) : null;
                    }).exceptionally(e -> {
                        logger.error("Error saving notification " , e);
                        return null;
                    });
            futureNotifications.add(future);
        });
        CompletableFuture.allOf(futureNotifications.toArray(new CompletableFuture[futureNotifications.size()]))
        .whenComplete((t,e)->{
            if(e == null)   {
                long timeTaken = (System.currentTimeMillis() - start)/1000;
                logger.info("Completed processing notifications. Total time taken in seconds: " + timeTaken);
                long sent = unsentNotifications.stream().filter(n->n.isSent()).count();
                long unsent = unsentNotifications.stream().filter(n->!n.isSent()).count();
                logger.info("Total sent: " + sent);
                logger.info("Total failed: " + unsent);
            }
        });
    }
}
