package org.rif.notifier.repositories;

import org.rif.notifier.models.entities.Subscription;
import org.rif.notifier.models.entities.SubscriptionPlan;
import org.rif.notifier.models.entities.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, String> {

    List<Subscription> findByUserAddress(String user_address);

    List<Subscription> findByUserAddressAndSubscriptionPlan(String user_address, SubscriptionPlan subscriptionPlan);

    List<Subscription> findByUserAddressAndStatus(String user_address, SubscriptionStatus status);

    Subscription findByUserAddressAndSubscriptionPlanAndStatus(String user_address, SubscriptionPlan subscriptionPlan, SubscriptionStatus status);

    List<Subscription> findByStatus(SubscriptionStatus subscriptionStatus);

    Subscription findByHash(String hash);

    Subscription findByHashAndUserAddress(String hash, String userAddress);

    Subscription findByPreviousSubscription(Subscription sub);

    @Query(value = "SELECT * FROM subscription A WHERE A.status = 'ACTIVE' AND A.notification_balance > 0", nativeQuery = true)
    List<Subscription> findByActiveWithBalance();

    @Query(value = "SELECT * FROM subscription A JOIN user_topic B ON A.id=B.id_subscription AND A.status = 'ACTIVE' AND B.id_topic = ?1", nativeQuery = true)
    List<Subscription> findByIdTopicAndSubscriptionActive(int id);

    @Query(value = "SELECT * FROM subscription A JOIN user_topic B ON A.id=B.id_subscription JOIN topic_params C ON B.id_topic=C.id_topic AND A.status = 'ACTIVE' AND C.param_type = \"CONTRACT_ADDRESS\" AND C.value = ?1", nativeQuery = true)
    List<Subscription> findByContractAddressAndSubscriptionActive(String address);

    @Query(value = "SELECT * FROM subscription A JOIN user_topic B ON A.id=B.id_subscription AND A.status = 'ACTIVE' AND A.notification_balance > 0 AND B.id_topic = ?1", nativeQuery = true)
    List<Subscription> findByIdTopicAndSubscriptionActiveAndPositiveBalance(int id);

    /**
     * Get all pending subscriptions that have been paid and don't have a pending or active previous subscription
     * @return
     */
    @Query(value = "SELECT DISTINCT cur.* FROM subscription cur JOIN subscription_payment p ON cur.id=p.subscription_id " +
                    "AND p.amount>=cur.price LEFT JOIN subscription prev ON prev.id=cur.previous_subscription_id WHERE " +
                    "(prev.id IS NULL OR prev.status NOT IN  ('ACTIVE')) AND cur.status='PENDING'",nativeQuery = true)
    List<Subscription> findPendingSubscriptions();

    @Query(value="SELECT COUNT(1) FROM Subscription s WHERE s.status <> 'EXPIRED' AND s.expirationDate < CURRENT_DATE")
    int countExpiredSubscriptions();

    @Modifying
    @Query(value = "UPDATE Subscription s SET s.status='EXPIRED' WHERE s.status <> 'EXPIRED' AND s.expirationDate < CURRENT_DATE")
    int updateExpiredSubscriptions();
}
