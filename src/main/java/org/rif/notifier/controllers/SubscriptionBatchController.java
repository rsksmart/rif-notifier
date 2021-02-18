package org.rif.notifier.controllers;

import io.swagger.annotations.*;
import org.rif.notifier.boot.configuration.NotifierConfig;
import org.rif.notifier.constants.ControllerConstants;
import org.rif.notifier.constants.ResponseConstants;
import org.rif.notifier.exception.SubscriptionException;
import org.rif.notifier.exception.ValidationException;
import org.rif.notifier.managers.datamanagers.NotificationPreferenceManager;
import org.rif.notifier.models.DTO.*;
import org.rif.notifier.models.entities.*;
import org.rif.notifier.services.SubscribeServices;
import org.rif.notifier.services.SubscriptionPlanServices;
import org.rif.notifier.services.UserServices;
import org.rif.notifier.validation.CurrencyValidator;
import org.rif.notifier.validation.NotificationPreferenceValidator;
import org.rif.notifier.validation.SubscribeValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.web3j.abi.datatypes.Address;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Optional;

@Api(tags = {"Batch Onboarding Resource"})
@Validated
@RestController
public class SubscriptionBatchController {
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionBatchController.class);

    private SubscribeServices subscribeServices;
    private SubscriptionPlanServices subscriptionPlanServices;
    private UserServices userServices;
    private CurrencyValidator currencyValidator;
    private NotificationPreferenceManager notificationPreferenceManager;
    private NotificationPreferenceValidator notificationPreferenceValidator;
    private SubscribeValidator subscribeValidator;
    private NotifierConfig notifierConfig;
    private Address providerAddress;
    private String providerPrivateKey;

    public SubscriptionBatchController(SubscribeServices subscribeServices, UserServices userServices,
                                       NotificationPreferenceManager notificationPreferenceManager,
                                       NotificationPreferenceValidator notificationPreferenceValidator,
                                       SubscribeValidator subscribeValidator, CurrencyValidator currencyValidator,
                                       NotifierConfig notifierConfig,SubscriptionPlanServices subscriptionPlanServices,
                                       @Qualifier("providerAddress") Address providerAddress,
                                       @Qualifier("providerPrivateKey") String providerPrivateKey) {
        this.subscribeServices = subscribeServices;
        this.userServices = userServices;
        this.notificationPreferenceManager = notificationPreferenceManager;
        this.notificationPreferenceValidator = notificationPreferenceValidator;
        this.subscribeValidator = subscribeValidator;
        this.providerAddress = providerAddress;
        this.providerPrivateKey = providerPrivateKey;
        this.currencyValidator = currencyValidator;
        this.notifierConfig = notifierConfig;
        this.subscriptionPlanServices = subscriptionPlanServices;
    }

    /**
     * @param subscriptionBatchDTO the structure of subscriptionPrice as below
     *   "userAddress": "0x882bf23c4a7E73cA96AF14CACfA2CC006F6781A9",
     *     "price": 20,
     *     "currency": "RIF",
     *     "subscriptionPlanId": 1,
     *   "topics": [
     *     {
     * 		"type": "NEW_TRANSACTIONS",
     * 		"notificationPreferences":[
     *                        {
     * 				"notificationService":"API",
     * 				"destination":"https://postman-echo.com/post",
     *                 "destinationParams":{
     *                     "username":"test"
     *                 }
     *            },
     *
     *            {
     * 				"notificationService":"EMAIL",
     * 				"destination":"123456@test.com"
     *            }
     * 		]
     * 	},
     * 	{
     * 		"type": "NEW_BLOCK",
     * 		"notificationPreferences":[
     *
     *            {
     * 				"notificationService":"EMAIL",
     * 				"destination":"123456@abc.com;123@abc.com"
     *            }
     * 		]
     * 	}
     *
     *   ]
     * }
     * @return
     */
    @ApiOperation(value = "Subscribe to Rif Notifier with plan id, topics, notification preferences", notes="Returns http 409 in case of error",
            response = DTOResponse.class, responseContainer = ControllerConstants.LIST_RESPONSE_CONTAINER)
    @ApiResponses(value={
            @ApiResponse(code=200, message="Subscription created successfully.", response=SubscriptionBatchResponse.class),
            @ApiResponse(code= 409, message="Error creating subsription.", response=DTOResponse.class)
    })
    @RequestMapping(value = "/subscribeToPlan", method = RequestMethod.POST, produces = {ControllerConstants.CONTENT_TYPE_APPLICATION_JSON})
    @ResponseBody
    public ResponseEntity<DTOResponse> subscribeToPlan(
            @ApiParam(required=true, name="Subscription details required to create the subscription. See SubscriptionBatchDTO") @Valid @RequestBody SubscriptionBatchDTO subscriptionBatchDTO) {
       return subscribeBatch(subscriptionBatchDTO, null);
    }


    @ApiOperation(value = "Renew Rif Notifier subscription with plan id, topics, notification preferences", notes="Returns http 409 in case of error",
            response = DTOResponse.class, responseContainer = ControllerConstants.LIST_RESPONSE_CONTAINER)
    @ApiResponses(value={
            @ApiResponse(code=200, message="Subscription created successfully.", response=SubscriptionBatchResponse.class),
            @ApiResponse(code= 409, message="Error creating subsription.", response=DTOResponse.class)
    })
    @RequestMapping(value = "/renewSubscription", method = RequestMethod.POST, produces = {ControllerConstants.CONTENT_TYPE_APPLICATION_JSON})
    @ResponseBody
    public ResponseEntity<DTOResponse> renewSubscription(
            @ApiParam(required=true, name="Subscription hash of the previous subscription.")
            @NotBlank @RequestParam String previousSubscriptionHash,
            @ApiParam(required=true, name="Subscription details required to create the subscription. See SubscriptionBatchDTO")
            @Valid @RequestBody SubscriptionBatchDTO subscriptionBatchDTO) {
        return subscribeBatch(subscriptionBatchDTO, previousSubscriptionHash);
    }

    protected ResponseEntity<DTOResponse> subscribeBatch(SubscriptionBatchDTO subscriptionBatchDTO, String previousSubscriptionHash)  {
        DTOResponse resp = new DTOResponse();
        User user = getNewOrExistingUser(subscriptionBatchDTO.getUserAddress());
        //first validate if the topic and preferences are in correct format
        validate(subscriptionBatchDTO);
        Optional<Subscription> previousSubscription = validateAndGetPreviousSubscription(previousSubscriptionHash);
        //proceed to create subscription
        SubscriptionPrice subscriptionPrice = new SubscriptionPrice(subscriptionBatchDTO.getPrice(), currencyValidator.validate(subscriptionBatchDTO.getCurrency()));
        Subscription subscription = createSubscription(user, subscriptionPrice, subscriptionBatchDTO.getSubscriptionPlanId(), previousSubscription.isPresent());
        previousSubscription.ifPresent(prev->subscription.setPreviousSubscription(prev));
        subscribeToTopic(subscription, subscriptionBatchDTO.getTopics());
        SubscriptionDTO subscriptionDTO = subscribeServices.createSubscriptionDTO(subscriptionBatchDTO, subscription, providerAddress, user);
        String hash = subscribeServices.getSubscriptionHash(subscriptionDTO);
        subscription.setHash(hash);
        //update the database with the generated hash
        subscribeServices.updateSubscription(subscription);
        //generate the subscription contract son
        resp.setContent(subscribeServices.createSubscriptionBatchResponse(subscriptionDTO, hash, providerPrivateKey));
        return new ResponseEntity<>(resp, resp.getStatus());
    }


    /*
     * Validate that the previous subscription exists and is not in pending state
     * Only active, completed or expired subscriptions can be renewed.
     * @param previousSubscriptionHash
     */
    private Optional<Subscription> validateAndGetPreviousSubscription(String previousSubscriptionHash)  {
        if(previousSubscriptionHash == null)    {
            return Optional.empty();
        }
        Optional<Subscription> subscription = Optional.ofNullable(subscribeServices.getSubscriptionByHash(previousSubscriptionHash));
        subscription.orElseThrow(()->new ValidationException(ResponseConstants.SUBSCRIPTION_NOT_FOUND_HASH));
        if(subscription.get().isPending())    {
            throw new ValidationException(ResponseConstants.PREVIOUS_SUBSCRIPTION_INVALID_STATE);
        }
        return subscription;
    }



    /*
     * throws ValidationException in case of validation failure
     */
    private void validate(SubscriptionBatchDTO subscriptionBatchDTO) {
        List<TopicDTO> topicDTOs = subscriptionBatchDTO.getTopics();
        if (topicDTOs.size() > topicDTOs.stream().distinct().count())   {
            throw new ValidationException("Duplicate topics found, please correct your json.");
        }
        //validate each topic
        topicDTOs.forEach(topicDTO->{
            Topic topic = new Topic(topicDTO.getType(), topicDTO.getTopicParams());
            if(!subscribeValidator.validateTopic(topic)){
                //Return an error when the user sends a wrong structure of topic
                throw new ValidationException(ResponseConstants.TOPIC_VALIDATION_FAILED);
            }
            //validate all the notification preferences for the given topic
            notificationPreferenceValidator.validate(topicDTO.getNotificationPreferences());
        });
    }


    private void subscribeToTopic(Subscription subscription, List<TopicDTO> topicDTOs) {
        topicDTOs.forEach(topicDTO->{
            Topic topic = new Topic(topicDTO.getType(), topicDTO.getTopicParams());
            //Return an error if the user sent topic is already subscribed
            Optional.ofNullable(subscribeServices.getTopicByHashCodeAndIdSubscription(topic, subscription.getId()))
                    .ifPresent(t->new SubscriptionException(ResponseConstants.AlREADY_SUBSCRIBED_TO_TOPIC,
                                new SubscriptionResponse(t.getId()), null));
            topic = subscribeServices.subscribeAndGetTopic(topic, subscription);
            saveNotificationPreferences(subscription, topic, topicDTO.getNotificationPreferences());
        });
    }

    private void saveNotificationPreferences(Subscription subscription, Topic topic, @Valid List<NotificationPreference> preferences)  {
        preferences.forEach(preference->{
            preference.setSubscription(subscription);
            //the topic id sent as part of request. This is an integer topic id for ex. 10.
            // When the topic id is set to 0, this preference will be used as default preference for those
            // notifications with topic that don't have notification preference speicied.
            preference.setIdTopic(topic.getId());
        });
        notificationPreferenceManager.saveNotificationPreferences(preferences);
    }

    private Subscription createSubscription(User user, SubscriptionPrice subscriptionPrice, int subscriptionPlanId, boolean renewal)   {
        //validate if this subscription plan actually exists in the database
        SubscriptionPlan subscriptionPlan = subscriptionPlanServices.getActiveSubscriptionPlan(subscriptionPlanId)
                .orElseThrow(() -> new ValidationException(ResponseConstants.SUBSCRIPTION_INCORRECT_TYPE));
        //validate if the provided price exists for this plan
        subscribeValidator.validateSubscriptionPrice(subscriptionPrice, subscriptionPlan);
        //throw exception if subscription already added
        if (!renewal) {
            Optional.ofNullable(subscribeServices.getActiveSubscriptionByAddressAndPlan(user.getAddress(), subscriptionPlan)).ifPresent(p -> {
                throw new SubscriptionException(ResponseConstants.SUBSCRIPTION_ALREADY_ADDED);
            });
        }
        //if no active subscription for given user and subscription type found, then create a new subscriiption
        return subscribeServices.createPendingSubscription(user, subscriptionPlan, subscriptionPrice);
    }

    private User getNewOrExistingUser(String address) {
        Optional<User> user = Optional.ofNullable(userServices.userExists(address));
        if (!user.isPresent()) {
            return userServices.saveUser(address);
        } else {
            //User already have an apikey
            return user.get();
        }
    }
}
