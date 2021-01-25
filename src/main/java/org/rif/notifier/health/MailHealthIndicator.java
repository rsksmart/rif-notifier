package org.rif.notifier.health;

import org.rif.notifier.boot.configuration.NotifierConfig;
import org.rif.notifier.models.entities.NotificationServiceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Verifies if mail service is functional, provided the service is enabled thru the configuration
 * property notificationservice.services.enabled
 */
@Component
public class MailHealthIndicator extends AbstractHealthIndicator {
    NotifierConfig config;
    JavaMailSenderImpl mailSender;

    /**
     * notifierConfig, which is used to retrieve enabled services
     * @param mailSender
     * @param config
     */
    public MailHealthIndicator(JavaMailSenderImpl mailSender, @Autowired NotifierConfig config)    {
        super("Mail health check failed");
        this.mailSender = mailSender;
        this.config = config;
    }

    /**
     * conditionally checks if mail service is working properly when the service is enable thru
     * configuration.
     * @param builder
     * @throws Exception
     */
    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception    {
        List<NotificationServiceType> enabled = config.getEnabledServices();
        boolean mailServiceEnabled = enabled.stream().anyMatch(s->s.equals(NotificationServiceType.EMAIL));
        if(mailServiceEnabled) {
            builder.withDetail("location", this.mailSender.getHost() + ":" + this.mailSender.getPort());
            this.mailSender.testConnection();
            builder.up();
        }
        else    {
            builder.withDetail("service", "service is not enabled in configuration");
            builder.status("disabled").build();
        }
    }
}
