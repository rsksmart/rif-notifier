package org.rif.notifier.controllers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.rif.notifier.constants.ControllerConstants;
import org.rif.notifier.constants.ResponseConstants;
import org.rif.notifier.exception.SubscriptionException;
import org.rif.notifier.exception.ValidationException;
import org.rif.notifier.models.DTO.DTOResponse;
import org.rif.notifier.models.entities.Notification;
import org.rif.notifier.models.entities.Subscription;
import org.rif.notifier.models.entities.User;
import org.rif.notifier.services.NotificationServices;
import org.rif.notifier.services.SubscribeServices;
import org.rif.notifier.services.UserServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Api(tags = {"Notification Resource"})
@RestController
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @Autowired
    private NotificationServices notificationServices;

    @Autowired
    private UserServices userServices;

    @Autowired
    private SubscribeServices subscribeServices;

    @ApiOperation(value = "Retrieve notifications for an address",
            response = DTOResponse.class, responseContainer = ControllerConstants.LIST_RESPONSE_CONTAINER)
    @RequestMapping(value = "/getNotifications", method = RequestMethod.GET, produces = {ControllerConstants.CONTENT_TYPE_APPLICATION_JSON})
    @ResponseBody
    public ResponseEntity<DTOResponse> GetNotifications(
            @RequestHeader(value="apiKey") String apiKey,
            @RequestParam(name = "fromId", required = false) Integer id,
            @RequestParam(name = "lastRows", required = false) Integer lastRows,
            @RequestParam(name = "idTopic", required = false) Set<Integer> idTopic
    ) {
        DTOResponse resp = new DTOResponse();
        List<Notification> notifications;
        if(apiKey != null && !apiKey.isEmpty()){
            User us = userServices.getUserByApiKey(apiKey);
            if(us != null){
                Subscription subscription = subscribeServices.getSubscriptionByAddress(us.getAddress());
                notifications = notificationServices.getNotificationsForSubscription(subscription, id, lastRows, idTopic);
                if(notifications.size() > 0) {
                    resp.setContent(notifications);
                }else{
                    //It may be happend that the user has no notifications cause the balance of the subscription is 0
                    if(subscription.getNotificationBalance() == 0) {
                        throw new SubscriptionException(ResponseConstants.SUBSCRIPTION_OUT_OF_BALANCE);
                    }
                }
            }else{
                //Return error, user does not exist
                throw new ValidationException(ResponseConstants.INCORRECT_APIKEY);
            }
        }else{
            //Return error, user does not exist
            throw new ValidationException(ResponseConstants.MISSING_APIKEY);
        }
        return new ResponseEntity<>(resp, resp.getStatus());
    }

    /**
     * This endpoint is used for mock testing of apiService. takes a valid json input and
     * returns the same as response if request is valid
     * @param apiKey
     * @param requestJson
     * @return
     */
    @ApiOperation(value = "test Endpoint for apiservice",
            response = DTOResponse.class, responseContainer = ControllerConstants.LIST_RESPONSE_CONTAINER)
    @RequestMapping(value = "/testEndpoint", method = RequestMethod.POST, consumes= {ControllerConstants.CONTENT_TYPE_APPLICATION_JSON}, produces = {ControllerConstants.CONTENT_TYPE_APPLICATION_JSON})
    @ResponseBody
    public ResponseEntity<DTOResponse> GetNotifications(
            @RequestHeader(value="apiKey") String apiKey,
            @RequestBody JsonNode requestJson
    ) {
        DTOResponse resp = new DTOResponse();
        resp.setContent(requestJson);
        return new ResponseEntity(resp, resp.getStatus());
    }
}