package org.rif.notifier.validation;

import org.rif.notifier.constants.TopicParamTypes;
import org.rif.notifier.exception.ValidationException;
import org.rif.notifier.models.entities.SubscriptionPlan;
import org.rif.notifier.models.entities.SubscriptionPrice;
import org.rif.notifier.models.entities.Topic;
import org.rif.notifier.models.entities.TopicParams;
import org.rif.notifier.services.UserServices;
import org.rif.notifier.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.rif.notifier.constants.TopicParamTypes.*;

@Component
public class SubscribeValidator extends BaseValidator {
    /**
     * Path to web3Types, will be used to check some parameters of a Topic
     */
    private static final String PATH_TO_TYPES = "org.web3j.abi.datatypes.";

    public SubscribeValidator(@Autowired UserServices userServices) {
       super(userServices) ;
    }
    /**
     * Validates a given Topic, it checks if all required fields are correctly setted.
     * For CONTRACT_EVENT it checks that it has all Params like CONTRACT_EVENT_ADDRESS, CONTRACT_EVENT_NAME and at least one CONTRACT_EVENT_PARAM
     * In case of other types like NEW_TRANSACTIONS will be applied other logic
     * @param topic Topic sent by a user, parsed by, to be checked in the method if it is correctly setted
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

    /**
     * Verifies if a given price is valid for the given subsriptionplan
     * @param plan
     * @param price
     */
    public void validateSubscriptionPrice(SubscriptionPrice price, SubscriptionPlan plan) {
        List<SubscriptionPrice> planPriceList = plan.getSubscriptionPriceList();
        boolean match = planPriceList.stream().anyMatch(p->p.getCurrency().equals(price.getCurrency()) &&
                p.getPrice().equals(price.getPrice()));
        if (!match) throw new ValidationException("Subscription price or currency not valid for this plan");
    }
}
