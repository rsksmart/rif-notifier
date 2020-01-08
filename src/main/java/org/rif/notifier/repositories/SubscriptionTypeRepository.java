package org.rif.notifier.repositories;

import org.rif.notifier.models.entities.SubscriptionType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionTypeRepository extends JpaRepository<SubscriptionType, String> {
    SubscriptionType findById(int id);
}
