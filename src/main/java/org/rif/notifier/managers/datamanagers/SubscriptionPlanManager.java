package org.rif.notifier.managers.datamanagers;

import org.rif.notifier.models.entities.SubscriptionPlan;
import org.rif.notifier.repositories.SubscriptionPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionPlanManager {

    @Autowired
    private SubscriptionPlanRepository subscriptionPlanRepository;

    public SubscriptionPlan getSubscriptionPlanById(int id){
        return subscriptionPlanRepository.findById(id);
    }
}
