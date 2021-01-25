package org.rif.notifier.health;

import com.twilio.Twilio;
import com.twilio.http.HttpMethod;
import com.twilio.http.Request;
import com.twilio.http.Response;
import com.twilio.http.TwilioRestClient;
import org.rif.notifier.boot.configuration.NotifierConfig;
import org.rif.notifier.models.entities.NotificationServiceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Verifies if sms service is functional, provided the service is enabled thru the configuration
 * property notificationservice.services.enabled
 */
@Component
public class SMSHealthIndicator extends AbstractHealthIndicator {
    NotifierConfig config;

    /**
     * notifierConfig, which is used to retrieve enabled services
     * @param config
     */
    public SMSHealthIndicator(@Autowired NotifierConfig config)    {
        super("SMS health check failed");
        this.config = config;
    }

    /**
     * conditionally checks if sms service is working properly when the service is enable thru
     * configuration.
     * @param builder
     * @throws Exception
     */
    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception    {
        List<NotificationServiceType> enabled = config.getEnabledServices();
        boolean smsServiceEnabled = enabled.stream().anyMatch(s->s.equals(NotificationServiceType.SMS));
        if(smsServiceEnabled) {
            TwilioRestClient rc = Twilio.getRestClient();
            Request req = new Request(HttpMethod.GET, "https://api.twilio.com/2010-04-01/Accounts");
            Response resp = rc.request(req) ;
            if (HttpStatus.resolve(resp.getStatusCode()) == HttpStatus.OK) {
                builder.up();
            }
            else    {
                builder.withDetail("twilio-api-response", resp.getContent());
                builder.down();
            }
        }
        else    {
            builder.withDetail("service","service is not enabled in configuration");
            builder.status("disabled").build();
        }
    }
}
