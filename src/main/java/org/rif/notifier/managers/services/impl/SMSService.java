package org.rif.notifier.managers.services.impl;

import org.rif.notifier.managers.services.NotificationService;
import org.rif.notifier.models.entities.NotificationLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SMSService implements NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(SMSService.class);
    @Override
    public void sendNotification(NotificationLog notificationLog){
        /*String destination = notificationLog.getNotificationPreference().getDestination();
        String data = notificationLog.getNotification().getData();
        Message message = Message.creator(new PhoneNumber(destination),
                new PhoneNumber("+15017250604"),
                data).create();*/
        logger.info("sending notification ******* ");
    }
}
