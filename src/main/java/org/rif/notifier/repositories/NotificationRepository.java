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
public interface NotificationRepository extends JpaRepository<Notification, String> {
    List<Notification> findAllBySubscriptionAndIdGreaterThanAndIdTopicIn(Subscription subscription, Integer id, Set<Integer> idTopic, Pageable pageable);

    //@Query(value = "SELECT * FROM notification A WHERE A.to_address = ?1 AND A.id > ?2", nativeQuery = true)
    List<Notification> findAllBySubscriptionAndIdGreaterThan(Subscription subscription, Integer id, Pageable pageable);

    List<Notification> findAllBySubscriptionAndIdTopicIn(Subscription subscription, Set<Integer> idTopic, Pageable pageable);

    List<Notification> findAllBySubscription(Subscription subscription, Pageable pageable);

    List<Notification> findAllBySentFalseAndNotificationLogs_RetryCountLessThan(int count);
    List<Notification> findAllBySentFalseAndSubscription_ActiveTrueAndNotificationLogs_RetryCountLessThan(int count);

}
