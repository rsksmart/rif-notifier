package org.rif.notifier.services;

import org.rif.notifier.managers.DbManagerFacade;
import org.rif.notifier.models.entities.User;
import org.rif.notifier.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        String apiKey = Utils.generateNewToken();
        return dbManagerFacade.saveUser(address, apiKey);
    }
}
