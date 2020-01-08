package org.rif.notifier.repositories;

import org.rif.notifier.models.entities.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, String> {

    Subscription findByUserAddress(String user_address);

    Subscription findByUserAddressAndActive(String user_address, boolean active);

    List<Subscription> findByActive(int active);

    @Query(value = "SELECT * FROM subscription A WHERE A.active = 1 AND A.notification_balance > 0", nativeQuery = true)
    List<Subscription> findByActiveWithBalance();

    @Query(value = "SELECT * FROM subscription A JOIN user_topic B ON A.id=B.id_subscription AND A.active = 1 AND B.id_topic = ?1", nativeQuery = true)
    List<Subscription> findByIdTopicAndSubscriptionActive(int id);

    @Query(value = "SELECT * FROM subscription A JOIN user_topic B ON A.id=B.id_subscription JOIN topic_params C ON B.id_topic=C.id_topic AND A.active = 1 AND C.param_type = \"CONTRACT_ADDRESS\" AND C.value = ?1", nativeQuery = true)
    List<Subscription> findByContractAddressAndSubscriptionActive(String address);

    @Query(value = "SELECT * FROM subscription A JOIN user_topic B ON A.id=B.id_subscription AND A.active = 1 AND A.notification_balance > 0 AND B.id_topic = ?1", nativeQuery = true)
    List<Subscription> findByIdTopicAndSubscriptionActiveAndPositiveBalance(int id);
}
