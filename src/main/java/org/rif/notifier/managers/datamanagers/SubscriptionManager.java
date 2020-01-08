package org.rif.notifier.managers.datamanagers;

import org.rif.notifier.models.entities.Subscription;
import org.rif.notifier.models.entities.SubscriptionType;
import org.rif.notifier.repositories.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class SubscriptionManager {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionManager.class);

    @Autowired
    private SubscriptionRepository subscriptionRepositorty;

    public List<Subscription> getAllActiveSubscriptionsWithBalance(){
        List<Subscription> lst = new ArrayList<>();
        subscriptionRepositorty.findByActiveWithBalance().forEach(lst::add);
        return lst;
    }

    public List<Subscription> getActiveSubscriptions(){
        List<Subscription> lst = new ArrayList<>();
        subscriptionRepositorty.findByActive(1).forEach(lst::add);
        return lst;
    }

    public List<Subscription> getActiveSubscriptionsByTopicId(int idTopic){
        List<Subscription> lst = new ArrayList<>();
        subscriptionRepositorty.findByIdTopicAndSubscriptionActive(idTopic).forEach(lst::add);
        return lst;
    }

    public List<Subscription> getActiveSubscriptionsByTopicIdWithBalance(int idTopic){
        List<Subscription> lst = new ArrayList<>();
        subscriptionRepositorty.findByIdTopicAndSubscriptionActiveAndPositiveBalance(idTopic).forEach(lst::add);
        return lst;
    }

    public List<Subscription> findByContractAddressAndSubscriptionActive(String address){
        List<Subscription> lst = new ArrayList<>();
        subscriptionRepositorty.findByContractAddressAndSubscriptionActive(address).forEach(lst::add);
        return lst;
    }

    public Subscription getSubscriptionByAddress(String user_address){
        return subscriptionRepositorty.findByUserAddress(user_address);
    }

    public Subscription getActiveSubscriptionByAddress(String user_address){
        return subscriptionRepositorty.findByUserAddressAndActive(user_address, true);
    }

    public Subscription insert(Date activeUntil, String userAddress, SubscriptionType type, String state) {
        Subscription sub = new Subscription(activeUntil, userAddress, type, state);
        Subscription result = subscriptionRepositorty.save(sub);
        return result;
    }

    public Subscription update(Subscription sub) {
        Subscription result = subscriptionRepositorty.save(sub);
        return result;
    }
}
