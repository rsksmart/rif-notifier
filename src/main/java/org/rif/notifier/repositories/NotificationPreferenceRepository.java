package org.rif.notifier.repositories;

import org.rif.notifier.models.entities.Notification;
import org.rif.notifier.models.entities.NotificationPreference;
import org.rif.notifier.models.entities.NotificationServiceType;
import org.rif.notifier.models.entities.Subscription;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Integer> {
    public NotificationPreference findBySubscriptionAndIdTopicAndNotificationService(Subscription sub, int idTopic, NotificationServiceType type);
    public NotificationPreference findBySubscriptionAndNotificationService(Subscription sub, NotificationServiceType type);
    List<NotificationPreference> findBySubscriptionAndIdTopic(Subscription subscription, Integer idTopic);
    public NotificationPreference findBySubscriptionAndIdTopicAndNotificationServiceAndDestination(Subscription sub, int idTopic, NotificationServiceType type, String destination);
}

