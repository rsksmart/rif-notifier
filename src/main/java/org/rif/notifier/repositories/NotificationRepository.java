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

    /**
     * This method pulls unsent notifications 'ACTIVE' and 'COMPLETED' subscriptions. Since the status turns to 'COMPLETED' as soon as the
     * notification balance is zero, we also fetch unsent notifications for 'COMPLETED' subscriptions due to the fact the notifier
     * can retry failed notifications as many times as the maxretries specified in configuration
     * @param retryCount
     * @return
     */
    @Query(value="SELECT * FROM notification n LEFT JOIN notification_log nl ON nl.notification_id=n.id JOIN subscription s ON s.id = n.subscription_id AND s.status not in ('PENDING', 'EXPIRED') WHERE n.sent=FALSE AND (nl.retry_count IS NULL OR nl.retry_count < ?1) AND (nl.sent IS NULL OR nl.sent=FALSE)", nativeQuery = true)
    Set<Notification> findUnsentNotifications(int retryCount);

    /**
     * Retrieves the count of unsent notifications for the given subscription provided the retry count for the notifications is the less than
     * the specified retrycount
     * @param subscriptionId
     * @param retryCount
     * @return
     */
    @Query(value="SELECT COUNT(1) FROM notification n LEFT JOIN notification_log nl ON nl.notification_id=n.id JOIN subscription s ON s.id = n.subscription_id AND s.status='ACTIVE' AND s.id=?1 WHERE n.sent=FALSE AND (nl.retry_count IS NULL OR nl.retry_count < ?2) AND (nl.sent IS NULL OR nl.sent=FALSE)", nativeQuery = true)
    int countUnsentNotifications(int subscriptionId, int retryCount);


}
