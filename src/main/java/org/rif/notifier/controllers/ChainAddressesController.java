package org.rif.notifier.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.rif.notifier.constants.ControllerConstants;
import org.rif.notifier.constants.ResponseConstants;
import org.rif.notifier.exception.SubscriptionException;
import org.rif.notifier.exception.ValidationException;
import org.rif.notifier.models.DTO.DTOResponse;
import org.rif.notifier.models.entities.ChainAddressEvent;
import org.rif.notifier.models.entities.Subscription;
import org.rif.notifier.models.entities.User;
import org.rif.notifier.services.ChainAddressesServices;
import org.rif.notifier.services.SubscribeServices;
import org.rif.notifier.services.UserServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.LoginException;
import javax.validation.Valid;
import java.util.List;
import java.util.Set;

@Api(tags = {"Chain addresses event Resource"})
@RestController
public class ChainAddressesController {

    private static final Logger logger = LoggerFactory.getLogger(ChainAddressesController.class);

    @Autowired
    private ChainAddressesServices chainAddressesServices;

    @Autowired
    private UserServices userServices;

    @Autowired
    private SubscribeServices subscribeServices;

    @ApiOperation(value = "Retrieve chain addresses event",
            response = DTOResponse.class, responseContainer = ControllerConstants.LIST_RESPONSE_CONTAINER)
    @RequestMapping(value = "/getRnsEvents", method = RequestMethod.GET, produces = {ControllerConstants.CONTENT_TYPE_APPLICATION_JSON})
    @ResponseBody
    public ResponseEntity<DTOResponse> getRnsEvents(
            @RequestHeader(name = "userAddress") String userAddress,
            @RequestHeader(value="apiKey") String apiKey,
            @RequestParam(name = "nodehash", required = false) String nodehash,
            @RequestParam(name = "eventName", required = false) Set<String> eventName
    ) throws LoginException  {
        DTOResponse resp = new DTOResponse();
        List<ChainAddressEvent> chainAddresses;
        if(apiKey != null && !apiKey.isEmpty()){
            User us = userServices.authenticate(userAddress, apiKey);
            List<Subscription> subscriptions = subscribeServices.getSubscriptionByAddress(us.getAddress());
            chainAddresses = chainAddressesServices.getChainAddresses(us.getAddress(), nodehash, eventName);
            if(chainAddresses.size() > 0) {
                resp.setContent(chainAddresses);
            }else{
                //It may be happend that the user has no notifications cause the balance of the subscription is 0
                if(subscriptions.stream().noneMatch(s->s.isActive()))   {
                    throw new SubscriptionException(ResponseConstants.NO_ACTIVE_SUBSCRIPTION);
                }
            }
        }else{
            //Return error, user does not exist
            throw new ValidationException(ResponseConstants.MISSING_APIKEY);
        }
        return new ResponseEntity<>(resp, resp.getStatus());
    }
}
