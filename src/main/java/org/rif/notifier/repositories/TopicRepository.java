package org.rif.notifier.repositories;

import org.rif.notifier.models.entities.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface TopicRepository extends JpaRepository<Topic, String> {
    Topic findById(int id);

    @Query(value = "SELECT * FROM topic A JOIN user_topic B ON A.id=B.id_topic WHERE A.hash = ?1 AND B.id_subscription = ?2", nativeQuery = true)
    Topic findByHashAndIdSubscription(String hash, int idSubscription);

    @Query(value = "SELECT distinct * FROM topic A JOIN user_topic B ON A.id=B.id_topic JOIN subscription C ON c.id=B.id_subscription WHERE c.status='ACTIVE' and c.notification_balance > 0", nativeQuery = true)
    Set<Topic> findAllTopicsWithActiveSubscriptionWithBalance();

    Topic findByHash(String hash);
}
