package org.rif.notifier.managers.services.impl;

import org.rif.notifier.exception.NotificationException;
import org.rif.notifier.managers.services.NotificationService;
import org.rif.notifier.models.entities.NotificationLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service("EmailService")
public class EmailService implements NotificationService {

    private static final String DEFAULT_MAIL_SUBJECT = "RSK Notification";
    private static final String SUCCESS_STRING = "success";
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    JavaMailSender mailSender;

    private String fromEmail;

    @Autowired
    public EmailService(@Qualifier("fromEmail") String fromEmail)   {
        this.fromEmail = fromEmail;
    }

    @Override
    public String sendNotification(NotificationLog notificationLog) throws NotificationException    {
        logger.info("sending email notification for id " + notificationLog.getNotification().getId());
        SimpleMailMessage msg = new SimpleMailMessage();
        String destination = notificationLog.getNotificationPreference().getDestination();
        msg.setTo(destination.split(";"));
        msg.setSubject(DEFAULT_MAIL_SUBJECT);
        msg.setText(notificationLog.getNotification().getData());
        msg.setFrom(fromEmail);
        try {
            mailSender.send(msg);
        }catch(MailException e)   {
            throw new NotificationException(e.getMessage(), e);
        }
        return SUCCESS_STRING;
    }
}
