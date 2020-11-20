package org.rif.notifier;

import org.rif.notifier.boot.configuration.WebConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@EnableScheduling
@EnableAsync
@SpringBootApplication(
        scanBasePackages = {"org.rif.notifier.datafetcher","org.rif.notifier.controllers", "org.rif.notifier.services",
                "org.rif.notifier.managers", "org.rif.notifier.managers.datamanagers", "org.rif.notifier.scheduled", "org.rif.notifier.repositories",
                "org.rif.notifier.notificationmanagers", "org.rif.notifier.helpers"},
        scanBasePackageClasses = {
                WebConfiguration.class,
        })
@PropertySource(ignoreResourceNotFound = true, value = "classpath:application.properties")
public class Application extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Application.class);
    }

    @Bean
    public Executor taskExecutor() {
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //TODO move to properties of the configurator
        executor.setCorePoolSize(7);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(11);
        executor.initialize();
        return executor;
    }


    public static void main(String[] args) { SpringApplication.run(Application.class, args); }
}
