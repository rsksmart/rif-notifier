package org.rif.notifier.managers.datamanagers;

import org.rif.notifier.constants.TopicTypes;
import org.rif.notifier.models.entities.Subscription;
import org.rif.notifier.models.entities.Topic;
import org.rif.notifier.repositories.TopicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class TopicManager {
    @Autowired
    private TopicRepository topicRepository;

    public Topic getTopicById(int Id){
        return topicRepository.findById(Id);
    }

    public Topic getTopicByHashCode(int hash){
        return topicRepository.findByHash(String.valueOf(hash));
    }

    public Topic getTopicByHashCodeAndIdSubscription(int hash, int idSubscription){
        return topicRepository.findByHashAndIdSubscription(String.valueOf(hash), idSubscription);
    }

    public Set<Topic> getAllTopicsWithActiveSubscriptionAndBalance()    {
        return topicRepository.findAllTopicsWithActiveSubscriptionWithBalance();
    }

    public Topic insert(TopicTypes type, String hash, Subscription sub){
        Topic tp = new Topic(type, hash, sub);
        return topicRepository.save(tp);
    }

    public Topic update(Topic tp){
        return topicRepository.save(tp);
    }
}
