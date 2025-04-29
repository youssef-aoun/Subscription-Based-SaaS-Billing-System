package com.youssef.Subscription_Based.SaaS.Billing.System.dao;

import com.youssef.Subscription_Based.SaaS.Billing.System.entities.payments.WebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebhookEventRepository extends JpaRepository<WebhookEvent, Long> {
    boolean existsByEventId(String eventId);
}
