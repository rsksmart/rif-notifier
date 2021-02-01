package org.rif.notifier.services;

import org.rif.notifier.managers.DbManagerFacade;
import org.rif.notifier.models.entities.SubscriptionPlan;
import org.rif.notifier.models.entities.User;
import org.rif.notifier.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubscriptionPlanServices {
    private DbManagerFacade dbManagerFacade;

    public SubscriptionPlanServices(DbManagerFacade dbManagerFacade)    {
        this.dbManagerFacade = dbManagerFacade;
    }

    public List<SubscriptionPlan> getSubscriptionPlans()   {
        return dbManagerFacade.getSubscriptionPlans();
    }

    public SubscriptionPlan getSubscriptionPlan(int id)   {
        return dbManagerFacade.getSubscriptionPlanById(id);
    }

}
