package com.youssef.Subscription_Based.SaaS.Billing.System.services.payments;

import com.stripe.model.Event;

public interface WebhookService {
    void handleStripeEvent(Event event);
}
