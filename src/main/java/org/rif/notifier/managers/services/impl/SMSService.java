package org.rif.notifier.managers.services.impl;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.rif.notifier.managers.services.NotificationService;
import org.rif.notifier.models.entities.NotificationLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class SMSService implements NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(SMSService.class);

    private PhoneNumber from;

    @Autowired
    public SMSService(@Qualifier("fromPhoneNumber") PhoneNumber from)   {
        this.from = from;
    }

    /**
     * Sends a sms notification to given notification. from phone is configured in application.properties (see NotifierConfig.java)
     * @param notificationLog the log containing the destination for given preference, and the data from notification
     * @return
     */
    @Override
    public String sendNotification(NotificationLog notificationLog){
        logger.info("sending sms notification for id " + notificationLog.getNotification().getId());
        String destination = notificationLog.getNotificationPreference().getDestination();
        String data = notificationLog.getNotification().getData();
        Message message = Message.creator(new PhoneNumber(destination), from,
                data).create();
        return message.getStatus().toString();
    }
}
