package com.youssef.Subscription_Based.SaaS.Billing.System.services.subscription;

import com.stripe.exception.StripeException;
import com.youssef.Subscription_Based.SaaS.Billing.System.dto.*;
import com.youssef.Subscription_Based.SaaS.Billing.System.entities.subscriptions.Subscription;

import java.util.List;

public interface SubscriptionService {
    UnsubscribeResponse  cancelActiveSubscription() throws StripeException;

    ViewSubscriptionResponse getSubscriptions();
    List<ViewSubscriptionHistoryResponse> getSubscriptionsHistory();
}
