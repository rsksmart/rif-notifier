package org.rif.notifier.boot.configuration;

import com.twilio.Twilio;
import com.twilio.type.PhoneNumber;
import org.rif.notifier.exception.ScheduledErrorHandler;
import org.rif.notifier.scheduled.NotificationProcessorJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.Executor;

@Configuration
public class NotifierConfig implements SchedulingConfigurer {

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

    @Value("${notifier.executor.corePoolSize}")
    private int corePoolSize;

    @Value("${notifier.executor.maxPoolSize}")
    private int maxPoolSize;

    @Value("${notifier.executor.queueCapacity}")
    private int queueCapacity;

    //thread pool size for all the scheduled tasks
    @Value("${notifier.run.poolSize}")
    private int schedulerPoolSize;


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
}
