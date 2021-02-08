package org.rif.notifier.controllers;

import io.swagger.annotations.*;
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
import org.rif.notifier.validation.NotificationPreferenceValidator;
import org.rif.notifier.validation.SubscribeValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;

@Api(tags = {"Subscription Plan Resource"})
@Validated
@RestController
public class SubscriptionPlanController {
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionPlanController.class);

    private SubscriptionPlanServices subscriptionPlanServices;

    public SubscriptionPlanController(SubscriptionPlanServices subscriptionPlanServices) {
        this.subscriptionPlanServices = subscriptionPlanServices;
    }


    @ApiOperation(value = "Retrieve all Subscription Plan and Price Details", notes = "Returns http 409 in case of error",
            response = DTOResponse.class, responseContainer = ControllerConstants.LIST_RESPONSE_CONTAINER)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Subscription plans retrieved successfully.", response = DTOResponse.class)
    })
    @GetMapping(value = "/getSubscriptionPlans", produces = {ControllerConstants.CONTENT_TYPE_APPLICATION_JSON})
    @ResponseBody
    public ResponseEntity<DTOResponse> getSubscriptionPlans(
            @ApiParam(required=false, name="specifies whether to fetch only active plans.")
            @RequestParam(name="activePlansOnly", required=false) boolean activePlansOnly) {
        DTOResponse resp = new DTOResponse();
        List<SubscriptionPlan> subscriptionPlans = subscriptionPlanServices.getSubscriptionPlans();
        if(activePlansOnly)
            subscriptionPlans = subscriptionPlans.stream().filter(p->p.isStatus()).collect(Collectors.toList());
        resp.setContent(subscriptionPlans);
        return new ResponseEntity<>(resp, resp.getStatus());
    }

    @ApiOperation(value = "Retrieve Subscription Plan, and Price Details for the given id", notes = "Returns http 409 in case of error",
            response = DTOResponse.class, responseContainer = ControllerConstants.LIST_RESPONSE_CONTAINER)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Subscription plan retrieved successfully.", response = DTOResponse.class),
            @ApiResponse(code = 409, message = "Error retrieving subsription plan.", response = DTOResponse.class)
    })
    @GetMapping(value = "/getSubscriptionPlan/{id}", produces = {ControllerConstants.CONTENT_TYPE_APPLICATION_JSON})
    @ResponseBody
    public ResponseEntity<DTOResponse> getSubscriptionPlan(@PathVariable("id") int id) {
        DTOResponse resp = new DTOResponse();
        SubscriptionPlan plan = Optional.ofNullable(subscriptionPlanServices.getSubscriptionPlan(id))
                .orElseThrow(()->{return new ValidationException(ResponseConstants.SUBSCRIPTION_PLAN_ID_INVALID);} );
        resp.setContent(plan);
        return new ResponseEntity<>(resp, resp.getStatus());
    }
}
