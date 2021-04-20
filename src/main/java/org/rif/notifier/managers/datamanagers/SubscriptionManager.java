package org.rif.notifier.managers.datamanagers;

import org.rif.notifier.models.entities.Subscription;
import org.rif.notifier.models.entities.SubscriptionPlan;
import org.rif.notifier.models.entities.SubscriptionPrice;
import org.rif.notifier.models.entities.SubscriptionStatus;
import org.rif.notifier.repositories.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.time.LocalDate.now;

@Service
public class SubscriptionManager {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionManager.class);

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    public List<Subscription> getAllActiveSubscriptionsWithBalance(){
        List<Subscription> lst = new ArrayList<>();
        subscriptionRepository.findByActiveWithBalance().forEach(lst::add);
        return lst;
    }

    public List<Subscription> getActiveSubscriptions(){
        List<Subscription> lst = new ArrayList<>();
        subscriptionRepository.findByStatus(SubscriptionStatus.ACTIVE).forEach(lst::add);
        return lst;
    }

    public List<Subscription> getActiveSubscriptionsByTopicId(int idTopic){
        List<Subscription> lst = new ArrayList<>();
        subscriptionRepository.findByIdTopicAndSubscriptionActive(idTopic).forEach(lst::add);
        return lst;
    }

    public List<Subscription> getActiveSubscriptionsByTopicIdWithBalance(int idTopic){
        List<Subscription> lst = new ArrayList<>();
        subscriptionRepository.findByIdTopicAndSubscriptionActiveAndPositiveBalance(idTopic).forEach(lst::add);
        return lst;
    }

    public List<Subscription> findByContractAddressAndSubscriptionActive(String address){
        List<Subscription> lst = new ArrayList<>();
        subscriptionRepository.findByContractAddressAndSubscriptionActive(address).forEach(lst::add);
        return lst;
    }

    public List<Subscription> getSubscriptionByAddress(String user_address){
        return subscriptionRepository.findByUserAddress(user_address);
    }

    public List<Subscription> getSubscriptionByAddressAndSubscriptionPlan(String user_address, SubscriptionPlan subscriptionPlan){
        return subscriptionRepository.findByUserAddressAndSubscriptionPlan(user_address, subscriptionPlan);
    }

    public List<Subscription> getActiveSubscriptionByAddress(String user_address){
        return subscriptionRepository.findByUserAddressAndStatus(user_address, SubscriptionStatus.ACTIVE);
    }

    public Subscription getActiveSubscriptionByAddressAndType(String user_address, SubscriptionPlan subscriptionPlan){
        return subscriptionRepository.findByUserAddressAndSubscriptionPlanAndStatus(user_address, subscriptionPlan, SubscriptionStatus.ACTIVE);
    }

    public Subscription getSubscriptionByHash(String hash){
        return subscriptionRepository.findByHash(hash);
    }

    public Subscription getSubscriptionByHashAndUserAddress(String hash, String userAddress){
        return subscriptionRepository.findByHashAndUserAddress(hash, userAddress);
    }

    public List<Subscription> getSubscriptionByHashListAndUserAddress(List<String> hashList, String userAddress) {
        return subscriptionRepository.findByHashInAndUserAddress(hashList, userAddress);
    }

    public Subscription getSubscriptionByPreviousSubscription(Subscription prev){
        return subscriptionRepository.findByPreviousSubscription(prev);
    }

    public List<Subscription> getPendingSubscriptions() {
        return subscriptionRepository.findPendingSubscriptions();
    }

    public int getExpiredSubscriptionsCount(){
        return subscriptionRepository.countExpiredSubscriptions();
    }

    public int updateExpiredSubscriptions() {
        return subscriptionRepository.updateExpiredSubscriptions();
    }

    public Subscription insert(Date activeUntil, String userAddress, SubscriptionPlan subscriptionPlan, SubscriptionStatus status, SubscriptionPrice subscriptionPrice) {
        Subscription sub = new Subscription(activeUntil, userAddress, subscriptionPlan, status);
        sub.setCurrency(subscriptionPrice.getCurrency());
        sub.setPrice(subscriptionPrice.getPrice());
        sub.setHash(String.valueOf(sub.hashCode()));
        sub.setExpirationDate(java.sql.Date.valueOf(now().plusDays(sub.getSubscriptionPlan().getValidity())));
        Subscription result = subscriptionRepository.save(sub);
        return result;
    }

    public Subscription update(Subscription sub) {
        Subscription result = subscriptionRepository.save(sub);
        return result;
    }
}
