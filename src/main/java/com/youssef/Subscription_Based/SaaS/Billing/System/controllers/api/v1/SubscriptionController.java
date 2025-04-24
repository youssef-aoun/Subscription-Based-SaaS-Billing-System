package com.youssef.Subscription_Based.SaaS.Billing.System.controllers.api.v1;

import com.youssef.Subscription_Based.SaaS.Billing.System.dto.SubscriptionRequest;
import com.youssef.Subscription_Based.SaaS.Billing.System.dto.SubscriptionResponse;
import com.youssef.Subscription_Based.SaaS.Billing.System.dto.UnsubscribeResponse;
import com.youssef.Subscription_Based.SaaS.Billing.System.dto.ViewSubscriptionResponse;
import com.youssef.Subscription_Based.SaaS.Billing.System.services.subscription.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @Autowired
    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @PostMapping
    public ResponseEntity<SubscriptionResponse> subscribe(@RequestBody SubscriptionRequest request){
        SubscriptionResponse response = subscriptionService.subscribeToPlan(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cancel")
    public ResponseEntity<UnsubscribeResponse> unsubscribe(){
        UnsubscribeResponse response = subscriptionService.unsubscribe();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<ViewSubscriptionResponse> getSubscriptions(){
        ViewSubscriptionResponse response = subscriptionService.getSubscriptions();
        return ResponseEntity.ok(response);
    }


}
