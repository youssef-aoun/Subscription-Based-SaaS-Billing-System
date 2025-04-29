package com.youssef.Subscription_Based.SaaS.Billing.System.services.payments;

public interface PaymentService {
    String createCheckoutSession(Long planId);
}
