package org.rif.notifier.boot.configuration;

import com.twilio.Twilio;
import com.twilio.type.PhoneNumber;
import org.rif.notifier.exception.ScheduledErrorHandler;
import org.rif.notifier.exception.ValidationException;
import org.rif.notifier.models.entities.NotificationServiceType;
import org.rif.notifier.util.Utils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.web3j.abi.datatypes.Address;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Configuration
public class NotifierConfig implements SchedulingConfigurer {

    //comma separated sms,api,email - list of enabled services
    @Value("${notificationservice.services.enabled}")
    private List<String> enabledServices;

    @Value("${notificationservice.twilio.account_sid:#{null}}")
    private Optional<String> twilioSid;

    @Value("${notificationservice.twilio.auth_token:#{null}}")
    private Optional<String> twilioAuthToken;

    //phone number used as from number
    @Value("${notificationservice.sms.from:#{null}}")
    private Optional<String> fromPhone;


    //from email address for  emailservice
    @Value("${notificationservice.email.from:#{null}}")
    private Optional<String> fromEmail;

    @Value("${notifier.executor.corePoolSize}")
    private int corePoolSize;

    @Value("${notifier.executor.maxPoolSize}")
    private int maxPoolSize;

    @Value("${notifier.executor.queueCapacity}")
    private int queueCapacity;

    //thread pool size for all the scheduled tasks
    @Value("${notifier.run.poolSize}")
    private int schedulerPoolSize;

    /**spring boot mail properties */
    @Value("${spring.mail.host:#{null}}")
    Optional<String> smtpHost;

    @Value("${spring.mail.port:#{null}}")
    Optional<String> smtpPort;

    @Value("${spring.mail.username:#{null}}")
    Optional<String> smtpUser;

    @Value("${spring.mail.password:#{null}}")
    Optional<String> smtpPassword;

    @Value("${spring.mail.properties.mail.smtp.auth:#{null}}")
    Optional<Boolean> smtpAuth;

    @Value("${spring.mail.properties.mail.smtp.connectiontimeout:#{null}}")
    Optional<Long> smtpConnectionTimeout;

    @Value("${spring.mail.properties.mail.smtp.timeout:#{null}}")
    Optional<Long> smtpTimeout;

    @Value("${spring.mail.properties.mail.smtp.writetimeout:#{null}}")
    Optional<Long> smtpWriteTimeout;

    @Value("${rif.notifier.provider.address:#{null}}")
    Optional<String> providerAddress;

    //comma separated rif,rbtc - list of accepted currencies
    @Value("${rif.notifier.subscription.currencies}")
    private List<String> acceptedCurrencies;

    @Value("${rif.notifier.provider.privatekey:#{null}}")
    Optional<String> providerPrivateKey;

    /**
     * Returns from PhoneNumber to use with twilio if sms service is enabled
     * @throws ValidationException
     */
    @Bean(name="fromPhoneNumber")
    public PhoneNumber initTwilio()  {
        boolean enabled = getEnabledServices().stream().anyMatch(e->e==NotificationServiceType.SMS);
        if (enabled) {
            Twilio.init(twilioSid.get(), twilioAuthToken.get());
            return new PhoneNumber(fromPhone.orElseThrow(()->new ValidationException("Required application property notificationservice.sms.from is not set")));
        }
        return new PhoneNumber(null);
    }

    /**
     * Returns from email to use with javamail if email service is enabled
     * @throws ValidationException
     */
    @Bean(name="fromEmail")
    public String initEmail()  {
        boolean enabled = getEnabledServices().stream().anyMatch(e->e==NotificationServiceType.EMAIL);
        return enabled ? fromEmail.orElseThrow(()->new ValidationException("Required application property notificationservice.email.from not set")) : "";
    }

    /**
     * validate the provider address and make the property available for other classes
     */
    @Bean(name="providerAddress")
    public Address providerAddress() {
       String address = providerAddress.orElseThrow(()->new ValidationException("rif.notifier.provider.address property is mandatory. Please provide a valid address"));
       try {
           //TODO: extended validation to verify address length
           //verifies if it's a valid hex address
           return new Address(address);
       } catch(RuntimeException e)  {
           throw new ValidationException("Invalid provider address format specified in rif.notifier.provider.address property. Please provide a valid address", e);
       }
    }

    /**
     * validate the provider provider privatekey and make the property available for other classes
     */
    @Bean(name="providerPrivateKey")
    public String providerPrivateKey() {
        String privateKey = providerPrivateKey.orElseThrow(()->new ValidationException("rif.notifier.provider.privatekey property is mandatory. Please provide a valid privatekey"));
        try {
            Utils.verify(privateKey);
            return privateKey;
        } catch(RuntimeException e)  {
            throw new ValidationException("Invalid private key specified. " + e.getMessage(), e);
        }
    }

    /**
     * if a given type of notification service is enabled, then the properties are required.
     * if properties not set then throw exception
     * @throws ValidationException
     */
    @Bean(name="initNotificationServices")
    public void initNotificationServices()    {
        getEnabledServices().stream().forEach(e->
        {
           if (e == NotificationServiceType.SMS)    {
               isPropertySet(twilioSid, "notificationservice.twilio.account_sid");
               isPropertySet(twilioAuthToken, "notificationservice.twilio.auth_token");
               isPropertySet(fromPhone, "notificationservice.sms.from");
           }
           else if (e == NotificationServiceType.EMAIL) {
               isPropertySet(fromEmail, "notificationservice.email.from");
               isPropertySet(smtpHost, "spring.mail.host");
               isPropertySet(smtpPort, "spring.mail.port");
               isPropertySet(smtpUser, "spring.mail.username");
               isPropertySet(smtpPassword, "spring.mail.password");
               isPropertySet(smtpAuth, "spring.mail.smtp.auth");
               isPropertySet(smtpConnectionTimeout, "spring.mail.properties.mail.smtp.connectiontimeout");
               isPropertySet(smtpTimeout, "spring.mail.properties.mail.smtp.timeout");
               isPropertySet(smtpWriteTimeout, "spring.mail.properties.mail.smtp.writetimeout");
           }
        });
    }

    /**
     * configures asynchronous execution of tasks
     * @return
     */
    @Bean
    public Executor taskExecutor() {
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.initialize();
        return executor;
    }

    /**
     * Configures task scheduler
     * @param taskRegistrar
     */
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar)    {
        TaskScheduler s = taskRegistrar.getScheduler();
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setErrorHandler(new ScheduledErrorHandler());
        taskScheduler.setThreadNamePrefix("notifier-scheduled-task-");
        taskScheduler.setPoolSize(schedulerPoolSize);
        taskScheduler.initialize();
        taskRegistrar.setTaskScheduler(taskScheduler);
    }

    /**
     * Returns a list of enabled services
     * @return
     */
    public List<NotificationServiceType> getEnabledServices()    {
        return enabledServices.stream().map(e->NotificationServiceType.valueOf(e)).collect(Collectors.toList());
    }

    public List<String> getAcceptedCurrencies() {
        return acceptedCurrencies;
    }

    private void isPropertySet(Optional<?> propVal, String propName)   {
        propVal.orElseThrow(()->new ValidationException("Property " + propName + " is required."));
    }

}
