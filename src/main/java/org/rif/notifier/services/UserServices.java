package org.rif.notifier.services;

import org.rif.notifier.constants.ResponseConstants;
import org.rif.notifier.exception.ValidationException;
import org.rif.notifier.managers.DbManagerFacade;
import org.rif.notifier.models.entities.User;
import org.rif.notifier.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.security.auth.login.CredentialException;
import javax.security.auth.login.CredentialNotFoundException;
import javax.security.auth.login.LoginException;

@Service
public class UserServices {
    @Autowired
    private DbManagerFacade dbManagerFacade;

    /**
     * Checks if a user exists in the table User, and returns true in case that exists
     * @param address will be used to check if the user is registered
     * @return boolean value, true if the address exists, false otherwise
     */
    public User userExists(String address){
        return dbManagerFacade.getUserByAddress(address);
    }

    /**
     * Gets a user by its ApiKey
     * @param apiKey Param given when you register a user
     * @return User entity
     */
    public User getUserByApiKey(String apiKey){
        return dbManagerFacade.getUserByApiKey(apiKey);
    }

    /**
     * Given an address creates a new apiKey and calls the dbManager to store it
     * @param address User address
     * @return User stored in the DB
     */
    public User saveUser(String address){
        if (address ==null) {
            return null;
        }
        String apiKey = Utils.generateNewToken();
        User user = dbManagerFacade.saveUser(address, apiKey);
        user.setPlainTextKey(apiKey);
        return user;
    }


    /**
     * Authenticates a given user using address and apikey
     * @param userAddress
     * @param apiKey
     * @return user - authenticated user, or throw exception if no user
     * @throws ValidationException if address or apikey is incorrect
     */
    public User authenticate(String userAddress, String apiKey) throws LoginException {
        User user = userExists(userAddress);
        if(user == null) {
            throw new CredentialNotFoundException(ResponseConstants.INCORRECT_USER_ADDRESS);
        }
        if(!Utils.checkPassword(apiKey, user.getApiKey()))   {
            throw new CredentialException(ResponseConstants.INCORRECT_APIKEY_USER);
        }
        return user;
    }
}
