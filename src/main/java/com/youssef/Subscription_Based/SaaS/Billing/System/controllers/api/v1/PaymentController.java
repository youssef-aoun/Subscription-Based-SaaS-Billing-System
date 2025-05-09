package com.youssef.Subscription_Based.SaaS.Billing.System.controllers.api.v1;

import com.youssef.Subscription_Based.SaaS.Billing.System.dto.CheckoutRequest;
import com.youssef.Subscription_Based.SaaS.Billing.System.services.payments.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.bind.annotation.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payment", description = "Operations related to payments")
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Operation(
            summary = "Create Stripe Checkout Session",
            description = "Creates a Stripe Checkout Session for the authenticated user based on the selected plan.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Checkout session created successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = "{ \"checkoutUrl\": \"https://checkout.stripe.com/...\" }")
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT token"),
                    @ApiResponse(responseCode = "404", description = "Plan not found")
            }
    )
    @PostMapping("/checkout-session")
    public ResponseEntity<?> createCheckoutSession(@RequestBody CheckoutRequest request, @RequestHeader("Authorization") String ignoredAuthorizationHeader) {
        String checkoutUrl = paymentService.createCheckoutSession(request.getPlanId());
        Map<String, String> response = new HashMap<>();
        response.put("checkoutUrl", checkoutUrl);
        return ResponseEntity.ok(response);
    }

}
