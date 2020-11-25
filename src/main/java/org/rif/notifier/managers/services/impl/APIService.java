package org.rif.notifier.managers.services.impl;

import org.rif.notifier.models.entities.Notification;
import org.rif.notifier.managers.services.NotificationService;
import org.rif.notifier.models.entities.NotificationLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class APIService implements NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(APIService.class);
    @Override
    public void sendNotification(NotificationLog notification) {
        logger.info("sending notification ******* ");
    }
}
