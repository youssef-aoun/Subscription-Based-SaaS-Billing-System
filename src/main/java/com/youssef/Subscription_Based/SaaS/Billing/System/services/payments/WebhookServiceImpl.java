package com.youssef.Subscription_Based.SaaS.Billing.System.services.payments;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.Invoice;
import com.stripe.model.checkout.Session;
import com.youssef.Subscription_Based.SaaS.Billing.System.dao.SubscriptionRepository;
import com.youssef.Subscription_Based.SaaS.Billing.System.dao.WebhookEventRepository;
import com.youssef.Subscription_Based.SaaS.Billing.System.entities.payments.WebhookEvent;
import com.youssef.Subscription_Based.SaaS.Billing.System.entities.subscriptions.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Consumer;

import static com.youssef.Subscription_Based.SaaS.Billing.System.entities.plan.Plan.MONTHLY;
import static com.youssef.Subscription_Based.SaaS.Billing.System.entities.subscriptions.Subscription.ACTIVE;
import static com.youssef.Subscription_Based.SaaS.Billing.System.entities.subscriptions.Subscription.CANCELED;

@Service
public class WebhookServiceImpl implements WebhookService{

    private final WebhookEventRepository webhookEventRepository;
    private final SubscriptionRepository subscriptionRepository;

    private final Map<String, Consumer<Event>> handlerMap = Map.of(
            "checkout.session.completed", this::handleCheckoutCompleted,
            "invoice.payment_failed", this::handlePaymentFailed,
            "customer.subscription.deleted", this::handleSubscriptionDeleted
    );

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

        WebhookEvent webhookLog = createWebhookLog(event);

        handlerMap.getOrDefault(event.getType(), this::handleUnhandled).accept(event);

        webhookLog.setProcessed(true);
        webhookEventRepository.save(webhookLog);
    }

    private LocalDateTime calculateEndDate(String billingCycle){
        if(billingCycle.equals(MONTHLY))
            return LocalDateTime.now().plusMonths(1);
        return LocalDateTime.now().plusYears(1);
    }
    private void handlePaymentFailed(Event event) {
        System.out.println("invoice.payment_failed webhook received");

        Invoice invoice = (Invoice) event.getDataObjectDeserializer()
                .getObject()
                .orElse(null);

        if (invoice == null) {
            System.out.println("Could not parse invoice from event.");
            return;
        }

        Subscription subscription = getSubscription(event);

        String stripeSubscriptionId = subscription.getStripeSubscriptionId();
        if (stripeSubscriptionId == null) {
            System.out.println("No subscription ID on invoice.");
            return;
        }

        subscription.setStatus("PAST_DUE");
        subscriptionRepository.save(subscription);

        System.out.println("Subscription " + subscription.getId() + " marked as PAST_DUE due to failed invoice.");
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
    private void handleUnhandled(Event event) {
        String type = event.getType();
        String eventId = event.getId();
        System.out.printf("Unhandled Stripe event received. Type: %s, Event ID: %s\n", type, eventId);
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

        if ("invoice.payment_failed".equals(event.getType())) {
            // ✅ Extract Stripe Subscription ID from raw JSON
            String rawJson = event.getData().getRawJsonObject().toString();
            JsonObject json = JsonParser.parseString(rawJson).getAsJsonObject();

            String stripeSubscriptionId = json.get("subscription").getAsString();

            return subscriptionRepository.findByStripeSubscriptionId(stripeSubscriptionId)
                    .orElseThrow(() -> new IllegalStateException("Local subscription not found by Stripe ID"));

        }

        throw new IllegalArgumentException("Unsupported event type: " + event.getType());
    }
    private WebhookEvent createWebhookLog(Event event) {
        WebhookEvent log = new WebhookEvent();
        log.setEventId(event.getId());
        log.setEventType(event.getType());
        log.setPayload(event.toJson());
        log.setProcessed(false);
        return webhookEventRepository.save(log);
    }
}
