package com.youssef.Subscription_Based.SaaS.Billing.System.dao;

import com.youssef.Subscription_Based.SaaS.Billing.System.entities.subscriptions.Subscription;
import com.youssef.Subscription_Based.SaaS.Billing.System.entities.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByUserAndStatus(User user, String status);
    Optional<Subscription> findFirstByUserAndStatusIn(User user, List<String> statuses);
    Optional<Subscription> findByStripeSubscriptionId(String id);
}
