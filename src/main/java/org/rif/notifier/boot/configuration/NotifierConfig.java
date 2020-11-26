package org.rif.notifier.boot.configuration;

import com.twilio.Twilio;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotifierConfig {

    @Value("${notificationservice.twilio.account_sid}")
    private String twilioSid;

    @Value("${notificationservice.twilio.auth_token}")
    private String twilioAuthToken;

    //phone number used as from number
    @Value("${notificationservice.sms.from}")
    private String fromPhone;


    //from email address for  emailservice
    @Value("${notificationservice.email.from}")
    private String fromEmail;


    /**
     * providing a non-object return value since this method is used only for initialization
     */
    @Bean(name="fromPhoneNumber")
    public PhoneNumber initTwilio()  {
        Twilio.init(twilioSid, twilioAuthToken);
        return new PhoneNumber(fromPhone);
    }

    /**
     * providing a non-object return value since this method is used only for initialization
     */
    @Bean(name="fromEmail")
    public String initEmail()  {
        return fromEmail;
    }
}
