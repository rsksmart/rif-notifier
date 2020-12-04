package org.rif.notifier.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.rif.notifier.constants.ControllerConstants;
import org.rif.notifier.constants.ResponseConstants;
import org.rif.notifier.exception.ResourceNotFoundException;
import org.rif.notifier.exception.ValidationException;
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

import javax.persistence.PersistenceException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;



@Api(tags = {"Notification Preference Resource"})
@RestController

public class NotificationPreferenceController {
    //multiple email addreses separated by comma
    private static final Pattern p = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    private static final Pattern phoneRegex = Pattern.compile("^\\+(?:[0-9] ?){6,14}[0-9]$");

    private static final Logger logger = LoggerFactory.getLogger(NotificationPreferenceController.class);

    @Autowired
    private NotificationPreferenceManager notificationPreferenceManager;

    @Autowired
    private UserServices userServices;

    @Autowired
    private SubscribeServices subscribeServices;

    /**
     *
     * @param apiKey the api key to use
     * @param notificationPreference example contents - {
     *     "notificationService":"API",
     *     "destination":"http://host/notify",
     *     "idTopic":"0",
     *     "destinationParams":{
     *         "apiKey":"test",
     *         "username":"test",
     *         "password":"test"
     *     }
     *     }
     *                              or
     *      {
     *           "notificationService":"EMAIL",
     *           "destination":"123456@abc.com;123@abc.com",
     *           "idTopic":"11",
     *       }
     *                               or
     *
     *                               {
     *           "notificationService":"SMS",
     *           "destination":"+191725245555",
     *           "idTopic":"10",
     *       }     *
     * @return
     */
    @ApiOperation(value = "save notification preferences for subscription and topic and notification service type",
            response = DTOResponse.class, responseContainer = ControllerConstants.LIST_RESPONSE_CONTAINER)
    @RequestMapping(value = "/saveNotificationPreference", method = RequestMethod.POST, produces = {ControllerConstants.CONTENT_TYPE_APPLICATION_JSON})
    @ResponseBody
    public ResponseEntity<DTOResponse> saveNotificationPreference   (
            @RequestHeader(value="apiKey") String apiKey,
            @RequestBody String notificationPreference

    ) {
        DTOResponse resp = new DTOResponse();
        NotificationPreference requestedPreference;
        User apiUser;

        //try {
            //validate apikey
            apiUser = validateApiKeyAndGetUser(apiKey);
            //validate request json
            requestedPreference = validateRequestJson(notificationPreference);
            //validate containing data
            validateRequestNotificationPreference(requestedPreference);
        //}
        //catch(ValidationException e)    {
            //return new ResponseEntity<>(resp, resp.getStatus());
        //}

        Subscription subscription = subscribeServices.getSubscriptionByAddress(apiUser.getAddress());
        //check if notification preference already associated with topic and subscription for given type, if no topic specified, default topic 0 will be used
        NotificationPreference preference = Optional.ofNullable(notificationPreferenceManager.getNotificationPreference(
                        subscription, requestedPreference.getIdTopic(), requestedPreference.getNotificationService()))
                        .orElse(new NotificationPreference());
        preference.setSubscription(subscription);
        //the topic id sent as part of request. This is an integer topic id for ex. 10.
        // When the topic id is set to 0, this preference will be used as default preference for those
        // notifications with topic that don't have notification preference speicied.
        preference.setIdTopic(requestedPreference.getIdTopic());
        //The type of notification service to use. Possible values are SMS, EMAIL, API
        //note the values must all be in upper case as given above
        preference.setNotificationService(requestedPreference.getNotificationService());
        //The destination to use. Email can have destination like 'a@b.com;c@d.com', addresses separated by semicolon
        // or sms can have destination as phone number for examle '+19175245555'
        preference.setDestination(requestedPreference.getDestination());
        //This property will be used only when the notificatonservice is set to API. An example structure can be found on the method comments
        preference.setDestinationParams(requestedPreference.getDestinationParams());
        preference = notificationPreferenceManager.saveNotificationPreference(preference);
        if(preference.getId() > 0) {
            resp.setContent(preference);
        }else{
            throw new PersistenceException(ResponseConstants.SAVE_NOTIFICATION_PREFERENCE_FAILED);
        }
        return new ResponseEntity<>(resp, resp.getStatus());
    }


    private User validateApiKeyAndGetUser(String apiKey) throws ValidationException {
        //Return error, user does not exist
        Optional.ofNullable(apiKey).orElseThrow(()->new ValidationException(ResponseConstants.MISSING_APIKEY));
        return Optional.ofNullable(userServices.getUserByApiKey(apiKey)).orElseThrow(()->new ValidationException(ResponseConstants.INCORRECT_APIKEY));
    }

    @ApiOperation(value = "remove notification preference for subscription and topic and notification service type",
            response = DTOResponse.class, responseContainer = ControllerConstants.LIST_RESPONSE_CONTAINER)
    @RequestMapping(value = "/removeNotificationPreference", method = RequestMethod.POST, produces = {ControllerConstants.CONTENT_TYPE_APPLICATION_JSON})
    @ResponseBody
    public ResponseEntity<DTOResponse> removeNotificationPreference(
            @RequestHeader(value="apiKey") String apiKey,
            @RequestBody String notificationPreference

    ) {
        DTOResponse resp = new DTOResponse();
        NotificationPreference requestedPreference;
        User apiUser;

        //validate apikey
        apiUser = validateApiKeyAndGetUser(apiKey);
        //validate request json
        requestedPreference = validateRequestJson(notificationPreference);

        Subscription subscription = subscribeServices.getSubscriptionByAddress(apiUser.getAddress());
        //check if notification preference already associated with topic and subscription for given type
        NotificationPreference preference = Optional.ofNullable(notificationPreferenceManager.getNotificationPreference(
                        subscription, requestedPreference.getIdTopic(), requestedPreference.getNotificationService()))
                        .orElseThrow(()->new ResourceNotFoundException(ResponseConstants.PREFERENCE_NOT_FOUND));

        notificationPreferenceManager.removeNotificationPreference(preference);
        resp.setStatus(HttpStatus.OK);
        resp.setContent(ResponseConstants.PREFERENCE_REMOVED);
        return new ResponseEntity<>(resp, resp.getStatus());
    }


    private void validateRequestNotificationPreference(NotificationPreference preference)   throws ValidationException {
        if (preference.getNotificationService() == NotificationServiceType.EMAIL) {
            validateEmail(preference);
        }
        else if (preference.getNotificationService() == NotificationServiceType.SMS) {
            validateSMS(preference);
        }
    }

    private void validateSMS(NotificationPreference preference) throws ValidationException  {
        //validate phone with international prefix +13475555555
        if (!phoneRegex.matcher(preference.getDestination()).matches()) {
                throw new ValidationException(ResponseConstants.INVALID_PHONE_NUMBER);
        }
    }

    private void validateEmail(NotificationPreference preference) {
        //validate email in case of email service type
        List<String> emails = Arrays.asList(preference.getDestination().split(";"));
        emails.forEach(email->{
            if(!p.matcher(email).matches())   {
                throw new ValidationException(ResponseConstants.INVALID_EMAIL_ADDRESS);
            }
        });
    }

    private NotificationPreference validateRequestJson(String notificationPreference)  {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(notificationPreference, NotificationPreference.class);
        }catch(IOException e)   {
            throw new ValidationException(ResponseConstants.PREFERENCE_VALIDATION_FAILED, e);
        }
    }
}
