package org.rif.notifier.managers;

import org.rif.notifier.models.entities.Notification;
import org.rif.notifier.models.entities.NotificationPreference;
import org.rif.notifier.models.entities.Subscription;
import org.rif.notifier.managers.services.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class NotificationManager {

    private static final Logger logger = LoggerFactory.getLogger(NotificationManager.class);

    @Autowired
    private DbManagerFacade dbManagerFacade;

    /**
     * Given an Address gets all the notifications
     * @param address Address of a user to be notified
     * @return List<Notification> with all the notifications of a user address
     */
    public List<Notification> getNotificationsForAddress(String address, Integer id, Integer lastRows, Set<Integer> idTopic){
        List<Notification> lst = new ArrayList<>();
        Subscription sub = dbManagerFacade.getActiveSubscriptionByAddress(address);
        if(sub != null) {
            //This will need to be migrated by topic
            if (sub.getActive() )
                lst = dbManagerFacade.getNotificationByUserAddress(address, id, lastRows, idTopic);
        }
        return lst;
    }

    /**
     * First implementation of a notificateUsers method, that will be called to send all notification by preferences indicated previously by the end-user
     */
    public void notificateUsers(){
        List<Subscription> activeSubs = dbManagerFacade.getAllActiveSubscriptions();
        for(Subscription sub : activeSubs){
            List<Notification> notifications = dbManagerFacade.getNotificationByUserAddress(sub.getUserAddress(), null, null, null);
            for(NotificationPreference preference : sub.getNotificationPreferences()){
                //TODO: write notification code
            }
        }
    }
}
