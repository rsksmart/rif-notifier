package org.rif.notifier.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.rif.notifier.constants.ControllerConstants;
import org.rif.notifier.constants.ResponseConstants;
import org.rif.notifier.models.DTO.DTOResponse;
import org.rif.notifier.models.entities.User;
import org.rif.notifier.services.UserServices;
import org.rif.notifier.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Api(tags = {"Onboarding Resource"})
@RestController
public class UserController {
    @Autowired
    private UserServices userServices;

    @ApiOperation(value = "Register to notifications giving an Address",
            response = DTOResponse.class, responseContainer = ControllerConstants.LIST_RESPONSE_CONTAINER)
    @RequestMapping(value = "/users", method = RequestMethod.POST, produces = {ControllerConstants.CONTENT_TYPE_APPLICATION_JSON})
    @ResponseBody
    public ResponseEntity<DTOResponse> register(
            @RequestParam(name = "address") String address,
            @RequestBody String signedAddress
            ) {
        DTOResponse resp = new DTOResponse();
        if(address != null && !address.isEmpty()){
            if(signedAddress != null && !signedAddress.isEmpty()) {
                if (Utils.canRecoverAddress(address, signedAddress)) {
                    User user = userServices.userExists(address);
                    if (user == null) {
                        resp.setData(userServices.saveUser(address));
                    } else {
                        //User already have an apikey
                        resp.setData(user);
                    }
                } else {
                    resp.setMessage(ResponseConstants.INCORRECT_SIGNED_ADDRESS);
                    resp.setStatus(HttpStatus.CONFLICT);
                }
            }else{
                resp.setMessage(ResponseConstants.SIGNED_ADDRESS_NOT_PROVIDED);
                resp.setStatus(HttpStatus.CONFLICT);
            }
        }else{
            resp.setMessage(ResponseConstants.ADDRESS_NOT_PROVIDED);
            resp.setStatus(HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(resp, resp.getStatus());
    }
}
