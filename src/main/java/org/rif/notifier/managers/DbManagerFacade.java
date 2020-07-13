package org.rif.notifier.managers;

import org.rif.notifier.constants.TopicParamTypes;
import org.rif.notifier.constants.TopicTypes;
import org.rif.notifier.managers.datamanagers.*;
import org.rif.notifier.models.entities.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DbManagerFacade {
    private static final Logger logger = LoggerFactory.getLogger(DbManagerFacade.class);

    @Autowired
    private RawDataManager rawDataManager;

    @Autowired
    private SubscriptionManager subscriptionManager;

    @Autowired
    private SubscriptionTypeManager subscriptionTypeManager;

    @Autowired
    private TopicManager topicManager;

    @Autowired
    private TopicParamsManager topicParamsManager;

    @Autowired
    private NotifEntityManager notifEntityManager;

    @Autowired
    private ChainAddressManager chainAddressManager;

    @Autowired
    private UserManager userManager;

    @Autowired
    private DataFetcherManager dataFetcherManager;

    public RawData saveRawData(String type, String data, boolean processed, BigInteger block, int idTopic, int hashcode){
        return rawDataManager.insert(type,data,processed, block, idTopic, hashcode);
    }

    @Transactional
    public List<RawData> saveRawDataBatch(List<RawData> rawData){
        return rawData.stream().map(rawData1 -> rawDataManager.insert(rawData1.getType(), rawData1.getData(), rawData1.isProcessed(), rawData1.getBlock(), rawData1.getIdTopic(), rawData1.getHashcode())).collect(Collectors.toList());
    }

    @Transactional
    public List<RawData> updateRawDataBatch(List<RawData> rawData){
        return rawData.stream().map(rawData1 -> rawDataManager.update(rawData1.getId(), rawData1.getType(), rawData1.getData(), rawData1.isProcessed(), rawData1.getBlock(), rawData1.getIdTopic())).collect(Collectors.toList());
    }

    public List<RawData> getAllRawData(){
        return rawDataManager.getAllRawData();
    }

    public List<RawData> getRawDataByType(String type){
        return rawDataManager.getRawDataByType(type);
    }

    public List<RawData> getRawDataByProcessed(boolean processed){
        return rawDataManager.getRawDataByProcessed(processed);
    }

    public List<RawData> getRawDataByTypeAndProcessed(String type, boolean processed){
        return rawDataManager.getRawDataByTypeAndProcessed(type, processed);
    }

    public List<RawData> getRawDataFilteredByTopic(){
        return new ArrayList<>();
    }

    public List<Subscription> getAllActiveSubscriptions(){
        return subscriptionManager.getActiveSubscriptions();
    }

    public List<Subscription> getAllActiveSubscriptionsWithBalance(){
        return subscriptionManager.getAllActiveSubscriptionsWithBalance();
    }

    public List<Subscription> getActiveSubscriptionsByTopicId(int idTopic){
        return subscriptionManager.getActiveSubscriptionsByTopicId(idTopic);
    }

    public List<Subscription> getActiveSubscriptionsByTopicIdWithBalance(int idTopic){
        return subscriptionManager.getActiveSubscriptionsByTopicIdWithBalance(idTopic);
    }

    public List<Subscription> findByContractAddressAndSubscriptionActive(String address){
        return subscriptionManager.findByContractAddressAndSubscriptionActive(address);
    }

    public Subscription getActiveSubscriptionByAddress(String user_address){
        return subscriptionManager.getActiveSubscriptionByAddress(user_address);
    }

    public Subscription getSubscriptionByAddress(String user_address){
        return subscriptionManager.getSubscriptionByAddress(user_address);
    }

    public Subscription createSubscription(Date activeUntil, String userAddress, SubscriptionType type, String state) {
        return subscriptionManager.insert(activeUntil, userAddress, type, state);
    }

    public Subscription updateSubscription(Subscription sub) {
        return subscriptionManager.update(sub);
    }

    public SubscriptionType getSubscriptionTypeByType(int id){ return  subscriptionTypeManager.getSubscriptionTypeById(id); }

    public Topic getTopicById(int Id){
        return topicManager.getTopicById(Id);
    }

    public Topic getTopicByHashCode(int hash){
        return topicManager.getTopicByHashCode(hash);
    }

    public RawData getRawdataByHashcode(int hashcode){
        return rawDataManager.getRawdataByHashcode(hashcode);
    }

    public Topic getTopicByHashCodeAndIdSubscription(int hash, int idSubscription){
        return topicManager.getTopicByHashCodeAndIdSubscription(hash, idSubscription);
    }

    public Topic saveTopic(TopicTypes type, String hash, Subscription sub){
        return topicManager.insert(type, hash, sub);
    }

    public Topic updateTopic(Topic tp){
        return topicManager.update(tp);
    }

    public TopicParams saveTopicParams(Topic topic, TopicParamTypes type, String value, int order, String valueType, boolean indexed, String filter){
        return topicParamsManager.insert(topic, type, value, order, valueType, indexed, filter);
    }

    @Transactional
    public List<Notification> saveNotificationBatch(List<Notification> notifications){
        return notifications.stream().map(notificationItem ->
                notifEntityManager.insert(notificationItem.getTo_address(), notificationItem.getTimestamp(), notificationItem.isSended(), notificationItem.getData(), notificationItem.getIdTopic())
        ).collect(Collectors.toList());
    }

    public List<Notification> getNotificationByUserAddress(String user_address, Integer id, Integer lastRows, Set<Integer> idTopics){
        if(id != null && lastRows != null && idTopics != null && idTopics.size() > 0)
            return notifEntityManager.getNotificationsByUserAddressAndIdAndIdTopicsWithLastRows(user_address, id, lastRows, idTopics);
        else if(lastRows == null && id != null && idTopics != null && idTopics.size() > 0)
            return notifEntityManager.getNotificationsByUserAddressAndIdGraterThanAndIdTopic(user_address, id, idTopics);
        else if(id != null && lastRows != null)
            return notifEntityManager.getNotificationsByUserAddressAndIdGraterThanWithLastRows(user_address, id, lastRows);
        else if(lastRows != null && idTopics != null && idTopics.size() > 0)
            return notifEntityManager.getNotificationsByUserAddressIdTopicIn(user_address, idTopics, lastRows);
        else if(id != null)
            return notifEntityManager.getNotificationsByUserAddressAndIdGraterThan(user_address, id);
        else if(lastRows != null)
            return notifEntityManager.getNotificationsByUserAddressWithLastRows(user_address, lastRows);
        else if(idTopics != null && idTopics.size() > 0)
            return notifEntityManager.getNotificationsByUserAddressAndIdTopic(user_address, idTopics);
        else
            return notifEntityManager.getNotificationsByUserAddress(user_address);
    }

    public List<ChainAddressEvent> getChainAddresses(String nodehash, Set<String> eventName){
        if(nodehash != null && eventName != null && eventName.size() > 0) {
            return chainAddressManager.getChainAddressesByNodehashAndEventname(nodehash, eventName);
        } else if(nodehash != null) {
            return chainAddressManager.getChainAddressesByNodehash(nodehash);
        } else if(eventName != null && eventName.size() > 0) {
            return chainAddressManager.getChainAddressesByEventname(eventName);
        } else {
            return chainAddressManager.getChainAddresses();
        }
    }

    public ChainAddressEvent getChainAddressEventByHashcode(int hash){
        return chainAddressManager.getChainAddressEventByHashcode(hash);
    }

    public User saveUser(String address, String apiKey){
        return userManager.insert(address, apiKey);
    }

    public User getUserByApiKey(String apiKey){
        return userManager.getUserByApikey(apiKey);
    }

    public User getUserByAddress(String address){
        return userManager.getUserByAddress(address);
    }

    public DataFetcherEntity saveLastBlock(BigInteger lastBlock){
        return dataFetcherManager.insert(lastBlock);
    }

    public DataFetcherEntity saveLastBlockChainAdddresses(BigInteger lastBlock){
        return dataFetcherManager.insertLastBlockChainAddress(lastBlock);
    }

    public BigInteger getLastBlock(){
        return dataFetcherManager.get();
    }

    public BigInteger getLastBlockForChainAddresses(){
        return dataFetcherManager.getBlockChainAddresses();
    }

    @Transactional
    public List<ChainAddressEvent> saveChainAddressesEvents(List<ChainAddressEvent> chainAddressEvents){
        return chainAddressEvents.stream().map(chainAddressEvent ->
                chainAddressManager.insert(chainAddressEvent.getNodehash(), chainAddressEvent.getEventName(), chainAddressEvent.getChain(), chainAddressEvent.getAddress(), chainAddressEvent.getHashChainaddress())
        ).collect(Collectors.toList());
    }
}
