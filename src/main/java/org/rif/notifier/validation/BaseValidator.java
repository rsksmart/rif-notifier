package org.rif.notifier.validation;

import org.rif.notifier.constants.ResponseConstants;
import org.rif.notifier.exception.ValidationException;
import org.rif.notifier.models.entities.User;
import org.rif.notifier.services.UserServices;

import java.util.Optional;

abstract public class BaseValidator {

    private UserServices userServices;

    public BaseValidator()  {}

    public BaseValidator(UserServices userServices)    {
        this.userServices = userServices;
    }

    public User validateApiKeyAndGetUser(String apiKey) throws ValidationException {
        //Return error, user does not exist
        Optional.ofNullable(apiKey).orElseThrow(()->new ValidationException(ResponseConstants.MISSING_APIKEY));
        return Optional.ofNullable(userServices.getUserByApiKey(apiKey)).orElseThrow(()->new ValidationException(ResponseConstants.INCORRECT_APIKEY));
    }


}
