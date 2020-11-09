package org.rif.notifier.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.tools.javac.util.Pair;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.rif.notifier.constants.ControllerConstants;
import org.rif.notifier.constants.ResponseConstants;
import org.rif.notifier.models.DTO.DTOResponse;
import org.rif.notifier.models.DTO.SubscriptionResponse;
import org.rif.notifier.models.entities.*;
import org.rif.notifier.services.LuminoEventServices;
import org.rif.notifier.services.SubscribeServices;
import org.rif.notifier.services.UserServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Api(tags = {"Onboarding Resource"})
@RestController
public class SubscribeController {
    private static final Logger logger = LoggerFactory.getLogger(SubscribeController.class);

    private SubscribeServices subscribeServices;
    private UserServices userServices;
    private LuminoEventServices luminoEventServices;

    @Autowired
    public SubscribeController(SubscribeServices subscribeServices, UserServices userServices, LuminoEventServices luminoEventServices) {
        this.subscribeServices = subscribeServices;
        this.userServices = userServices;
        this.luminoEventServices = luminoEventServices;
    }

    @ApiOperation(value = "Generate a subscription with an Apikey",
            response = DTOResponse.class, responseContainer = ControllerConstants.LIST_RESPONSE_CONTAINER)
    @RequestMapping(value = "/subscribe", method = RequestMethod.POST, produces = {ControllerConstants.CONTENT_TYPE_APPLICATION_JSON})
    @ResponseBody
    public ResponseEntity<DTOResponse> subscribe(
            @RequestParam(name = "type", required = false) Integer type,
            @RequestHeader(value="apiKey") String apiKey) {
        DTOResponse resp = new DTOResponse();
        User us = userServices.getUserByApiKey(apiKey);
        if(us != null){
//            SubscriptionType subType = subscribeServices.getSubscriptionTypeByType(type);
//            if(subType != null) {
                if (subscribeServices.getActiveSubscriptionByAddress(us.getAddress()) == null) {
                    SubscriptionType subTypeMocked = new SubscriptionType(Integer.MAX_VALUE);
                    resp.setContent(subscribeServices.createSubscription(us, subTypeMocked));
                }else{
                    resp.setMessage(ResponseConstants.SUBSCRIPTION_ALREADY_ADDED);
                    resp.setStatus(HttpStatus.CONFLICT);
                }
//            }else{
//                resp.setMessage(ResponseConstants.SUBSCRIPTION_INCORRECT_TYPE);
//                resp.setStatus(HttpStatus.CONFLICT);
//            }
        }else{
            resp.setMessage(ResponseConstants.INCORRECT_APIKEY);
            resp.setStatus(HttpStatus.CONFLICT);
        }

        return new ResponseEntity<>(resp, resp.getStatus());
    }
    @ApiOperation(value = "Subscribes to a topic",
            response = DTOResponse.class, responseContainer = ControllerConstants.LIST_RESPONSE_CONTAINER)
    @RequestMapping(value = "/subscribeToTopic", method = RequestMethod.POST, produces = {ControllerConstants.CONTENT_TYPE_APPLICATION_JSON})
    @ResponseBody
    public ResponseEntity<DTOResponse> subscribeToTopic(
            @RequestHeader(value="apiKey") String apiKey,
            @RequestBody String userTopic) {
        ObjectMapper mapper = new ObjectMapper();
        Topic userSendedTopic = null;
        DTOResponse resp = new DTOResponse();
        try {
            userSendedTopic = mapper.readValue(userTopic, Topic.class);
            User us = userServices.getUserByApiKey(apiKey);
            if(us != null){
                //Check if the user did subscribe
                Subscription sub = subscribeServices.getSubscriptionByAddress(us.getAddress());
                if(sub != null) {
                    if(subscribeServices.validateTopic(userSendedTopic)){
                        Topic topic = subscribeServices.getTopicByHashCodeAndIdSubscription(userSendedTopic, sub.getId());
                        if(topic == null) {
                            resp.setContent(subscribeServices.subscribeToTopic(userSendedTopic, sub));
                        }else{
                            //Return an error because the user is sending a topic that he's already subscribed
                            resp.setMessage(ResponseConstants.AlREADY_SUBSCRIBED_TO_TOPIC);
                            resp.setContent(new SubscriptionResponse(topic.getId()));
                            resp.setStatus(HttpStatus.CONFLICT);
                        }
                    }else{
                        //Return an error because the user sends a wrong structure of topic
                        resp.setMessage(ResponseConstants.TOPIC_VALIDATION_FAILED);
                        resp.setStatus(HttpStatus.CONFLICT);
                    }
                }else{
                    //Return an error because the user still did not create the subscription
                    resp.setMessage(ResponseConstants.NO_ACTIVE_SUBSCRIPTION);
                    resp.setStatus(HttpStatus.CONFLICT);
                }
            }else{
                resp.setMessage(ResponseConstants.INCORRECT_APIKEY);
                resp.setStatus(HttpStatus.CONFLICT);
            }
        } catch (IOException e) {
            resp.setMessage(ResponseConstants.TOPIC_VALIDATION_FAILED);
            resp.setStatus(HttpStatus.CONFLICT);
        }

        return new ResponseEntity<>(resp, resp.getStatus());
    }
    @ApiOperation(value = "Gets the subscription info",
            response = DTOResponse.class, responseContainer = ControllerConstants.LIST_RESPONSE_CONTAINER)
    @RequestMapping(value = "/getSubscriptionInfo", method = RequestMethod.GET, produces = {ControllerConstants.CONTENT_TYPE_APPLICATION_JSON})
    @ResponseBody
    public ResponseEntity<DTOResponse> getSubscriptionInfo(
            @RequestHeader(value="apiKey") String apiKey) {
        DTOResponse resp = new DTOResponse();
        User us = userServices.getUserByApiKey(apiKey);
        if (us != null) {
            //Check if the user did subscribe
            Subscription sub = subscribeServices.getSubscriptionByAddress(us.getAddress());
            if (sub != null) {
                resp.setContent(sub.toStringInfo());
            } else {
                //Return an error because the user still did not create the subscription
                resp.setMessage(ResponseConstants.SUBSCRIPTION_NOT_FOUND);
                resp.setStatus(HttpStatus.CONFLICT);
            }
        } else {
            resp.setMessage(ResponseConstants.INCORRECT_APIKEY);
            resp.setStatus(HttpStatus.CONFLICT);
        }

        return new ResponseEntity<>(resp, resp.getStatus());
    }
    @ApiOperation(value = "Gets all preloaded events",
            response = DTOResponse.class, responseContainer = ControllerConstants.LIST_RESPONSE_CONTAINER)
    @RequestMapping(value = "/getLuminoTokens", method = RequestMethod.GET, produces = {ControllerConstants.CONTENT_TYPE_APPLICATION_JSON})
    @ResponseBody
    public ResponseEntity<DTOResponse> getLuminoTokens(
            @RequestHeader(value="apiKey") String apiKey) {
        DTOResponse resp = new DTOResponse();
        User us = userServices.getUserByApiKey(apiKey);
        if (us != null) {
            //Check if the user did subscribe
            Subscription sub = subscribeServices.getSubscriptionByAddress(us.getAddress());
            if (sub != null) {
                resp.setContent(luminoEventServices.getTokens());
            } else {
                //Return an error because the user still did not create the subscription
                resp.setMessage(ResponseConstants.SUBSCRIPTION_NOT_FOUND);
                resp.setStatus(HttpStatus.CONFLICT);
            }
        } else {
            resp.setMessage(ResponseConstants.INCORRECT_APIKEY);
            resp.setStatus(HttpStatus.CONFLICT);
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
            @RequestHeader(value="apiKey") String apiKey) {
        DTOResponse resp = new DTOResponse();
        User us = userServices.getUserByApiKey(apiKey);
        if (us != null) {
            //Check if the user did subscribe
            Subscription sub = subscribeServices.getSubscriptionByAddress(us.getAddress());
            if (sub != null) {
                token = token.toLowerCase();
                if(luminoEventServices.isToken(token)){
                    Topic openChannelTopic = luminoEventServices.getChannelOpenedTopicForToken(token, participantOne, participantTwo);
                    Topic topic = subscribeServices.getTopicByHashCodeAndIdSubscription(openChannelTopic, sub.getId());
                    if(topic == null) {
                        resp.setContent(subscribeServices.subscribeToTopic(openChannelTopic, sub));
                    }else{
                        //Return an error because the user is sending a topic that he's already subscribed
                        resp.setMessage(ResponseConstants.AlREADY_SUBSCRIBED_TO_TOPIC);
                        resp.setContent(new SubscriptionResponse(topic.getId()));
                        resp.setStatus(HttpStatus.CONFLICT);
                    }
                }else{
                    //Return an error because the user send a incorrect token
                    resp.setMessage(ResponseConstants.INCORRECT_TOKEN);
                    resp.setStatus(HttpStatus.CONFLICT);
                }
            } else {
                //Return an error because the user still did not create the subscription
                resp.setMessage(ResponseConstants.SUBSCRIPTION_NOT_FOUND);
                resp.setStatus(HttpStatus.CONFLICT);
            }
        } else {
            resp.setMessage(ResponseConstants.INCORRECT_APIKEY);
            resp.setStatus(HttpStatus.CONFLICT);
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
            @RequestHeader(value="apiKey") String apiKey) {
        DTOResponse resp = new DTOResponse();
        User us = userServices.getUserByApiKey(apiKey);
        if (us != null) {
            //Check if the user did subscribe
            Subscription sub = subscribeServices.getSubscriptionByAddress(us.getAddress());
            if (sub != null) {
                token = token.toLowerCase();
                if(luminoEventServices.isToken(token)){
                    Topic closeChannelTopic = luminoEventServices.getChannelClosedTopicForToken(token, channelIdentifier, closingParticipant);
                    Topic topic = subscribeServices.getTopicByHashCodeAndIdSubscription(closeChannelTopic, sub.getId());
                    if(topic == null) {
                        resp.setContent(subscribeServices.subscribeToTopic(closeChannelTopic, sub));
                    }else{
                        //Return an error because the user is sending a topic that he's already subscribed
                        resp.setMessage(ResponseConstants.AlREADY_SUBSCRIBED_TO_TOPIC);
                        resp.setContent(new SubscriptionResponse(topic.getId()));
                        resp.setStatus(HttpStatus.CONFLICT);
                    }
                }else{
                    //Return an error because the user send a incorrect token
                    resp.setMessage(ResponseConstants.INCORRECT_TOKEN);
                    resp.setStatus(HttpStatus.CONFLICT);
                }
            } else {
                //Return an error because the user still did not create the subscription
                resp.setMessage(ResponseConstants.SUBSCRIPTION_NOT_FOUND);
                resp.setStatus(HttpStatus.CONFLICT);
            }
        } else {
            resp.setMessage(ResponseConstants.INCORRECT_APIKEY);
            resp.setStatus(HttpStatus.CONFLICT);
        }

        return new ResponseEntity<>(resp, resp.getStatus());
    }
    @ApiOperation(value = "Unsubscribes from a topic given an id",
            response = DTOResponse.class, responseContainer = ControllerConstants.LIST_RESPONSE_CONTAINER)
    @RequestMapping(value = "/unsubscribeFromTopic", method = RequestMethod.POST, produces = {ControllerConstants.CONTENT_TYPE_APPLICATION_JSON})
    @ResponseBody
    public ResponseEntity<DTOResponse> unsubscribeFromTopic(
            @RequestHeader(value="apiKey") String apiKey,
            @RequestParam(value="idTopic") int idTopic) {
        DTOResponse resp = new DTOResponse();
         User us = userServices.getUserByApiKey(apiKey);
        if(us != null){
            //Check if the user did subscribe
            Subscription sub = subscribeServices.getSubscriptionByAddress(us.getAddress());
            if(sub != null) {
                Topic tp = subscribeServices.getTopicById(idTopic);
                if(tp != null){
                    if(!subscribeServices.unsubscribeFromTopic(sub, tp)){
                        resp.setMessage(ResponseConstants.UNSUBSCRIBED_FROM_TOPIC_FAILED);
                        resp.setStatus(HttpStatus.CONFLICT);
                    }
                }else{
                    resp.setMessage(ResponseConstants.INVALID_TOPIC_ID);
                    resp.setStatus(HttpStatus.CONFLICT);
                }
            }else{
                //Return an error because the user still did not create the subscription
                resp.setMessage(ResponseConstants.NO_ACTIVE_SUBSCRIPTION);
                resp.setStatus(HttpStatus.CONFLICT);
            }
        }else{
            resp.setMessage(ResponseConstants.INCORRECT_APIKEY);
            resp.setStatus(HttpStatus.CONFLICT);
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
            @RequestHeader(value="apiKey") String apiKey) {
        DTOResponse resp = new DTOResponse();
        User us = userServices.getUserByApiKey(apiKey);
        if (us != null) {
            //Check if the user did subscribe
            Subscription sub = subscribeServices.getSubscriptionByAddress(us.getAddress());
            if (sub != null) {
                List<SubscriptionResponse> lstTopicId = new ArrayList<>();
                luminoEventServices.getTokens().forEach(token -> {
                    Topic openChannelTopic = luminoEventServices.getChannelOpenedTopicForToken(token, participantOne, participantTwo);
                    Topic topic = subscribeServices.getTopicByHashCodeAndIdSubscription(openChannelTopic, sub.getId());
                    if(topic == null) {
                        lstTopicId.add(subscribeServices.subscribeToTopic(openChannelTopic, sub));
                    }else{
                        //Return an error because the user is sending a topic that he's already subscribed
                        resp.setMessage(ResponseConstants.AlREADY_SUBSCRIBED_TO_SOME_TOPICS);
                        lstTopicId.add(new SubscriptionResponse(topic.getId()));
                        resp.setStatus(HttpStatus.CONFLICT);
                    }
                });
                resp.setContent(lstTopicId);
            } else {
                //Return an error because the user still did not create the subscription
                resp.setMessage(ResponseConstants.SUBSCRIPTION_NOT_FOUND);
                resp.setStatus(HttpStatus.CONFLICT);
            }
        } else {
            resp.setMessage(ResponseConstants.INCORRECT_APIKEY);
            resp.setStatus(HttpStatus.CONFLICT);
        }

        return new ResponseEntity<>(resp, resp.getStatus());
    }
}
