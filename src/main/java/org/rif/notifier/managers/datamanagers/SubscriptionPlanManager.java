package org.rif.notifier.managers.datamanagers;

import org.rif.notifier.models.entities.SubscriptionPlan;
import org.rif.notifier.repositories.SubscriptionPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubscriptionPlanManager {

    @Autowired
    private SubscriptionPlanRepository subscriptionPlanRepository;

    public SubscriptionPlan getSubscriptionPlanById(int id){
        return subscriptionPlanRepository.findById(id);
    }

    public List<SubscriptionPlan> getSubscriptionPlans()    {
        return subscriptionPlanRepository.findAll();
    }

    public void save(SubscriptionPlan plan)   {
        subscriptionPlanRepository.save(plan);
    }

    public void saveAll(List<SubscriptionPlan> plan)   {
        subscriptionPlanRepository.saveAll(plan);
    }
}
