package org.rif.notifier.scheduled;

import org.rif.notifier.constants.TopicTypes;
import org.rif.notifier.managers.DbManagerFacade;
import org.rif.notifier.models.entities.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.*;


@Component
public class DataProcessorJob {
    private static final Logger logger = LoggerFactory.getLogger(DataProcessorJob.class);

    @Autowired
    private DbManagerFacade dbManagerFacade;

    /**
     * Gets all raw data not processed and makes the relationship with the subscription.
     * It saves the result on the notification table, ready to be sent to the user.
     */
    @Scheduled(fixedDelayString = "${notifier.run.fixedDelayProcessJob}", initialDelayString = "${notifier.run.fixedInitialDelayProcessJob}")
    public void run() {
        //Rawdata to be marked as processed
        List<RawData> processedRows = new ArrayList<>();

        //Bring all the raw data not processed
        List<RawData> rawData = dbManagerFacade.getRawDataByProcessed(false);
        if (rawData.size() > 0) {
            logger.info(Thread.currentThread().getId() + String.format(" - Rawdata not processed = %d", rawData.size()));
            List<Notification> ntfsData = new ArrayList<>();
            List<Subscription> subscriptionsWithNotif = new ArrayList<>();
            rawData.forEach(rawDataItem -> {
                String dataForNotification = "";
                //Bring subs with notification balance also
                List<Subscription> activeSubs = dbManagerFacade.getActiveSubscriptionsByTopicIdWithBalance(rawDataItem.getIdTopic());
                for (Subscription sub : activeSubs) {
                    //Here we can add some logic to each type of event
                    switch (TopicTypes.valueOf(rawDataItem.getType())) {
                        case CONTRACT_EVENT:
                            //break;
                        case NEW_TRANSACTIONS:
                            //break;
                        case PENDING_TRANSACTIONS:
                            //break;
                        case NEW_BLOCK:
                            //break;
                        default:
                            dataForNotification = rawDataItem.getData();
                            break;
                    }
                    Date date = new Date();
                    //Add subscription to later discount the notification from balance
                    if(subscriptionsWithNotif.stream().noneMatch(subItem -> subItem.getUserAddress().equals(sub.getUserAddress()))){
                        //Just add the notification, if we're here it's because the user has at least 1 in notification balance

                        ntfsData.add(new Notification(sub, new Timestamp(date.getTime()).toString(), false, dataForNotification, rawDataItem.getIdTopic()));
                        sub.decrementNotificationBalance();
                        subscriptionsWithNotif.add(sub);
                    }else{
                        Subscription addedSub = subscriptionsWithNotif.stream().filter(item -> item.getUserAddress().equals(sub.getUserAddress()))
                                .findFirst().get();
                        //Before adding, we need to check if the sub has balance yet
                        if(addedSub.getNotificationBalance() > 0) {
                            ntfsData.add(new Notification(sub, new Timestamp(date.getTime()).toString(), false, dataForNotification, rawDataItem.getIdTopic()));
                            addedSub.decrementNotificationBalance();
                        }
                    }
                }
                if (processedRows.stream().noneMatch(item -> item.getId().equals(rawDataItem.getId())))
                    processedRows.add(rawDataItem);
            });
            logger.info(Thread.currentThread().getId() + String.format(" - Finished processing notifications, count = %s", ntfsData.size()));
            if (!ntfsData.isEmpty()) {
                List<Notification> savedNotfs = dbManagerFacade.saveNotificationBatch(ntfsData);
                //Discount notifications from subscription
                subscriptionsWithNotif.forEach(updatedSub -> {
                            dbManagerFacade.updateSubscription(updatedSub);
                        }
                );
                logger.info(Thread.currentThread().getId() + String.format(" - Saved all notifications, count = %d", savedNotfs.size()));
            }
        }
        logger.info(Thread.currentThread().getId() + String.format(" - Rawdata to mark as processed - %d", processedRows.size()));
        if (processedRows.size() > 0) {
            //Now i need to mark all processed raw data
            processedRows.forEach(item -> {
                item.setProcessed(true);
            });
            dbManagerFacade.updateRawDataBatch(processedRows);
            logger.info(Thread.currentThread().getId() + " - Rawdata settled as processed -");
        }
    }
}