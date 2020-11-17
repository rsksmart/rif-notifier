package org.rif.notifier.repositories;

import org.rif.notifier.models.entities.NotificationPreference;
import org.rif.notifier.models.entities.NotificationServiceType;
import org.rif.notifier.models.entities.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Integer> {
    public NotificationPreference findBySubscriptionAndIdTopicAndNotificationService(Subscription sub, int idTopic, NotificationServiceType type);
    public NotificationPreference findBySubscriptionAndNotificationService(Subscription sub, NotificationServiceType type);
}

