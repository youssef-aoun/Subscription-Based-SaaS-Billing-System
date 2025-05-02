package com.youssef.Subscription_Based.SaaS.Billing.System.controllers.api.v1;

import com.stripe.model.Event;
import com.stripe.net.Webhook;
import com.youssef.Subscription_Based.SaaS.Billing.System.services.payments.WebhookService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

@Hidden
@RestController
@RequestMapping("/api/v1/payments")
public class WebhookController {

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    private final WebhookService webhookService;

    @Autowired
    public WebhookController(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<?> handleStripeWebhook(HttpServletRequest request){
        String payload;
        String sigHeader = request.getHeader("Stripe-Signature");

        try (Scanner s = new Scanner(request.getInputStream(), StandardCharsets.UTF_8).useDelimiter("\\A")) {
            payload = s.hasNext() ? s.next() : "";
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to read payload");
        }

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        }

        webhookService.handleStripeEvent(event);

        return ResponseEntity.ok("Webhook received");
    }
}
