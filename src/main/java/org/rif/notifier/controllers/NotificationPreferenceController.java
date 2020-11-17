package org.rif.notifier.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import okhttp3.Response;
import org.hibernate.validator.internal.constraintvalidators.bv.EmailValidator;
import org.rif.notifier.constants.ControllerConstants;
import org.rif.notifier.constants.ResponseConstants;
import org.rif.notifier.managers.NotificationManager;
import org.rif.notifier.managers.datamanagers.NotificationPreferenceManager;
import org.rif.notifier.models.DTO.DTOResponse;
import org.rif.notifier.models.entities.*;
import org.rif.notifier.services.SubscribeServices;
import org.rif.notifier.services.UserServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Set;



@Api(tags = {"Notification Resource"})
@RestController
public class NotificationPreferenceController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationPreferenceController.class);

    @Autowired
    private NotificationPreferenceManager notifictionPreferenceManager;

    @Autowired
    private UserServices userServices;

    @Autowired
    private SubscribeServices subscribeServices;

    @ApiOperation(value = "save notification preferences for subscription and topic and notification service type",
            response = DTOResponse.class, responseContainer = ControllerConstants.LIST_RESPONSE_CONTAINER)
    @RequestMapping(value = "/saveNotificationPreference", method = RequestMethod.POST, produces = {ControllerConstants.CONTENT_TYPE_APPLICATION_JSON})
    @ResponseBody
    public ResponseEntity<DTOResponse> saveNotificationPreference(
            @RequestHeader(value="apiKey") String apiKey,
            @RequestParam(name = "idTopic", required = false) Integer idTopic,
            @RequestParam(name = "notificationServiceType", required = true) NotificationServiceType serviceType,
            @RequestParam(name = "destination", required = true) String destination,
            @RequestBody String destinationParams

    ) {
        DTOResponse resp = new DTOResponse();
        ObjectMapper mapper = new ObjectMapper();
        List<Notification> notifications;
        if(apiKey != null && !apiKey.isEmpty()){
            User us = userServices.getUserByApiKey(apiKey);
            if(us != null){
                Subscription subscription = subscribeServices.getSubscriptionByAddress(us.getAddress());
                NotificationPreference preference = null;
                if (idTopic == null) {
                    preference = notifictionPreferenceManager.getNotificationPreference(subscription, serviceType);
                }else   {
                    preference = notifictionPreferenceManager.getNotificationPreference(subscription, idTopic, serviceType);
                }
                if (preference == null)  {
                    preference = new NotificationPreference();
                }
                DestinationParams params = null;
                try {
                    params = mapper.readValue(destinationParams, DestinationParams.class);
                } catch(IOException e)  {
                    resp.setMessage(ResponseConstants.INVALID_DESTINATION_PARAMS);
                    resp.setStatus(HttpStatus.CONFLICT);
                }
                preference.setIdTopic(idTopic);
                preference.setSubscription(subscription);
                preference.setNotificationService(serviceType);
                preference.setDestination(destination);
                preference.setDestinationParams(params);
                preference = notifictionPreferenceManager.saveNotificationPreference(preference);
                if(preference.getId() > 0) {
                    resp.setContent(preference);
                }else{
                    resp.setMessage(ResponseConstants.SAVE_NOTIFICATION_PREFERENCE_FAILED);
                    resp.setStatus(HttpStatus.CONFLICT);
                }
                }
            }else{
                //Return error, user does not exist
                resp.setMessage(ResponseConstants.INCORRECT_APIKEY);
                resp.setStatus(HttpStatus.CONFLICT);
            }
            return new ResponseEntity<>(resp, resp.getStatus());
        }
    }
