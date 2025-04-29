package com.youssef.Subscription_Based.SaaS.Billing.System.services.payments;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.checkout.Session;
import com.youssef.Subscription_Based.SaaS.Billing.System.dao.SubscriptionRepository;
import com.youssef.Subscription_Based.SaaS.Billing.System.dao.WebhookEventRepository;
import com.youssef.Subscription_Based.SaaS.Billing.System.entities.payments.WebhookEvent;
import com.youssef.Subscription_Based.SaaS.Billing.System.entities.subscriptions.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.youssef.Subscription_Based.SaaS.Billing.System.entities.plan.Plan.MONTHLY;
import static com.youssef.Subscription_Based.SaaS.Billing.System.entities.subscriptions.Subscription.ACTIVE;
import static com.youssef.Subscription_Based.SaaS.Billing.System.entities.subscriptions.Subscription.CANCELED;

@Service
public class WebhookServiceImpl implements WebhookService{

    private final WebhookEventRepository webhookEventRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Autowired
    public WebhookServiceImpl(WebhookEventRepository webhookEventRepository, SubscriptionRepository subscriptionRepository) {
        this.webhookEventRepository = webhookEventRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    public void handleStripeEvent(Event event) {
        String eventId = event.getId();

        if(webhookEventRepository.existsByEventId(eventId)){
            return;
        }

        WebhookEvent webhookLog = new WebhookEvent();
        webhookLog.setEventId(eventId);
        webhookLog.setEventType(event.getType());
        webhookLog.setPayload(event.toJson());
        webhookLog.setProcessed(false);
        webhookEventRepository.save(webhookLog);

        try {
            switch (event.getType()) {
                case "checkout.session.completed":
                    handleCheckoutCompleted(event);
                    break;
                case "invoice.payment_failed":
                    handlePaymentFailed(event);
                    break;
                case "customer.subscription.deleted":
                    handleSubscriptionDeleted(event);
                    break;
                default:
                    System.out.println("Unhandled event type: " + event.getType());
            }

            webhookLog.setProcessed(true);
            webhookEventRepository.save(webhookLog);
        }catch (Exception e){
            System.out.println("Error parsing event " + eventId + ": " + e.getMessage());
        }
    }


    private LocalDateTime calculateEndDate(String billingCycle){
        if(billingCycle.equals(MONTHLY))
            return LocalDateTime.now().plusMonths(1);
        return LocalDateTime.now().plusYears(1);
    }

    private void handlePaymentFailed(Event event) {
        System.out.println("Payment failed. Consider notifying user.");
    }

    private void handleCheckoutCompleted(Event event) {
        Session session;

        session = getSession(event);

        Subscription subscription = getSubscription(event);

        subscription.setStripeSubscriptionId(session.getSubscription());
        subscription.setStatus(ACTIVE);
        subscription.setEndDate(calculateEndDate(subscription.getPlan().getBillingCycle()));

        subscriptionRepository.save(subscription);

        System.out.println("✅ Subscription " + subscription.getId() + " activated for user: " + subscription.getUser().getEmail());
    }

    private void handleSubscriptionDeleted(Event event) {
        Subscription subscription = getSubscription(event);
        subscription.setStatus(CANCELED);
        subscriptionRepository.save(subscription);
    }

    private Session getSession(Event event){
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        if (dataObjectDeserializer.getObject().isPresent()) {
            return (Session) dataObjectDeserializer.getObject().get();
        } else {
            String jsonString = event.getData().getRawJsonObject().getAsJsonObject().toString();

            JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();

            String sessionId = jsonObject.get("id").getAsString();

            try {
                return Session.retrieve(sessionId);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to retrieve Session from Stripe");
            }
        }
    }

    private Subscription getSubscription(Event event) {
        if ("checkout.session.completed".equals(event.getType())) {
            // ✅ Extract from Checkout Session metadata
            Session session = getSession(event);
            String subscriptionIdStr = session.getMetadata().get("subscriptionId");

            if (subscriptionIdStr == null) {
                throw new IllegalStateException("Missing subscriptionId in session metadata.");
            }

            Long subscriptionId = Long.parseLong(subscriptionIdStr);

            return subscriptionRepository.findById(subscriptionId)
                    .orElseThrow(() -> new IllegalStateException("Subscription not found in DB"));
        }

        if ("customer.subscription.deleted".equals(event.getType())) {
            // ✅ Extract directly from Stripe Subscription object
            com.stripe.model.Subscription stripeSub =
                    (com.stripe.model.Subscription) event.getDataObjectDeserializer()
                            .getObject()
                            .orElse(null);

            if (stripeSub == null) {
                throw new IllegalStateException("Could not parse Stripe subscription from event.");
            }

            String stripeId = stripeSub.getId();

            return subscriptionRepository.findByStripeSubscriptionId(stripeId)
                    .orElseThrow(() -> new IllegalStateException("Local subscription not found by Stripe ID"));
        }

        throw new IllegalArgumentException("Unsupported event type: " + event.getType());
    }

}
