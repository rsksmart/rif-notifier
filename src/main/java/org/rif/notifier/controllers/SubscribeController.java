package org.rif.notifier.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.rif.notifier.constants.ControllerConstants;
import org.rif.notifier.constants.ResponseConstants;
import org.rif.notifier.exception.SubscriptionException;
import org.rif.notifier.exception.ValidationException;
import org.rif.notifier.models.DTO.DTOResponse;
import org.rif.notifier.models.DTO.SubscriptionResponse;
import org.rif.notifier.models.entities.*;
import org.rif.notifier.services.SubscribeServices;
import org.rif.notifier.services.UserServices;
import org.rif.notifier.validation.CurrencyValidator;
import org.rif.notifier.validation.SubscribeValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Optional;

@Api(tags = {"Onboarding Resource"})
@RestController
@ConditionalOnProperty(value = "notifier.endpoints.subscribecontroller",havingValue = "true")
public class SubscribeController {
    private static final Logger logger = LoggerFactory.getLogger(SubscribeController.class);

    private SubscribeServices subscribeServices;
    private UserServices userServices;
    private SubscribeValidator subscribeValidator;
    private CurrencyValidator currencyValidator;

    @Autowired
    public SubscribeController(SubscribeServices subscribeServices, UserServices userServices,
                               @Autowired SubscribeValidator subscribeValidator,
                               @Autowired CurrencyValidator currencyValidator) {
        this.subscribeServices = subscribeServices;
        this.userServices = userServices;
        this.subscribeValidator = subscribeValidator;
        this.currencyValidator = currencyValidator;
    }

    /**
     *
     * @param apiKey
     * @param price - price of the subscription
     * @param currency - the currency for subscription
     * @return
     */
    @ApiOperation(value = "Generate a subscription with an Apikey",
            response = DTOResponse.class, responseContainer = ControllerConstants.LIST_RESPONSE_CONTAINER)
    @RequestMapping(value = "/subscribe", method = RequestMethod.POST, produces = {ControllerConstants.CONTENT_TYPE_APPLICATION_JSON})
    @ResponseBody
    public ResponseEntity<DTOResponse> subscribe(
            @RequestHeader(value="apiKey") String apiKey,
            @RequestParam int planId,
            @RequestParam BigInteger price,
            @RequestParam String currency) {
        DTOResponse resp = new DTOResponse();
        User us = Optional.ofNullable(userServices.getUserByApiKey(apiKey)).orElseThrow(()->new ValidationException(ResponseConstants.INCORRECT_APIKEY));
        SubscriptionPlan subscriptionPlan = subscribeServices.getSubscriptionPlanById(planId);
        SubscriptionPrice subscriptionPrice = new SubscriptionPrice(price, currencyValidator.validate(currency));
        //validate if this subscription plan actually exists in the database
        subscriptionPlan = subscribeServices.getSubscriptionPlanById(subscriptionPlan.getId());
        Optional.ofNullable(subscriptionPlan).orElseThrow(()->new ValidationException(ResponseConstants.SUBSCRIPTION_INCORRECT_TYPE));
        //validate if the provided price exists for this plan
        subscribeValidator.validateSubscriptionPrice(subscriptionPrice, subscriptionPlan);
        //throw exception if subscription already added
        Optional.ofNullable(subscribeServices.getActiveSubscriptionByAddressAndPlan(us.getAddress(), subscriptionPlan)).ifPresent(p-> {
           throw new SubscriptionException(ResponseConstants.SUBSCRIPTION_ALREADY_ADDED) ;
        });
        //if no active subscription for given user and subscription type found, then create a new subscriiption
        resp.setContent(subscribeServices.createSubscription(us, subscriptionPlan, subscriptionPrice));
        return new ResponseEntity<>(resp, resp.getStatus());
    }

    @ApiOperation(value = "Subscribes to a topic",
            response = DTOResponse.class, responseContainer = ControllerConstants.LIST_RESPONSE_CONTAINER)
    @RequestMapping(value = "/subscribeToTopic", method = RequestMethod.POST, produces = {ControllerConstants.CONTENT_TYPE_APPLICATION_JSON})
    @ResponseBody
    public ResponseEntity<DTOResponse> subscribeToTopic(
            @RequestHeader(value="apiKey") String apiKey,
            @RequestParam(name = "subscriptionHash") String subscriptionHash,
            @RequestBody String userTopic) {
        ObjectMapper mapper = new ObjectMapper();
        Topic userSentTopic = null;
        DTOResponse resp = new DTOResponse();
        try {
            userSentTopic = mapper.readValue(userTopic, Topic.class);
            //check valid user and if not throw exception
            User us = Optional.ofNullable(userServices.getUserByApiKey(apiKey)).orElseThrow(()->new ValidationException(ResponseConstants.INCORRECT_APIKEY));
            //check valid subscription type otherwise throw error
            //if no active subscription is found for hash and user address then throw exception
            Subscription sub = subscribeServices.getActiveSubscriptionByHashAndUserAddress(subscriptionHash, us.getAddress());
            if (sub == null)    {
                throw new ValidationException(ResponseConstants.NO_ACTIVE_SUBSCRIPTION);
            }
            //validate the user sent topic
            if(subscribeValidator.validateTopic(userSentTopic)){
                //Return an error if the user sent topic is already subscribed
                Optional.ofNullable(subscribeServices.getTopicByHashCodeAndIdSubscription(userSentTopic, sub.getId())).ifPresent(t->new SubscriptionException(ResponseConstants.AlREADY_SUBSCRIBED_TO_TOPIC, new SubscriptionResponse(t.getId()), null));
                resp.setContent(subscribeServices.subscribeToTopic(userSentTopic, sub));
            }else{
                //Return an error when the user sends a wrong structure of topic
                throw new ValidationException(ResponseConstants.TOPIC_VALIDATION_FAILED);
            }
        } catch (IOException e) {
            throw new ValidationException(ResponseConstants.TOPIC_VALIDATION_FAILED);
        }

        return new ResponseEntity<>(resp, resp.getStatus());
    }


    @ApiOperation(value = "Unsubscribes from a topic given an id",
            response = DTOResponse.class, responseContainer = ControllerConstants.LIST_RESPONSE_CONTAINER)
    @RequestMapping(value = "/unsubscribeFromTopic", method = RequestMethod.POST, produces = {ControllerConstants.CONTENT_TYPE_APPLICATION_JSON})
    @ResponseBody
    public ResponseEntity<DTOResponse> unsubscribeFromTopic(
            @RequestHeader(value="apiKey") String apiKey,
            @RequestParam(name = "subscriptionHash") String subscriptionHash,
            @RequestParam(value="idTopic") int idTopic) {
        DTOResponse resp = new DTOResponse();
         User us = userServices.getUserByApiKey(apiKey);
        if(us != null){
            //Check if the user has active subscription and is owner of the given subscription hash
            Subscription sub = subscribeServices.getActiveSubscriptionByHashAndUserAddress(subscriptionHash, us.getAddress());
            if(sub != null) {
                Topic tp = subscribeServices.getTopicById(idTopic);
                if(tp != null){
                    if(!subscribeServices.unsubscribeFromTopic(sub, tp)){
                        throw new SubscriptionException(ResponseConstants.UNSUBSCRIBED_FROM_TOPIC_FAILED);
                    }
                }else{
                    throw new SubscriptionException(ResponseConstants.INVALID_TOPIC_ID);
                }
            }else{
                //Return an error because the user still did not create the subscription
                throw new SubscriptionException(ResponseConstants.NO_ACTIVE_SUBSCRIPTION);
            }
        }else{
            throw new ValidationException(ResponseConstants.INCORRECT_APIKEY);
        }
        return new ResponseEntity<>(resp, resp.getStatus());
    }
}
