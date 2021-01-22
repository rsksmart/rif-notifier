package org.rif.notifier.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.rif.notifier.managers.DbManagerFacade;
import org.rif.notifier.models.DTO.SubscriptionBatchDTO;
import org.rif.notifier.models.DTO.SubscriptionBatchResponse;
import org.rif.notifier.models.DTO.SubscriptionDTO;
import org.rif.notifier.models.DTO.SubscriptionResponse;
import org.rif.notifier.models.entities.*;
import org.rif.notifier.services.blockchain.lumino.LuminoInvoice;
import org.rif.notifier.util.Utils;
import org.rif.notifier.validation.SubscribeValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class SubscribeServices  {
    private DbManagerFacade dbManagerFacade;
    private SubscribeValidator subscribeValidator;

    public SubscribeServices(@Autowired DbManagerFacade dbManagerFacade, @Autowired SubscribeValidator subscribeValidator){
        this.dbManagerFacade = dbManagerFacade;
        this.subscribeValidator = subscribeValidator;
    }

    private static final Logger logger = LoggerFactory.getLogger(SubscribeServices.class);

    /**
     * Creates a subscription for a user, and a given type of subscription.
     * It creates a luminoInvoice that will be returned for the user to pay it.
     * When the user pays the invoice, the subscription will be activated
     * Actually we are not validating the subscription type, so it can be any number
     * @param user User that will be associated with the subscription
     * @param plan Subscription plan to create the subscription
     * @return LuminoInvoice string hash
     */
    public String createSubscription(User user, SubscriptionPlan plan, SubscriptionPrice subscriptionPrice){
        String retVal = "";
        if(user != null && plan!= null) {
//            if(isSubscriptionTypeValid(type.getId())) {
            //Subscription sub = dbManagerFacade.createSubscription(new Date(), user.getAddress(), type, SubscriptionConstants.PENDING_PAYMENT);
            Subscription sub = dbManagerFacade.createSubscription(new Date(), user.getAddress(), plan, SubscriptionStatus.ACTIVE, subscriptionPrice);
            //Pending to generate a lumino-invoice
            retVal = LuminoInvoice.generateInvoice(user.getAddress());
//            }
        }
        return retVal;
    }

    public Subscription createPendingSubscription(User user, SubscriptionPlan plan, SubscriptionPrice subscriptionPrice)  {
        return dbManagerFacade.createSubscription(new Date(),user.getAddress(), plan, SubscriptionStatus.PENDING, subscriptionPrice);
    }

    /**
     * This is use to activate a given subscription
     * @param subscription Subscription that will be activated
     * @return true in success case, false otherwise
     */
    public boolean activateSubscription(Subscription subscription){
        boolean retVal = false;
        if(subscription != null) {
            if(!subscription.isActive()) {
                subscription.setStatus(SubscriptionStatus.ACTIVE);
                subscription.setActiveSince(new Date());
                Subscription sub = dbManagerFacade.updateSubscription(subscription);
                retVal = sub != null;
            }
        }
        return retVal;
    }

    /**
     * Adds notification balance to a subscription, generates a lumino invoice of the type selected by the user.
     * @param subscription Subscription that the user wants to be updated
     * @param type Type selected, to get the new balance
     * @return Lumino invoice
     */
    public String addBalanceToSubscription(Subscription subscription, SubscriptionPlan type){
        String retVal = "";
        if(subscription != null && type != null) {
            subscription.setStatus(SubscriptionStatus.PENDING);
            subscription.setNotificationBalance(subscription.getNotificationBalance() + type.getNotificationQuantity());
            retVal = LuminoInvoice.generateInvoice(subscription.getUserAddress());
            if(!retVal.isEmpty()) {
                Subscription sub = dbManagerFacade.updateSubscription(subscription);
            }
        }
        return retVal;
    }

    public List<Subscription> getActiveSubscriptionByAddress(String user_address){
        return dbManagerFacade.getActiveSubscriptionByAddress(user_address);
    }

    public Subscription getActiveSubscriptionByAddressAndPlan(String user_address, SubscriptionPlan subscriptionPlan){
        return dbManagerFacade.getActiveSubscriptionByAddressAndType(user_address, subscriptionPlan);
    }

    public List<Subscription> getSubscriptionByAddress(String user_address){
        return dbManagerFacade.getSubscriptionByAddress(user_address);
    }

    public Subscription getSubscriptionByAddressAndPlan(String user_address, SubscriptionPlan subscriptionPlan){
        return dbManagerFacade.getSubscriptionByAddressAndType(user_address, subscriptionPlan);
    }

    /**
     * Makes the relation between subscription and topic.
     * First checks if the Topic is already created, so if it is, it creates only the relation, in other case it applies logic to the topic sent
     * At this moment the Topic needs to be correctly validated
     * @param topic Topic type fully validated
     * @param sub Subscription type, to be associated with the Topic sent
     */
    public SubscriptionResponse subscribeToTopic(Topic topic, Subscription sub)  {
       return Optional.ofNullable(subscribeAndGetTopic(topic, sub))
               .map(t->{return new SubscriptionResponse(t.getId());})
               .orElse(SubscriptionResponse.INVALID);
    }
    public Topic subscribeAndGetTopic(Topic topic, Subscription sub){
        if(topic != null && sub != null) {
            //Checks if the Topic already exists
            Topic tp = this.getTopicByHash(topic);
            if (tp == null) {
                //Generate Topic with no params
                tp = dbManagerFacade.saveTopic(topic.getType(), "" + topic.hashCode(), sub);
                switch (topic.getType()) {
                    case CONTRACT_EVENT:
                        //Generates params for the contract event
                        for (TopicParams param : topic.getTopicParams()) {
                            dbManagerFacade.saveTopicParams(
                                    tp, param.getType(), param.getValue(), param.getOrder(), param.getValueType(), param.getIndexed(), param.getFilter()
                            );
                        }
                        break;
                    case PENDING_TRANSACTIONS:
                    case NEW_BLOCK:
                    case NEW_TRANSACTIONS:
                        //Non of this topics types need params at this moment
                        break;
                }
            } else {
                //Add topic-subscription relationship
                tp.addSubscription(sub);
                dbManagerFacade.updateTopic(tp);
            }
            return tp;
            //This line was throwing error cause the Json is too large
            //resp.setData(ut);
        }
        return null;
    }

    /**
     * Validates if the user is sending a valid subscription type
     * @param type Type that need to exists in subscription types
     * @return if type exists
     */
    public boolean isSubscriptionPlanValid(int type){
        return Optional.ofNullable(dbManagerFacade.getSubscriptionPlanById(type)).isPresent();
    }

    /**
     * Returns a subscription type by giving it the int type
     * @param planId Int type, to be searched
     * @return Subscription type in case finds it
     */
    public SubscriptionPlan getSubscriptionPlanById(int planId){
        return dbManagerFacade.getSubscriptionPlanById(planId);
    }

    /**
     * Brings a Topic by its hashCode
     * @param topic
     * @return Returns a topic if finds it
     */
    public Topic getTopicByHash(Topic topic){
        return dbManagerFacade.getTopicByHashCode(topic.hashCode());
    }

    public Topic getTopicByHashCodeAndIdSubscription(Topic topic, int idSubscription){
        return dbManagerFacade.getTopicByHashCodeAndIdSubscription(topic.hashCode(), idSubscription);
    }

    public boolean validateTopic(Topic topic) {
        return subscribeValidator.validateTopic(topic);
    }

    /***
     * Given a subscription and a topic it deletes the relationship between them
     * @param sub Subscription of the user that dont want to listen to topic
     * @param tp Topic to be devinculated from subscription
     * @return
     */
    public boolean unsubscribeFromTopic(Subscription sub, Topic tp){
        if(tp.getSubscriptions().contains(sub)) {
            tp.getSubscriptions().remove(sub);
            return dbManagerFacade.updateTopic(tp) != null;
        }
        return false;
    }

    /**
     * Retrieves a Topic from a given id
     * @param idTopic to be searched
     * @return
     */
    public Topic getTopicById(int idTopic){
        return dbManagerFacade.getTopicById(idTopic);
    }

    /*public Map<String, Object> buildSubscriptionResponseMap(SubscriptionDTO subscriptionDTO, String hash, String privateKey)   {
        Map<String,Object> resp = new TreeMap<>();
        resp.put("hash", hash);
        resp.put("signature", signHash(hash, privateKey));
        resp.put("subscription", subscriptionDTO);
        return resp;
    }*/

    public SubscriptionBatchResponse createSubscriptionBatchResponse(SubscriptionDTO subscriptionDTO, String hash, String privateKey)   {
        String signature = signHash(hash, privateKey);
        SubscriptionBatchResponse response = new SubscriptionBatchResponse(hash, signature, subscriptionDTO);
        return response;
    }


    public SubscriptionDTO createSubscriptionDTO(SubscriptionBatchDTO subscriptionBatchDTO,
                                                 Subscription subscription, String providerAddress, User user)   {
        SubscriptionDTO subscriptionDTO = new SubscriptionDTO();
        subscriptionDTO.setUserAddress(subscriptionBatchDTO.getUserAddress());
        subscriptionDTO.setProviderAddress(providerAddress);
        subscriptionDTO.setPrice(subscription.getPrice());
        subscriptionDTO.setExpirationDate(subscription.getExpirationDate());
        subscriptionDTO.setNotificationBalance(subscription.getNotificationBalance());
        subscriptionDTO.setStatus(subscription.getStatus());
        subscriptionDTO.setCurrency(subscription.getCurrency());
        subscriptionDTO.setTopics(subscriptionBatchDTO.getTopics());
        subscriptionDTO.setApiKey(user.getApiKey());
        return subscriptionDTO;
    }

    public String getSubscriptionHash(SubscriptionDTO subscriptionDTO) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return Utils.generateHash(mapper.writeValueAsString(subscriptionDTO));
        } catch(IOException e)    {
           throw new RuntimeException(e);
        }
    }

    public void updateSubscription(Subscription subscription)   {
        dbManagerFacade.updateSubscription(subscription);
    }

    public String signHash(String hash, String privateKey) {
        return Utils.signAsString(hash, privateKey);
    }

}
