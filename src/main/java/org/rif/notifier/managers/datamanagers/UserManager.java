package org.rif.notifier.managers.datamanagers;

import org.rif.notifier.models.entities.User;
import org.rif.notifier.repositories.UserRepository;
import org.rif.notifier.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserManager {
    @Autowired
    private UserRepository userRepository;

    public User insert(String address, String apiKey){
        String hashedKey = Utils.hashPassword(apiKey);
        User rd = new User(address, hashedKey);
        User result = userRepository.save(rd);
        return result;
    }

    public User getUserByApikey(String apiKey){
        return userRepository.findByApiKey(apiKey);
    }

    public User getUserByAddress(String address){
        return userRepository.findByAddress(address);
    }
}
