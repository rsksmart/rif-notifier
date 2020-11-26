package org.rif.notifier.managers.datamanagers;

import org.rif.notifier.models.entities.Notification;
import org.rif.notifier.models.entities.Subscription;
import org.rif.notifier.repositories.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
public class NotificationManager {
    @Autowired
    private NotificationRepository notificationRepository;

    @Value("${notifier.notifications.maxquerylimit}")
    private int MAX_LIMIT_QUERY;

    public Notification insert(Subscription sub, String timestamp, boolean sent, String data, int idTopic){
        Notification ntf = new Notification(sub, timestamp, sent, data, idTopic);
        Notification result = notificationRepository.save(ntf);
        return result;
    }

    public Notification saveNotification(Notification notification) {
        return notificationRepository.save(notification);
    }

    public List<Notification> getNotificationsByUserAddressAndIdAndIdTopicsWithLastRows(Subscription subscription, Integer id, Integer lastRows, Set<Integer> idTopics){
        Pageable pageable = PageRequest.of(0, MAX_LIMIT_QUERY < lastRows ? MAX_LIMIT_QUERY : lastRows, Sort.by("id").descending());
        return new ArrayList<>(notificationRepository.findAllBySubscriptionAndIdGreaterThanAndIdTopicIn(subscription, id, idTopics, pageable));
    }
    public List<Notification> getNotificationsByUserAddressAndIdGraterThanAndIdTopic(Subscription subscription, Integer id, Set<Integer> idTopics){
        Pageable DEFAULT_PAGEABLE = PageRequest.of(0, MAX_LIMIT_QUERY, Sort.by("id").descending());
        return new ArrayList<>(notificationRepository.findAllBySubscriptionAndIdGreaterThanAndIdTopicIn(subscription, id, idTopics, DEFAULT_PAGEABLE));
    }
    public List<Notification> getNotificationsByUserAddressAndIdGraterThanWithLastRows(Subscription subscription, Integer id, Integer lastRows){
        Pageable pageable = PageRequest.of(0, MAX_LIMIT_QUERY < lastRows ? MAX_LIMIT_QUERY : lastRows, Sort.by("id").descending());
        return new ArrayList<>(notificationRepository.findAllBySubscriptionAndIdGreaterThan(subscription, id, pageable));
    }
    public List<Notification> getNotificationsByUserAddressIdTopicIn(Subscription subscription, Set<Integer> idTopics, Integer lastRows){
        Pageable pageable = PageRequest.of(0, MAX_LIMIT_QUERY < lastRows ? MAX_LIMIT_QUERY : lastRows, Sort.by("id").descending());
        return new ArrayList<>(notificationRepository.findAllBySubscriptionAndIdTopicIn(subscription, idTopics, pageable));
    }
    public List<Notification> getNotificationsByUserAddressAndIdGraterThan(Subscription subscription, Integer id){
        Pageable DEFAULT_PAGEABLE = PageRequest.of(0, MAX_LIMIT_QUERY, Sort.by("id").descending());
        return new ArrayList<>(notificationRepository.findAllBySubscriptionAndIdGreaterThan(subscription, id, DEFAULT_PAGEABLE));
    }
    public List<Notification> getNotificationsByUserAddressWithLastRows(Subscription subscription, Integer lastRows){
        Pageable pageable = PageRequest.of(0, MAX_LIMIT_QUERY < lastRows ? MAX_LIMIT_QUERY : lastRows, Sort.by("id").descending());
        return new ArrayList<>(notificationRepository.findAllBySubscription(subscription, pageable));
    }
    public List<Notification> getNotificationsByUserAddressAndIdTopic(Subscription subscription, Set<Integer> idTopics){
        Pageable DEFAULT_PAGEABLE = PageRequest.of(0, MAX_LIMIT_QUERY, Sort.by("id").descending());
        return new ArrayList<>(notificationRepository.findAllBySubscriptionAndIdTopicIn(subscription, idTopics, DEFAULT_PAGEABLE));
    }
    public List<Notification> getNotificationsByUserAddress(Subscription subscription){
        Pageable DEFAULT_PAGEABLE = PageRequest.of(0, MAX_LIMIT_QUERY, Sort.by("id").descending());
        return new ArrayList<>(notificationRepository.findAllBySubscription(subscription, DEFAULT_PAGEABLE));
    }

    public Set<Notification> getUnsentNotificationsWithActiveSubscription(int maxRetries) {
        return notificationRepository.findAllBySentFalseAndSubscription_ActiveTrueAndNotificationLogs_SentFalseAndNotificationLogs_RetryCountLessThan(maxRetries);
    }

    public List<Notification> getUnsentNotifications(int maxRetries) {
        return notificationRepository.findAllBySentFalseAndNotificationLogs_RetryCountLessThan(maxRetries);
    }
}
