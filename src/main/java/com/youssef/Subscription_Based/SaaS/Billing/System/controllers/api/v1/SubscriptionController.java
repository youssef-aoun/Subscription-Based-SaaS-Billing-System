package com.youssef.Subscription_Based.SaaS.Billing.System.controllers.api.v1;

import com.stripe.exception.StripeException;
import com.youssef.Subscription_Based.SaaS.Billing.System.dto.UnsubscribeResponse;
import com.youssef.Subscription_Based.SaaS.Billing.System.dto.ViewSubscriptionHistoryResponse;
import com.youssef.Subscription_Based.SaaS.Billing.System.dto.ViewSubscriptionResponse;
import com.youssef.Subscription_Based.SaaS.Billing.System.services.subscription.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @Autowired
    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @PostMapping("/cancel")
    public ResponseEntity<UnsubscribeResponse> unsubscribe() throws StripeException {
        UnsubscribeResponse response = subscriptionService.cancelActiveSubscription();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<ViewSubscriptionResponse> getSubscriptions(){
        ViewSubscriptionResponse response = subscriptionService.getSubscriptions();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<List<ViewSubscriptionHistoryResponse>> getSubscriptionsHistory(){
        List<ViewSubscriptionHistoryResponse> response = subscriptionService.getSubscriptionsHistory();
        return ResponseEntity.ok(response);
    }

}
