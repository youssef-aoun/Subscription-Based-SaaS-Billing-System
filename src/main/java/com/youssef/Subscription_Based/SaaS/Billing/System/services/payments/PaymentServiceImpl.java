package com.youssef.Subscription_Based.SaaS.Billing.System.services.payments;

import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.youssef.Subscription_Based.SaaS.Billing.System.dao.PlanRepository;
import com.youssef.Subscription_Based.SaaS.Billing.System.dao.SubscriptionRepository;
import com.youssef.Subscription_Based.SaaS.Billing.System.entities.plan.Plan;
import com.youssef.Subscription_Based.SaaS.Billing.System.entities.subscriptions.Subscription;
import com.youssef.Subscription_Based.SaaS.Billing.System.entities.user.User;
import com.youssef.Subscription_Based.SaaS.Billing.System.services.users.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static com.youssef.Subscription_Based.SaaS.Billing.System.entities.subscriptions.Subscription.ACTIVE;
import static com.youssef.Subscription_Based.SaaS.Billing.System.entities.subscriptions.Subscription.PENDING;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PlanRepository planRepository;
    private final UserService userService;
    private final SubscriptionRepository subscriptionRepository;

    @Value("${stripe.success.url}")
    private String successUrl;

    @Value("${stripe.cancel.url}")
    private String cancelUrl;

    @Autowired
    public PaymentServiceImpl(PlanRepository planRepository, UserService userService, SubscriptionRepository subscriptionRepository) {
        this.planRepository = planRepository;
        this.userService = userService;
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    public String createCheckoutSession(Long planId) {
        Plan plan = planRepository.findPlanById(planId).orElseThrow(() -> new IllegalArgumentException("Invalid plan id"));

        if(plan.getStripePriceId() == null) {
            throw new IllegalStateException("Plan is not linked to Stripe pricing");
        }

        User user = userService.getCurrentAuthenticatedUser();

        validateOrHandleExistingSubscription(user, plan);

        Subscription subscription = createLocalSubscription(user, plan);
        return buildStripeCheckoutSession(subscription, plan);
    }



    private void validateOrHandleExistingSubscription(User user, Plan plan){
        // 🔐 Duplicate subscription check
        Optional<Subscription> existingSubOpt = subscriptionRepository.findFirstByUserAndStatusIn(
                user,
                Arrays.asList("ACTIVE", "PENDING")
        );

        if (existingSubOpt.isPresent()) {
            Subscription existingSub = existingSubOpt.get();

            if(existingSub.getStatus().equals(PENDING))
                subscriptionRepository.delete(existingSub);
            else if(existingSub.getStatus().equals(ACTIVE)){
                Plan existingPlan = existingSub.getPlan();

                if(isHigherTier(plan, existingPlan)){
                    throw new IllegalStateException("You have an active subscription. Please upgrade instead");
                } else {
                    throw new IllegalStateException("You already have an active subscription.");
                }
            }
        }
    }

    private boolean isHigherTier(Plan requestedPlan, Plan existingPlan){
        return requestedPlan.getPrice() > existingPlan.getPrice();
    }

    private Subscription createLocalSubscription(User user, Plan plan){
        Subscription subscription = new Subscription();

        subscription.setUser(user);
        subscription.setPlan(plan);
        subscription.setStatus(PENDING);
        subscription.setStartDate(LocalDateTime.now());

        return subscriptionRepository.save(subscription);
    }

    private String buildStripeCheckoutSession(Subscription subscription, Plan plan){
        try{

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                    .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(cancelUrl)
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setPrice(plan.getStripePriceId())
                                    .setQuantity(1L)
                                    .build()
                    )
                    .putMetadata("userId", subscription.getUser().getId().toString())
                    .putMetadata("subscriptionId", subscription.getId().toString())
                    .build();

            Session session = Session.create(params);
            return session.getUrl();
        } catch (Exception e) {
            throw new RuntimeException("Stripe Checkout Session creation failed", e);
        }
    }
}
