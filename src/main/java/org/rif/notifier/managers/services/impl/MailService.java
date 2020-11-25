package org.rif.notifier.managers.services.impl;

import org.rif.notifier.managers.services.NotificationService;
import org.rif.notifier.models.entities.NotificationLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MailService implements NotificationService {
    @Override
    public void sendNotification(NotificationLog notificationLog){
        String destination = notificationLog.getNotificationPreference().getDestination();
    }
}
