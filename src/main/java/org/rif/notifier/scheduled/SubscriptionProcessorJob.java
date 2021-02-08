package org.rif.notifier.scheduled;

import org.apache.commons.lang3.mutable.MutableInt;
import org.rif.notifier.managers.DbManagerFacade;
import org.rif.notifier.models.entities.Subscription;
import org.rif.notifier.services.SubscribeServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SubscriptionProcessorJob {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionProcessorJob.class);

    private DbManagerFacade dbManagerFacade;
    private SubscribeServices subscribeServices;

    public SubscriptionProcessorJob(SubscribeServices subscribeServices, DbManagerFacade dbManagerFacade) {
       this.dbManagerFacade = dbManagerFacade;
       this.subscribeServices = subscribeServices;
    }

    /**
     * Run subscription expiration every 1 hour. Expire the subscription once the expiration date is reached.
     */
    @Scheduled(initialDelayString = "${notifier.run.fixedInitialSubscriptionProcessorJob}", fixedDelayString = "${notifier.run.fixedDelaySubscriptionProcessorJob}")
    public void run() {
        //expire all those subscriptions whose expiration date is less than current date and
        if(subscribeServices.getExpiredSubscriptionsCount() > 0) {
            int numExpired = subscribeServices.updateExpiredSubscriptions();
            logger.info(numExpired + " subscriptions were automatically expired.");
        }
        //activate/renew pending subscriptions that have been paid and have no active or pending previous subscription
        List<Subscription> pendingSubscriptions = subscribeServices.getPendingSubscriptions();
        MutableInt activatedCount = new MutableInt();
        pendingSubscriptions.forEach(sub->{
            if(subscribeServices.activateSubscription(sub)) {
                activatedCount.increment();
            }
        });
        logger.info(activatedCount.intValue() + " subscriptions were automatically activated/renewed.");
    }
}
