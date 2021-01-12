package org.rif.notifier.repositories;

import org.rif.notifier.models.entities.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Integer> {
    SubscriptionPlan findById(int id);
    SubscriptionPlan findByNotificationAmount(int notifications);
    SubscriptionPlan findByName(String name);
}
