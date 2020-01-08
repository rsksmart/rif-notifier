package org.rif.notifier.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.rif.notifier.constants.ControllerConstants;
import org.rif.notifier.constants.ResponseConstants;
import org.rif.notifier.models.DTO.DTOResponse;
import org.rif.notifier.models.entities.Notification;
import org.rif.notifier.models.entities.Subscription;
import org.rif.notifier.models.entities.User;
import org.rif.notifier.managers.NotificationManager;
import org.rif.notifier.services.SubscribeServices;
import org.rif.notifier.services.UserServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Api(tags = {"Notification Resource"})
@RestController
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @Autowired
    private NotificationManager notificationManager;

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
                notifications = notificationManager.getNotificationsForAddress(us.getAddress(), id, lastRows, idTopic);
                if(notifications.size() > 0) {
                    resp.setData(notifications);
                }else{
                    //It may be happend that the user has no notifications cause the balance of the subscription is 0
                    if(subscription.getNotificationBalance() == 0) {
                        resp.setMessage(ResponseConstants.SUBSCRIPTION_OUT_OF_BALANCE);
                        resp.setStatus(HttpStatus.CONFLICT);
                    }
                }
            }else{
                //Return error, user does not exist
                resp.setMessage(ResponseConstants.INCORRECT_APIKEY);
                resp.setStatus(HttpStatus.CONFLICT);
            }
        }else{
            //Return error, user does not exist
            resp.setMessage(ResponseConstants.INCORRECT_APIKEY);
            resp.setStatus(HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(resp, resp.getStatus());
    }
}