package org.rif.notifier.repositories;

import org.rif.notifier.models.entities.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Integer> {
    SubscriptionPlan findById(int id);
    Optional<SubscriptionPlan> findByIdAndStatusTrue(int id);
    SubscriptionPlan findByNotificationQuantity(int notifications);
    SubscriptionPlan findByName(String name);
}
