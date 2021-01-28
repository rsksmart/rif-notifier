package org.rif.notifier.repositories;

import org.rif.notifier.models.entities.Subscription;
import org.rif.notifier.models.entities.SubscriptionPlan;
import org.rif.notifier.models.entities.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, String> {

    List<Subscription> findByUserAddress(String user_address);

    Subscription findByUserAddressAndSubscriptionPlan(String user_address, SubscriptionPlan subscriptionPlan);

    List<Subscription> findByUserAddressAndStatus(String user_address, SubscriptionStatus status);

    Subscription findByUserAddressAndSubscriptionPlanAndStatus(String user_address, SubscriptionPlan subscriptionPlan, SubscriptionStatus status);

    List<Subscription> findByStatus(SubscriptionStatus subscriptionStatus);

    Subscription findByHash(String hash);

    @Query(value = "SELECT * FROM subscription A WHERE A.status = 'ACTIVE' AND A.notification_balance > 0", nativeQuery = true)
    List<Subscription> findByActiveWithBalance();

    @Query(value = "SELECT * FROM subscription A JOIN user_topic B ON A.id=B.id_subscription AND A.status = 'ACTIVE' AND B.id_topic = ?1", nativeQuery = true)
    List<Subscription> findByIdTopicAndSubscriptionActive(int id);

    @Query(value = "SELECT * FROM subscription A JOIN user_topic B ON A.id=B.id_subscription JOIN topic_params C ON B.id_topic=C.id_topic AND A.status = 'ACTIVE' AND C.param_type = \"CONTRACT_ADDRESS\" AND C.value = ?1", nativeQuery = true)
    List<Subscription> findByContractAddressAndSubscriptionActive(String address);

    @Query(value = "SELECT * FROM subscription A JOIN user_topic B ON A.id=B.id_subscription AND A.status = 'ACTIVE' AND A.notification_balance > 0 AND B.id_topic = ?1", nativeQuery = true)
    List<Subscription> findByIdTopicAndSubscriptionActiveAndPositiveBalance(int id);

}
