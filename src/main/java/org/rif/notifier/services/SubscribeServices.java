package org.rif.notifier.services;

import org.rif.notifier.constants.SubscriptionConstants;
import org.rif.notifier.constants.TopicParamTypes;
import org.rif.notifier.managers.DbManagerFacade;
import org.rif.notifier.models.entities.*;
import org.rif.notifier.services.blockchain.lumino.LuminoInvoice;
import org.rif.notifier.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

import static org.rif.notifier.constants.TopicParamTypes.*;

@Service
public class SubscribeServices  {
    @Autowired
    private DbManagerFacade dbManagerFacade;

    private static final Logger logger = LoggerFactory.getLogger(SubscribeServices.class);

    /**
     * Path to web3Types, will be used to check some parameters of a Topic
     */
    private static final String PATH_TO_TYPES = "org.web3j.abi.datatypes.";

    /**
     * Creates a subscription for a user, and a given type of subscription.
     * It creates a luminoInvoice that will be returned for the user to pay it.
     * When the user pays the invoice, the subscription will be activated
     * Actually we are not validating the subscription type, so it can be any number
     * @param user User that will be associated with the subscription
     * @param type Subscription type to create the subscription
     * @return LuminoInvoice string hash
     */
    public String createSubscription(User user, SubscriptionType type){
        String retVal = "";
        if(user != null && type != null) {
//            if(isSubscriptionTypeValid(type.getId())) {
            //Subscription sub = dbManagerFacade.createSubscription(new Date(), user.getAddress(), type, SubscriptionConstants.PENDING_PAYMENT);
            Subscription sub = dbManagerFacade.createSubscription(new Date(), user.getAddress(), type, SubscriptionConstants.PAYED_PAYMENT);
            //Pending to generate a lumino-invoice
            retVal = LuminoInvoice.generateInvoice(user.getAddress());
//            }
        }
        return retVal;
    }

    /**
     * This is use to activate a given subscription
     * @param subscription Subscription that will be activated
     * @return true in success case, false otherwise
     */
    public boolean activateSubscription(Subscription subscription){
        boolean retVal = false;
        if(subscription != null) {
            if(!subscription.getActive()) {
                subscription.setActive(true);
                subscription.setState(SubscriptionConstants.PAYED_PAYMENT);
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
    public String addBalanceToSubscription(Subscription subscription, SubscriptionType type){
        String retVal = "";
        if(subscription != null && type != null) {
            subscription.setState(SubscriptionConstants.PENDING_PAYMENT);
            subscription.setNotificationBalance(subscription.getNotificationBalance() + type.getNotifications());
            subscription.setActive(false);
            retVal = LuminoInvoice.generateInvoice(subscription.getUserAddress());
            if(!retVal.isEmpty()) {
                Subscription sub = dbManagerFacade.updateSubscription(subscription);
            }
        }
        return retVal;
    }

    public Subscription getActiveSubscriptionByAddress(String user_address){
        return dbManagerFacade.getActiveSubscriptionByAddress(user_address);
    }

    public Subscription getSubscriptionByAddress(String user_address){
        return dbManagerFacade.getSubscriptionByAddress(user_address);
    }

    /**
     * Makes the relation between subscription and topic.
     * First checks if the Topic is already created, so if it is, it creates only the relation, in other case it applies logic to the topic sended
     * At this moment the Topic needs to be correctly validated
     * @param topic Topic type fully validated
     * @param sub Subscription type, to be associated with the Topic sended
     */
    public int subscribeToTopic(Topic topic, Subscription sub){
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
                return tp.getId();
            } else {
                //Add topic-subscription relationship
                tp.addSubscription(sub);
                dbManagerFacade.updateTopic(tp);
                return tp.getId();
            }
            //This line was throwing error cause the Json is too large
            //resp.setData(ut);
        }
        return -1;
    }

    /**
     * Validates if the user is sending a valid subscription type
     * @param type Type that need to exists in subscription types
     * @return if type exists
     */
    public boolean isSubscriptionTypeValid(int type){
        SubscriptionType subType = dbManagerFacade.getSubscriptionTypeByType(type);
        return subType != null;
    }

    /**
     * Returns a subscription type by giving it the int type
     * @param type Int type, to be searched
     * @return Subscription type in case finds it
     */
    public SubscriptionType getSubscriptionTypeByType(int type){
        return dbManagerFacade.getSubscriptionTypeByType(type);
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

    /**
     * Validates a given Topic, it checks if all required fields are correctly setted.
     * For CONTRACT_EVENT it checks that it has all Params like CONTRACT_EVENT_ADDRESS, CONTRACT_EVENT_NAME and at least one CONTRACT_EVENT_PARAM
     * In case of other types like NEW_TRANSACTIONS will be applied other logic
     * @param topic Topic sended by a user, parsed by, to be checked in the method if it is correctly setted
     * @return True in case that the Topic is correctly validated and has all the required fields, false if something's missed
     */
    public boolean validateTopic(Topic topic){
        if(topic.getType() != null) {
            switch (topic.getType()) {
                case CONTRACT_EVENT:
                    return validateContractEventParams(topic.getTopicParams());
                case PENDING_TRANSACTIONS:
                case NEW_BLOCK:
                case NEW_TRANSACTIONS:
                    return true;
            }
        }
        return false;
    }

    /**
     * Checks all the required parameters for a Contract Event, returns false if some parameter is missed
     * Required:
     * -CONTRACT_ADDRESS (Checks if is just 1 param)
     * -EVENT_NAME (Just 1 param)
     * -EVENT_PARAM (Checks if is at least 1)
     * For the EVENT_PARAMS checks if also has a valid web3-type
     * When finding just 1 error, it breaks the for, and returns false
     * @param params List of Params for a CONTRACT_EVENT
     * @return True if all the required params are correctly setted
     */
    private boolean validateContractEventParams(List<TopicParams> params){
    	int counterContractAddress = 0, counterEventName = 0;
    	
    	for (TopicParams param : params) {
    		// if we receive a param without type or value we know is invalid
    		if ((null == param.getType()) || (null == param.getValue()) || param.getValue().isEmpty()) {
    			return false;
    		}
            TopicParamTypes type = param.getType();
    		if(type.equals(CONTRACT_ADDRESS) || type.equals(EVENT_NAME) || type.equals(EVENT_PARAM)) {
    			switch (type) {
                    case CONTRACT_ADDRESS:
                        if(param.getValue().isEmpty())
                            return false;
	                	counterContractAddress++;
	                    break;
	                case EVENT_NAME:
                        if(param.getValue().isEmpty())
                            return false;
	                	counterEventName++;
	                    break;
                    case EVENT_PARAM:
                        if(param.getValue().isEmpty() || param.getValueType().isEmpty() || !isWeb3Type(param.getValueType()))
                            return false;
                        break;
	            }
    		} else {
    			// if we reach this point it means the parameter has an invalid type
    			return false;
    		}
    	}
    	
        //Checking that the user sends at least 1 contract_address and 1 event name
        return (1 == counterContractAddress) && (1 == counterEventName);
    }

    /**
     * Checks if the given String is a correct Web3Type
     * @param type String to be checked
     * @return True if the type exists in the library
     */
    private boolean isWeb3Type(String type) {
        boolean ret = false;
        if (Utils.isClass(PATH_TO_TYPES + type))
            ret =  true;
        else if(Utils.isClass(PATH_TO_TYPES + "generated." + type))
            ret = true;

        return ret;
    }
}
