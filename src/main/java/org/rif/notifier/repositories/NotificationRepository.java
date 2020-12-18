package org.rif.notifier.repositories;

import org.rif.notifier.models.entities.Notification;
import org.rif.notifier.models.entities.Subscription;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findAllBySubscriptionAndIdGreaterThanAndIdTopicIn(Subscription subscription, Integer id, Set<Integer> idTopic, Pageable pageable);

    //@Query(value = "SELECT * FROM notification A WHERE A.to_address = ?1 AND A.id > ?2", nativeQuery = true)
    List<Notification> findAllBySubscriptionAndIdGreaterThan(Subscription subscription, Integer id, Pageable pageable);

    List<Notification> findAllBySubscriptionAndIdTopicIn(Subscription subscription, Set<Integer> idTopic, Pageable pageable);

    List<Notification> findAllBySubscription(Subscription subscription, Pageable pageable);

    List<Notification> findAllBySentFalseAndNotificationLogs_RetryCountLessThan(int count);

    @Query(value="SELECT * FROM notification n LEFT JOIN notification_log nl ON nl.notification_id=n.id JOIN subscription s ON s.user_address = n.to_address AND s.active=TRUE WHERE n.sent=FALSE AND (nl.retry_count IS NULL OR nl.retry_count < ?1) AND (nl.sent IS NULL OR nl.sent=FALSE)", nativeQuery = true)
    Set<Notification> findUnsentNotifications(int retryCount);


}
