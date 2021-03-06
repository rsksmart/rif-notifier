package org.rif.notifier.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.rif.notifier.constants.ControllerConstants;
import org.rif.notifier.constants.ResponseConstants;
import org.rif.notifier.exception.SubscriptionException;
import org.rif.notifier.exception.ValidationException;
import org.rif.notifier.models.DTO.DTOResponse;
import org.rif.notifier.models.DTO.SubscriptionResponse;
import org.rif.notifier.models.entities.Subscription;
import org.rif.notifier.models.entities.Topic;
import org.rif.notifier.models.entities.User;
import org.rif.notifier.services.LuminoEventServices;
import org.rif.notifier.services.SubscribeServices;
import org.rif.notifier.services.UserServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.List;

@Api(tags = {"Lumino Onboarding Resource"})
@RestController
public class LuminoSubscribeController {
    private static final Logger logger = LoggerFactory.getLogger(LuminoSubscribeController.class);

    private SubscribeServices subscribeServices;
    private UserServices userServices;
    private LuminoEventServices luminoEventServices;


    @Autowired
    public LuminoSubscribeController(SubscribeServices subscribeServices, UserServices userServices, LuminoEventServices luminoEventServices) {
        this.userServices = userServices;
        this.luminoEventServices = luminoEventServices;
        this.subscribeServices = subscribeServices;
    }

    @ApiOperation(value = "Gets all preloaded events",
            response = DTOResponse.class, responseContainer = ControllerConstants.LIST_RESPONSE_CONTAINER)
    @RequestMapping(value = "/getLuminoTokens", method = RequestMethod.GET, produces = {ControllerConstants.CONTENT_TYPE_APPLICATION_JSON})
    @ResponseBody
    public ResponseEntity<DTOResponse> getLuminoTokens(
            @RequestParam(name = "subscriptionHash") String subscriptionHash,
            @RequestHeader(value="userAddress") String userAddress,
            @RequestHeader(value="apiKey") String apiKey) throws LoginException {
        DTOResponse resp = new DTOResponse();
        User us = userServices.authenticate(userAddress, apiKey);
        //Check if the user did subscribe and has an active subscription and owner of the subscriptionhash
        if(subscribeServices.getActiveSubscriptionByHashAndUserAddress(subscriptionHash, userAddress) != null) {
            resp.setContent(luminoEventServices.getTokens());
        } else {
            //Return an error because the user still did not create the subscription
            throw new SubscriptionException(ResponseConstants.NO_ACTIVE_SUBSCRIPTION);
        }
        return new ResponseEntity<>(resp, resp.getStatus());
    }

    @ApiOperation(value = "Subscribes to a lumino open channel event",
            response = DTOResponse.class, responseContainer = ControllerConstants.LIST_RESPONSE_CONTAINER)
    @RequestMapping(value = "/subscribeToOpenChannel", method = RequestMethod.POST, produces = {ControllerConstants.CONTENT_TYPE_APPLICATION_JSON})
    @ResponseBody
    public ResponseEntity<DTOResponse> subscribeToOpenChannel(
            @RequestParam(name = "token") String token,
            @RequestParam(name = "participantone", required = false) String participantOne,
            @RequestParam(name = "participanttwo", required = false) String participantTwo,
            @RequestParam(name = "subscriptionHash") String subscriptionHash,
            @RequestHeader(value="userAddress") String userAddress,
            @RequestHeader(value="apiKey") String apiKey) throws LoginException {
        DTOResponse resp = new DTOResponse();
        User us = userServices.authenticate(userAddress,apiKey);
        //Check if the user did subscribe and has an active subscription and owner of the subscriptionhash
        Subscription sub = subscribeServices.getActiveSubscriptionByHashAndUserAddress(subscriptionHash, userAddress);
        if (sub != null) {
            token = token.toLowerCase();
            if(luminoEventServices.isToken(token)){
                Topic openChannelTopic = luminoEventServices.getChannelOpenedTopicForToken(token, participantOne, participantTwo);
                Topic topic = subscribeServices.getTopicByHashCodeAndIdSubscription(openChannelTopic, sub.getId());
                if(topic == null) {
                    resp.setContent(subscribeServices.subscribeToTopic(openChannelTopic, sub));
                }else{
                    //Return an error because the user is sending a topic that he's already subscribed
                    throw new SubscriptionException(ResponseConstants.AlREADY_SUBSCRIBED_TO_TOPIC, new SubscriptionResponse(topic.getId()), null);
                }
            }else{
                //Return an error because the user send a incorrect token
                throw new SubscriptionException(ResponseConstants.INCORRECT_TOKEN);
            }
        } else {
            //Return an error because the user still did not create the subscription
            throw new SubscriptionException(ResponseConstants.NO_ACTIVE_SUBSCRIPTION);
        }
        return new ResponseEntity<>(resp, resp.getStatus());
    }

    @ApiOperation(value = "Subscribes to a lumino close channel event",
            response = DTOResponse.class, responseContainer = ControllerConstants.LIST_RESPONSE_CONTAINER)
    @RequestMapping(value = "/subscribeToCloseChannel", method = RequestMethod.POST, produces = {ControllerConstants.CONTENT_TYPE_APPLICATION_JSON})
    @ResponseBody
    public ResponseEntity<DTOResponse> subscribeToCloseChannel(
            @RequestParam(name = "token") String token,
            @RequestParam(name = "channelidentifier", required = false) Integer channelIdentifier,
            @RequestParam(name = "closingparticipant", required = false) String closingParticipant,
            @RequestParam(name = "subscriptionHash") String subscriptionHash,
            @RequestHeader(value="userAddress") String userAddress,
            @RequestHeader(value="apiKey") String apiKey) throws LoginException {
        DTOResponse resp = new DTOResponse();
        User us = userServices.authenticate(userAddress, apiKey);
        //Check if the user did subscribe and has an active subscription and owner of the subscriptionhash
        Subscription sub = subscribeServices.getActiveSubscriptionByHashAndUserAddress(subscriptionHash, userAddress);
        if (sub != null) {
            token = token.toLowerCase();
            if(luminoEventServices.isToken(token)){
                Topic closeChannelTopic = luminoEventServices.getChannelClosedTopicForToken(token, channelIdentifier, closingParticipant);
                Topic topic = subscribeServices.getTopicByHashCodeAndIdSubscription(closeChannelTopic, sub.getId());
                if(topic == null) {
                    resp.setContent(subscribeServices.subscribeToTopic(closeChannelTopic, sub));
                }else{
                    //Return an error because the user is sending a topic that he's already subscribed
                    throw new SubscriptionException(ResponseConstants.AlREADY_SUBSCRIBED_TO_TOPIC, new SubscriptionResponse(topic.getId()), null);
                }
            }else{
                //Return an error because the user send a incorrect token
                throw new SubscriptionException(ResponseConstants.INCORRECT_TOKEN);
            }
        } else {
            //Return an error because the user still did not create the subscription
            throw new SubscriptionException(ResponseConstants.NO_ACTIVE_SUBSCRIPTION);
        }
        return new ResponseEntity<>(resp, resp.getStatus());
    }

    @ApiOperation(value = "Subscribes to all lumino open channel events",
            response = DTOResponse.class, responseContainer = ControllerConstants.LIST_RESPONSE_CONTAINER)
    @RequestMapping(value = "/subscribeToLuminoOpenChannels", method = RequestMethod.POST, produces = {ControllerConstants.CONTENT_TYPE_APPLICATION_JSON})
    @ResponseBody
    public ResponseEntity<DTOResponse> subscribeToLuminoOpenChannels(
            @RequestParam(name = "participantone", required = false) String participantOne,
            @RequestParam(name = "participanttwo", required = false) String participantTwo,
            @RequestParam(name = "subscriptionHash") String subscriptionHash,
            @RequestHeader(value="userAddress") String userAddress,
            @RequestHeader(value="apiKey") String apiKey) throws LoginException {
        DTOResponse resp = new DTOResponse();
        User us = userServices.authenticate(userAddress, apiKey);
        //Check if the user did subscribe and has an active subscription and owner of the subscriptionhash
        Subscription sub = subscribeServices.getActiveSubscriptionByHashAndUserAddress(subscriptionHash, userAddress);
        if (sub != null) {
            List<SubscriptionResponse> lstTopicId = new ArrayList<>();
            luminoEventServices.getTokens().forEach(token -> {
                Topic openChannelTopic = luminoEventServices.getChannelOpenedTopicForToken(token, participantOne, participantTwo);
                Topic topic = subscribeServices.getTopicByHashCodeAndIdSubscription(openChannelTopic, sub.getId());
                if(topic == null) {
                    lstTopicId.add(subscribeServices.subscribeToTopic(openChannelTopic, sub));
                }else{
                    //Return an error because the user is sending a topic that he's already subscribed
                    lstTopicId.add(new SubscriptionResponse(topic.getId()));
                    throw new SubscriptionException(ResponseConstants.AlREADY_SUBSCRIBED_TO_SOME_TOPICS, lstTopicId, null);
                }
            });
            resp.setContent(lstTopicId);
        } else {
            //Return an error because the user still did not create the subscription
            throw new SubscriptionException(ResponseConstants.NO_ACTIVE_SUBSCRIPTION);
        }
        return new ResponseEntity<>(resp, resp.getStatus());
    }
}
