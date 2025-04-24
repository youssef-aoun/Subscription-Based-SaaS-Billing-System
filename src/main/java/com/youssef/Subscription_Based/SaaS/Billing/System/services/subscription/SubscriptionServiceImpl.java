package com.youssef.Subscription_Based.SaaS.Billing.System.services.subscription;

import com.youssef.Subscription_Based.SaaS.Billing.System.dao.PlanRepository;
import com.youssef.Subscription_Based.SaaS.Billing.System.dao.SubscriptionRepository;
import com.youssef.Subscription_Based.SaaS.Billing.System.dto.SubscriptionRequest;
import com.youssef.Subscription_Based.SaaS.Billing.System.dto.SubscriptionResponse;
import com.youssef.Subscription_Based.SaaS.Billing.System.dto.UnsubscribeResponse;
import com.youssef.Subscription_Based.SaaS.Billing.System.dto.ViewSubscriptionResponse;
import com.youssef.Subscription_Based.SaaS.Billing.System.entities.plan.Plan;
import com.youssef.Subscription_Based.SaaS.Billing.System.entities.subscriptions.Subscription;
import com.youssef.Subscription_Based.SaaS.Billing.System.entities.user.User;
import com.youssef.Subscription_Based.SaaS.Billing.System.services.users.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SubscriptionServiceImpl implements SubscriptionService{

    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;
    private final UserService userService;

    @Autowired
    public SubscriptionServiceImpl(SubscriptionRepository subscriptionRepository, PlanRepository planRepository, UserService userService) {
        this.subscriptionRepository = subscriptionRepository;
        this.planRepository = planRepository;
        this.userService = userService;
    }


    @Override
    public SubscriptionResponse subscribeToPlan(SubscriptionRequest request) {
        User currentUser = userService.getCurrentAuthenticatedUser();

        subscriptionRepository.findByUserAndStatus(currentUser, "ACTIVE").ifPresent(sub -> {
            throw new IllegalStateException("User already has an active subscription.");
        });

        Plan plan = planRepository.findPlanById(request.getPlanId()).orElseThrow(() -> new IllegalArgumentException("Invalid Plan id."));

        Subscription subscription = new Subscription();
        subscription.setUser(currentUser);
        subscription.setPlan(plan);
        subscription.setStatus("ACTIVE");
        subscription.setStartDate(LocalDateTime.now());

        if("MONTHLY".equalsIgnoreCase(plan.getBillingCycle())){
            subscription.setEndDate(LocalDateTime.now().plusMonths(1));
        }else if("YEARLY".equalsIgnoreCase(plan.getBillingCycle())){
            subscription.setEndDate(LocalDateTime.now().plusYears(1));
        }

        Subscription savedSub = subscriptionRepository.save(subscription);

        SubscriptionResponse response = new SubscriptionResponse();
        response.setSubscriptionId(savedSub.getId());
        response.setStatus(savedSub.getStatus());
        response.setPlanName(plan.getName());
        response.setStartDate(savedSub.getStartDate());
        response.setEndDate(savedSub.getEndDate());

        return response;
    }

    @Override
    public UnsubscribeResponse unsubscribe() {
        User currentUser = userService.getCurrentAuthenticatedUser();
        Subscription subscription = subscriptionRepository
                .findByUserAndStatus(
                        currentUser,
                        "ACTIVE")
                .orElseThrow(() -> new IllegalStateException("No active subscriptions."));

        subscription.setStatus("CANCELED");
        subscription.setEndDate(LocalDateTime.now());

        subscriptionRepository.save(subscription);

        UnsubscribeResponse response = new UnsubscribeResponse();
        response.setMessage("Subscription canceled successfully.");
        response.setSubscriptionId(subscription.getId());
        response.setCanceledAt(LocalDateTime.now());

        return response;
    }

    @Override
    public ViewSubscriptionResponse getSubscriptions() {
        User currentUser = userService.getCurrentAuthenticatedUser();

        Subscription subscription = subscriptionRepository.findByUserAndStatus(
                currentUser,
                "ACTIVE"
        ).orElseThrow(() -> new IllegalStateException("No active subscription found"));


        ViewSubscriptionResponse response = new ViewSubscriptionResponse();

        Plan plan = subscription.getPlan();

        response.setSubscriptionId(subscription.getId());
        response.setBillingCycle(plan.getBillingCycle());
        response.setStatus("ACTIVE");
        response.setStartDate(subscription.getStartDate());
        response.setEndDate(subscription.getEndDate());
        response.setPlanName(plan.getName());
        return response;
    }
}
