package com.youssef.Subscription_Based.SaaS.Billing.System.services.payments;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.Invoice;
import com.stripe.model.checkout.Session;
import com.youssef.Subscription_Based.SaaS.Billing.System.dao.InvoiceRepository;
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
    private final InvoiceRepository invoiceRepository;

    @Autowired
    public WebhookServiceImpl(WebhookEventRepository webhookEventRepository, SubscriptionRepository subscriptionRepository,
                              InvoiceRepository invoiceRepository) {
        this.webhookEventRepository = webhookEventRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.invoiceRepository = invoiceRepository;
    }

    @Override
    public void handleStripeEvent(Event event) {
        String eventId = event.getId();

        if(webhookEventRepository.existsByEventId(eventId)){
            return;
        }

        WebhookEvent webhookLog = createWebhookLog(event);

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
        System.out.println("📢 Processing 'checkout.session.completed' event...");

        Session session= getSession(event);

        // Extract the subscription ID from the session
        String stripeSubscriptionId = session.getSubscription();
        if (stripeSubscriptionId == null || stripeSubscriptionId.isEmpty()) {
            System.out.println("⚠️ No Stripe Subscription ID found in the session. Skipping subscription activation.");
            return;
        }

        // Extract metadata from the session to find the corresponding subscription in your DB
        String userId = session.getMetadata().get("userId");
        String localSubscriptionId = session.getMetadata().get("subscriptionId");

        if (localSubscriptionId == null) {
            System.out.println("⚠️ Missing local subscriptionId in metadata. Cannot process subscription.");
            return;
        }

        Subscription subscription = getSubscription(event);

        subscription.setStripeSubscriptionId(session.getSubscription());
        subscription.setStatus(ACTIVE);
        subscription.setEndDate(calculateEndDate(subscription.getPlan().getBillingCycle()));

        subscriptionRepository.save(subscription);

        createInvoiceRecord(subscription, session);

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
    private Session getSession(Event event) {
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();

        if (deserializer.getObject().isPresent()) {
            return (Session) deserializer.getObject().get();
        } else {
            // Fallback: Parse the raw JSON payload directly
            String rawJson = event.getData().getObject().toJson();
            JsonObject jsonObject = JsonParser.parseString(rawJson).getAsJsonObject();

            if (!jsonObject.has("id")) {
                System.out.println("❌ No 'id' field found in raw JSON data.");
                throw new IllegalStateException("No 'id' field found in raw JSON data.");
            }

            String sessionId = jsonObject.get("id").getAsString();
            System.out.println("📢 Fetching session from Stripe API using ID: " + sessionId);

            try {
                return Session.retrieve(sessionId);
            } catch (StripeException e) {
                throw new IllegalStateException("Failed to retrieve Session from Stripe API.", e);
            }
        }
    }

    private Subscription getSubscription(Event event) {
        System.out.println("📦 Extracting subscription for event type: " + event.getType());

        if ("checkout.session.completed".equals(event.getType())) {
            Session session = getSession(event);
            System.out.println("🔍 Retrieved session ID: " + session.getId());
            System.out.println("🔧 Session Metadata: " + session.getMetadata());

            String subscriptionIdStr = session.getMetadata().get("subscriptionId");

            if (subscriptionIdStr == null) {
                System.out.println("⚠️ Missing 'subscriptionId' in session metadata.");
                throw new IllegalStateException("Missing subscriptionId in session metadata.");
            }

            Long subscriptionId = Long.parseLong(subscriptionIdStr);
            System.out.println("📌 Looking up local subscription with ID: " + subscriptionId);

            return subscriptionRepository.findById(subscriptionId)
                    .orElseThrow(() -> {
                        System.out.println("❌ Subscription not found in DB for ID: " + subscriptionId);
                        return new IllegalStateException("Subscription not found in DB");
                    });
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

        System.out.println("❌ Unsupported event type: " + event.getType());
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
    private void createInvoiceRecord(Subscription subscription, Session session){
        try {
            boolean invoiceExists = invoiceRepository.existsByStripeInvoiceId(session.getInvoice());
            if (invoiceExists) {
                System.out.printf("📄 Invoice already exists for Stripe Invoice ID: %s\n", session.getInvoice());
                return;
            }
            com.youssef.Subscription_Based.SaaS.Billing.System.entities.payments.Invoice invoice = new com.youssef.Subscription_Based.SaaS.Billing.System.entities.payments.Invoice();
            invoice.setUser(subscription.getUser());
            invoice.setSubscription(subscription);
            invoice.setAmount(subscription.getPlan().getPrice());
            invoice.setStatus("PAID");
            invoice.setIssuedDate(LocalDateTime.now());
            invoice.setDueDate(null);
            invoice.setStripeInvoiceId(session.getInvoice());

            invoiceRepository.save(invoice);
        }catch (Exception e){
            System.out.println("Failed to create invoice: " + e.getMessage());
        }
    }
}
