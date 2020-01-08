package org.rif.notifier.managers.datamanagers;

import org.rif.notifier.models.entities.Notification;
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
public class NotifEntityManager {
    @Autowired
    private NotificationRepository notificationRepository;

    @Value("${notifier.notifications.maxquerylimit}")
    private int MAX_LIMIT_QUERY;

    public Notification insert(String to_address, String timestamp, boolean sended, String data, int idTopic){
        Notification ntf = new Notification(to_address, timestamp, sended, data, idTopic);
        Notification result = notificationRepository.save(ntf);
        return result;

    }

    public List<Notification> getNotificationsByUserAddressAndIdAndIdTopicsWithLastRows(String user_address, Integer id, Integer lastRows, Set<Integer> idTopics){
        Pageable pageable = PageRequest.of(0, MAX_LIMIT_QUERY < lastRows ? MAX_LIMIT_QUERY : lastRows, Sort.by("id").descending());
        return new ArrayList<>(notificationRepository.findAllByToAddressAndIdGreaterThanAndIdTopicIn(user_address, id, idTopics, pageable));
    }
    public List<Notification> getNotificationsByUserAddressAndIdGraterThanAndIdTopic(String user_address, Integer id, Set<Integer> idTopics){
        Pageable DEFAULT_PAGEABLE = PageRequest.of(0, MAX_LIMIT_QUERY, Sort.by("id").descending());
        return new ArrayList<>(notificationRepository.findAllByToAddressAndIdGreaterThanAndIdTopicIn(user_address, id, idTopics, DEFAULT_PAGEABLE));
    }
    public List<Notification> getNotificationsByUserAddressAndIdGraterThanWithLastRows(String user_address, Integer id, Integer lastRows){
        Pageable pageable = PageRequest.of(0, MAX_LIMIT_QUERY < lastRows ? MAX_LIMIT_QUERY : lastRows, Sort.by("id").descending());
        return new ArrayList<>(notificationRepository.findAllByToAddressAndIdGraterThan(user_address, id, pageable));
    }
    public List<Notification> getNotificationsByUserAddressIdTopicIn(String user_address, Set<Integer> idTopics, Integer lastRows){
        Pageable pageable = PageRequest.of(0, MAX_LIMIT_QUERY < lastRows ? MAX_LIMIT_QUERY : lastRows, Sort.by("id").descending());
        return new ArrayList<>(notificationRepository.findAllByToAddressAndIdTopicIn(user_address, idTopics, pageable));
    }
    public List<Notification> getNotificationsByUserAddressAndIdGraterThan(String user_address, Integer id){
        Pageable DEFAULT_PAGEABLE = PageRequest.of(0, MAX_LIMIT_QUERY, Sort.by("id").descending());
        return new ArrayList<>(notificationRepository.findAllByToAddressAndIdGraterThan(user_address, id, DEFAULT_PAGEABLE));
    }
    public List<Notification> getNotificationsByUserAddressWithLastRows(String user_address, Integer lastRows){
        Pageable pageable = PageRequest.of(0, MAX_LIMIT_QUERY < lastRows ? MAX_LIMIT_QUERY : lastRows, Sort.by("id").descending());
        return new ArrayList<>(notificationRepository.findAllByToAddress(user_address, pageable));
    }
    public List<Notification> getNotificationsByUserAddressAndIdTopic(String user_address, Set<Integer> idTopics){
        Pageable DEFAULT_PAGEABLE = PageRequest.of(0, MAX_LIMIT_QUERY, Sort.by("id").descending());
        return new ArrayList<>(notificationRepository.findAllByToAddressAndIdTopicIn(user_address, idTopics, DEFAULT_PAGEABLE));
    }
    public List<Notification> getNotificationsByUserAddress(String user_address){
        Pageable DEFAULT_PAGEABLE = PageRequest.of(0, MAX_LIMIT_QUERY, Sort.by("id").descending());
        return new ArrayList<>(notificationRepository.findAllByToAddress(user_address, DEFAULT_PAGEABLE));
    }
}
