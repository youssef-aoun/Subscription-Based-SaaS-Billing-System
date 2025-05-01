package com.youssef.Subscription_Based.SaaS.Billing.System.services.subscription;

import com.stripe.exception.StripeException;
import com.stripe.param.SubscriptionUpdateParams;
import com.youssef.Subscription_Based.SaaS.Billing.System.dao.SubscriptionRepository;
import com.youssef.Subscription_Based.SaaS.Billing.System.dto.*;
import com.youssef.Subscription_Based.SaaS.Billing.System.entities.plan.Plan;
import com.youssef.Subscription_Based.SaaS.Billing.System.entities.subscriptions.Subscription;
import com.youssef.Subscription_Based.SaaS.Billing.System.entities.user.User;
import com.youssef.Subscription_Based.SaaS.Billing.System.services.users.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.youssef.Subscription_Based.SaaS.Billing.System.entities.subscriptions.Subscription.ACTIVE;
import static com.youssef.Subscription_Based.SaaS.Billing.System.entities.subscriptions.Subscription.CANCEL_AT_PERIOD_END;

@Service
public class SubscriptionServiceImpl implements SubscriptionService{

    private final SubscriptionRepository subscriptionRepository;
    private final UserService userService;

    @Autowired
    public SubscriptionServiceImpl(SubscriptionRepository subscriptionRepository, UserService userService) {
        this.subscriptionRepository = subscriptionRepository;
        this.userService = userService;
    }

    @Override
    public ViewSubscriptionResponse getSubscriptions() {
        User currentUser = userService.getCurrentAuthenticatedUser();

        Subscription subscription = subscriptionRepository.findFirstByUserAndStatusIn(
                currentUser,
                Arrays.asList(ACTIVE, CANCEL_AT_PERIOD_END)
        ).orElseThrow(() -> new IllegalStateException("No active subscription found"));


        ViewSubscriptionResponse response = new ViewSubscriptionResponse();

        Plan plan = subscription.getPlan();

        response.setSubscriptionId(subscription.getId());
        response.setBillingCycle(plan.getBillingCycle());
        response.setStatus(ACTIVE);
        response.setStartDate(subscription.getStartDate());
        response.setEndDate(subscription.getEndDate());
        response.setPlanName(plan.getName());

        boolean cancelScheduled = CANCEL_AT_PERIOD_END.equals(subscription.getStatus());
        response.setCancelAtPeriodEnd(cancelScheduled);

        return response;
    }

    @Override
    public UnsubscribeResponse cancelActiveSubscription() throws StripeException {

        User user = userService.getCurrentAuthenticatedUser();

        Subscription subscription = subscriptionRepository.findByUserAndStatus(user, ACTIVE)
                .orElseThrow(() -> new IllegalStateException("Could not find an active subscription"));


        String  stripeSubscriptionId = subscription.getStripeSubscriptionId();


        if (stripeSubscriptionId == null) {
            throw new IllegalStateException("Stripe subscription ID not found for user subscription");
        }


        com.stripe.model.Subscription stripeSub = com.stripe.model.Subscription.retrieve(stripeSubscriptionId);

        SubscriptionUpdateParams params = SubscriptionUpdateParams.builder()
                        .setCancelAtPeriodEnd(true)
                        .build();

        stripeSub.update(params);

        // 🛠 Update our local subscription
        subscription.setStatus(CANCEL_AT_PERIOD_END);
        subscriptionRepository.save(subscription);

        // 🛠 Build the response
        UnsubscribeResponse unsubscribeResponse = new UnsubscribeResponse();
        unsubscribeResponse.setSubscriptionId(subscription.getId());
        unsubscribeResponse.setMessage("Your subscription has been scheduled for cancellation.");

        return unsubscribeResponse;
    }

    @Override
    public List<ViewSubscriptionHistoryResponse> getSubscriptionsHistory() {
        User user = userService.getCurrentAuthenticatedUser();
        List<Subscription> subscriptions = subscriptionRepository.findAllByUser(user);

        return subscriptions.stream().map(subscription -> {
            ViewSubscriptionHistoryResponse response = new ViewSubscriptionHistoryResponse();
            response.setSubscriptionId(subscription.getId());
            response.setPlanName(subscription.getPlan().getName());
            response.setBillingCycle(subscription.getPlan().getBillingCycle());
            response.setStatus(subscription.getStatus());
            response.setStartDate(subscription.getStartDate());
            response.setEndDate(subscription.getEndDate());
            if(subscription.getStatus().equals(CANCEL_AT_PERIOD_END))
                response.setCancelAtPeriodEnd(true);
            return response;
        }).collect(Collectors.toList());
    }
}
