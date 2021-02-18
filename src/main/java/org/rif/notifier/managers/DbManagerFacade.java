package org.rif.notifier.managers;

import org.rif.notifier.boot.configuration.NotifierConfig;
import org.rif.notifier.constants.TopicParamTypes;
import org.rif.notifier.constants.TopicTypes;
import org.rif.notifier.managers.datamanagers.*;
import org.rif.notifier.managers.datamanagers.NotificationManager;
import org.rif.notifier.models.entities.*;
import org.rif.notifier.models.entities.Currency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.abi.datatypes.Address;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DbManagerFacade {
    private static final Logger logger = LoggerFactory.getLogger(DbManagerFacade.class);

    @Autowired
    NotifierConfig notifierConfig;

    @Autowired
    private RawDataManager rawDataManager;

    @Autowired
    private SubscriptionManager subscriptionManager;

    @Autowired
    private SubscriptionPlanManager subscriptionPlanManager;

    @Autowired
    private TopicManager topicManager;

    @Autowired
    private TopicParamsManager topicParamsManager;

    @Autowired
    private NotificationManager notificationManager;

    @Autowired
    private ChainAddressManager chainAddressManager;

    @Autowired
    private UserManager userManager;

    @Autowired
    private DataFetcherManager dataFetcherManager;

    @Autowired
    private NotificationLogManager notificationLogManager;

    @Autowired
    private NotificationPreferenceManager notificationPreferenceManager;

    @Autowired
    private CurrencyManager currencyManager;

    public RawData saveRawData(String type, String data, boolean processed, BigInteger block, int idTopic, int hashcode){
        return rawDataManager.insert(type,data,processed, block, idTopic, hashcode);
    }

    @Transactional
    public List<RawData> saveRawDataBatch(List<RawData> rawData){
        return rawData.stream().map(rawData1 -> rawDataManager.insert(rawData1.getType(), rawData1.getData(), rawData1.isProcessed(), rawData1.getBlock(), rawData1.getIdTopic(), rawData1.getRowhashcode())).collect(Collectors.toList());
    }

    @Transactional
    public List<RawData> updateRawDataBatch(List<RawData> rawData){
        return rawData.stream().map(rawData1 -> rawDataManager.update(rawData1.getId(), rawData1.getType(), rawData1.getData(), rawData1.isProcessed(), rawData1.getBlock(), rawData1.getIdTopic(), rawData1.getRowhashcode())).collect(Collectors.toList());
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

    public Set<Topic> getAllTopicsWithActiveSubscriptionAndBalance(){
        return topicManager.getAllTopicsWithActiveSubscriptionAndBalance();
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

    public List<Subscription> getActiveSubscriptionByAddress(String user_address){
        return subscriptionManager.getActiveSubscriptionByAddress(user_address);
    }

    public Subscription getActiveSubscriptionByAddressAndType(String user_address, SubscriptionPlan subscriptionPlan){
        return subscriptionManager.getActiveSubscriptionByAddressAndType(user_address, subscriptionPlan);
    }

    public List<Subscription> getSubscriptionByAddress(String user_address){
        return subscriptionManager.getSubscriptionByAddress(user_address);
    }

    public Subscription getSubscriptionByAddressAndType(String user_address, SubscriptionPlan subscriptionPlan){
        return subscriptionManager.getSubscriptionByAddressAndSubscriptionPlan(user_address, subscriptionPlan);
    }

    public Subscription getSubscriptionByHash(String hash){
        return subscriptionManager.getSubscriptionByHash(hash);
    }

    public Subscription getSubscriptionByPreviousSubscription(Subscription prev){
        return subscriptionManager.getSubscriptionByPreviousSubscription(prev);
    }

    public List<Subscription> getPendingSubscriptions() {
        return subscriptionManager.getPendingSubscriptions();
    }

    public int getExpiredSubscriptionsCount()   {
        return subscriptionManager.getExpiredSubscriptionsCount();
    }

    @Transactional
    public int updateExpiredSubscriptions() {
        return subscriptionManager.updateExpiredSubscriptions();
    }

    @Transactional
    public Subscription createSubscription(Date activeUntil, String userAddress, SubscriptionPlan subscriptionPlan, SubscriptionStatus subscriptionStatus, SubscriptionPrice subscriptionPrice) {
        return subscriptionManager.insert(activeUntil, userAddress, subscriptionPlan, subscriptionStatus, subscriptionPrice);
    }

    public Subscription updateSubscription(Subscription sub) {
        return subscriptionManager.update(sub);
    }

    public SubscriptionPlan getSubscriptionPlanById(int id){ return  subscriptionPlanManager.getSubscriptionPlanById(id); }

    public Optional<SubscriptionPlan> getActiveSubscriptionPlanById(int id){ return  subscriptionPlanManager.getActiveSubscriptionPlanById(id); }

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
                notificationManager.insert(notificationItem.getSubscription(), notificationItem.getTimestamp(), notificationItem.isSent(), notificationItem.getData(), notificationItem.getIdTopic())
        ).collect(Collectors.toList());
    }

    @Transactional
    public Notification saveNotification(Notification notification){
        return notificationManager.saveNotification(notification);
    }

    public List<Notification> getNotificationsBySubscription(Subscription subscription, Integer id, Integer lastRows, Set<Integer> idTopics){
        if(id != null && lastRows != null && idTopics != null && idTopics.size() > 0)
            return notificationManager.getNotificationsByUserAddressAndIdAndIdTopicsWithLastRows(subscription, id, lastRows, idTopics);
        else if(lastRows == null && id != null && idTopics != null && idTopics.size() > 0)
            return notificationManager.getNotificationsByUserAddressAndIdGraterThanAndIdTopic(subscription, id, idTopics);
        else if(id != null && lastRows != null)
            return notificationManager.getNotificationsByUserAddressAndIdGraterThanWithLastRows(subscription, id, lastRows);
        else if(lastRows != null && idTopics != null && idTopics.size() > 0)
            return notificationManager.getNotificationsByUserAddressIdTopicIn(subscription, idTopics, lastRows);
        else if(id != null)
            return notificationManager.getNotificationsByUserAddressAndIdGraterThan(subscription, id);
        else if(lastRows != null)
            return notificationManager.getNotificationsByUserAddressWithLastRows(subscription, lastRows);
        else if(idTopics != null && idTopics.size() > 0)
            return notificationManager.getNotificationsByUserAddressAndIdTopic(subscription, idTopics);
        else
            return notificationManager.getNotificationsByUserAddress(subscription);
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
        return dataFetcherManager.saveOrUpdate(lastBlock);
    }

    public DataFetcherEntity saveLastBlockChainAddresses(BigInteger lastBlock){
        return dataFetcherManager.saveOrUpdateBlockChainAddress(lastBlock);
    }

    public DataFetcherEntity saveLastBlockPayment(BigInteger lastBlock){
        return dataFetcherManager.saveOrUpdateBlockPayment(lastBlock);
    }

    public BigInteger getLastBlock(){
        return dataFetcherManager.getLastRSKBlock();
    }

    public BigInteger getLastBlockForChainAddresses(){
        return dataFetcherManager.getLastRSKChainAddrBlock();
    }

    public BigInteger getLastBlockForPayment(){
        return dataFetcherManager.getLastPaymentBlock();
    }

    @Transactional
    public List<ChainAddressEvent> saveChainAddressesEvents(List<ChainAddressEvent> chainAddressEvents){
        return chainAddressEvents.stream().map(chainAddressEvent ->
                chainAddressManager.insert(chainAddressEvent.getNodehash(), chainAddressEvent.getEventName(), chainAddressEvent.getChain(), chainAddressEvent.getAddress(), chainAddressEvent.getRowhashcode(), chainAddressEvent.getBlock())
        ).collect(Collectors.toList());
    }


    public List<Notification> getUnsentNotifications(int maxRetries) {
        return notificationManager.getUnsentNotifications(maxRetries);
    }

    public Set<Notification> getUnsentNotificationsWithActiveSubscription(int maxRetries) {
        return notificationManager.getUnsentNotificationsWithActiveSubscription(maxRetries);
    }

    public int getUnsentNotificationsCount(int subscriptionId, int maxRetries) {
        return notificationManager.getUnsentNotificationsCount(subscriptionId, maxRetries);
    }

    public void logSuccessfulNotification(Notification notificationId, NotificationPreference notificationPreferenceId, String resultText) {
        notificationLogManager.logFailedNotification(notificationId, notificationPreferenceId, resultText);
    }

    public void logFailedNotification(Notification notificationId, NotificationPreference notificationPreferenceId, String errorMessage) {
        notificationLogManager.logFailedNotification(notificationId, notificationPreferenceId, errorMessage);
    }

    public NotificationPreference saveNotificationPreference(NotificationPreference preference) {
        return notificationPreferenceManager.saveNotificationPreference(preference);
    }

    public NotificationPreference getNotificationPreference(Subscription sub, int idTopic, NotificationServiceType type)    {
        return notificationPreferenceManager.getNotificationPreference(sub, idTopic, type);
    }

    public NotificationPreference getNotificationPreference(Subscription sub, NotificationServiceType type)    {
        return notificationPreferenceManager.getNotificationPreference(sub, type);
    }

    public List<NotificationPreference> getNotificationPreferences(Subscription sub, int idTopic)    {
        List<NotificationServiceType> enabledServices = notifierConfig.getEnabledServices();
        List<NotificationPreference> enabledPreferences = notificationPreferenceManager.getNotificationPreferences(sub, idTopic).stream().filter(
                p->enabledServices.stream().anyMatch(p2-> p2 == p.getNotificationService())).collect(Collectors.toList());
        return enabledPreferences;
    }

    public void saveSubscriptionPlan(SubscriptionPlan plan) {
        subscriptionPlanManager.save(plan);
    }

    public void saveSubscriptionPlans(List<SubscriptionPlan> plan) {
        subscriptionPlanManager.saveAll(plan);
    }

    public List<SubscriptionPlan> getSubscriptionPlans()    {
        return subscriptionPlanManager.getSubscriptionPlans();
    }

    public Optional<Currency> getCurrencyByAddress(Address address)    {
        return currencyManager.getCurrencyByAddress(address);
    }

    public Optional<Currency> getCurrencyByName(String name)    {
        return currencyManager.getCurrencyByName(name);
    }

    public Currency saveCurrency(Currency cur)  {
        return currencyManager.saveCurrency(cur);
    }
}
