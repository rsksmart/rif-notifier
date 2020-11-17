package org.rif.notifier.managers.datamanagers;

import org.rif.notifier.models.entities.NotificationPreference;
import org.rif.notifier.models.entities.NotificationServiceType;
import org.rif.notifier.models.entities.Subscription;
import org.rif.notifier.repositories.NotificationPreferenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationPreferenceManager {
    @Autowired
    NotificationPreferenceRepository notificationPreferenceRepository;

    public NotificationPreference saveNotificationPreference(NotificationPreference preference)   {
        return notificationPreferenceRepository.save(preference);
    }

    public NotificationPreference getNotificationPreference(Subscription sub, int idTopic, NotificationServiceType type)   {
       return notificationPreferenceRepository.findBySubscriptionAndIdTopicAndNotificationService(sub, idTopic, type) ;
    }

    public NotificationPreference getNotificationPreference(Subscription sub, NotificationServiceType type)   {
        return notificationPreferenceRepository.findBySubscriptionAndNotificationService(sub, type) ;
    }
}
