package com.youssef.Subscription_Based.SaaS.Billing.System.controllers.api.v1;

import com.youssef.Subscription_Based.SaaS.Billing.System.dto.CheckoutRequest;
import com.youssef.Subscription_Based.SaaS.Billing.System.services.payments.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }


    @PostMapping("/checkout-session")
    public ResponseEntity<?> createCheckoutSession(@RequestBody CheckoutRequest request) {
        String checkoutUrl = paymentService.createCheckoutSession(request.getPlanId());
        Map<String, String> response = new HashMap<>();
        response.put("checkoutUrl", checkoutUrl);
        return ResponseEntity.ok(response);
    }

}
