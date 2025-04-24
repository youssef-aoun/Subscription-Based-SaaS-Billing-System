package com.youssef.Subscription_Based.SaaS.Billing.System.services.subscription;

import com.youssef.Subscription_Based.SaaS.Billing.System.dto.SubscriptionRequest;
import com.youssef.Subscription_Based.SaaS.Billing.System.dto.SubscriptionResponse;
import com.youssef.Subscription_Based.SaaS.Billing.System.dto.UnsubscribeResponse;
import com.youssef.Subscription_Based.SaaS.Billing.System.dto.ViewSubscriptionResponse;

public interface SubscriptionService {
    SubscriptionResponse subscribeToPlan(SubscriptionRequest request);
    UnsubscribeResponse unsubscribe();
    ViewSubscriptionResponse getSubscriptions();
}
