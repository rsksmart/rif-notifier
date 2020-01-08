package org.rif.notifier.managers.datamanagers;

import org.rif.notifier.models.entities.SubscriptionType;
import org.rif.notifier.repositories.SubscriptionTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubscriptionTypeManager {

    @Autowired
    private SubscriptionTypeRepository subscriptionTypeRepository;

    public SubscriptionType getSubscriptionTypeById(int id){
        return subscriptionTypeRepository.findById(id);
    }
}
