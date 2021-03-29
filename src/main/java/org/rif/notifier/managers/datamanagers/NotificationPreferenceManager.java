package org.rif.notifier.managers.datamanagers;

import org.apache.commons.lang3.StringUtils;
import org.rif.notifier.helpers.EncryptHelper;
import org.rif.notifier.models.entities.DestinationParams;
import org.rif.notifier.models.entities.NotificationPreference;
import org.rif.notifier.models.entities.NotificationServiceType;
import org.rif.notifier.models.entities.Subscription;
import org.rif.notifier.repositories.NotificationPreferenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationPreferenceManager {

    private NotificationPreferenceRepository notificationPreferenceRepository;
    private EncryptHelper encryptHelper;

    public NotificationPreferenceManager(NotificationPreferenceRepository notificationPreferenceRepository, EncryptHelper encryptHelper)    {
        this.notificationPreferenceRepository = notificationPreferenceRepository;
        this.encryptHelper = encryptHelper;
    }


    public NotificationPreference saveNotificationPreference(NotificationPreference preference)   {
        encryptParams(preference.getDestinationParams());
        return notificationPreferenceRepository.save(preference);
    }

    /**
     * Encrypt the password if any, before saving the notification preferences
     * @param preferences
     * @return
     */
    public List<NotificationPreference> saveNotificationPreferences(List<NotificationPreference> preferences)   {
        preferences.stream().map(pref->pref.getDestinationParams()).forEach(this::encryptParams);
        return notificationPreferenceRepository.saveAll(preferences);
    }


    public void removeNotificationPreference(NotificationPreference preference)   {
        notificationPreferenceRepository.delete(preference);
    }

    public NotificationPreference getNotificationPreference(Subscription sub, int idTopic, NotificationServiceType type)   {
       return notificationPreferenceRepository.findBySubscriptionAndIdTopicAndNotificationService(sub, idTopic, type) ;
    }

    public NotificationPreference getNotificationPreference(Subscription sub, int idTopic, NotificationServiceType type, String destination)   {
        return notificationPreferenceRepository.findBySubscriptionAndIdTopicAndNotificationServiceAndDestination(sub, idTopic, type, destination) ;
    }

    public NotificationPreference getNotificationPreference(Subscription sub, NotificationServiceType type)   {
        return notificationPreferenceRepository.findBySubscriptionAndNotificationService(sub, type) ;
    }

    public List<NotificationPreference> getNotificationPreferences(Subscription sub, int idTopic)   {
        return notificationPreferenceRepository.findBySubscriptionAndIdTopic(sub, idTopic) ;
    }

    private void encryptParams(DestinationParams destinationParams) {
        if (destinationParams != null)  {
            String password = destinationParams.getPassword();
            String apiKey = destinationParams.getApiKey();
            destinationParams.setPassword(encryptHelper.encrypt(password));
            destinationParams.setApiKey(encryptHelper.encrypt(apiKey));
        }
    }
}
