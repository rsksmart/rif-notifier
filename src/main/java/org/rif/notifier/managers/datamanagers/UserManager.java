package org.rif.notifier.managers.datamanagers;

import org.rif.notifier.models.entities.User;
import org.rif.notifier.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserManager {
    @Autowired
    private UserRepository userRepository;

    public User insert(String address, String apiKey){
        User rd = new User(address, apiKey);
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
