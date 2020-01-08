package org.rif.notifier.repositories;

import org.rif.notifier.models.entities.TopicParams;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopicParamsRepository extends JpaRepository<TopicParams, String> {
}
