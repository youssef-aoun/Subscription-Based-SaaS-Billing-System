package com.youssef.Subscription_Based.SaaS.Billing.System.controllers.api.v1;

import com.stripe.exception.StripeException;
import com.youssef.Subscription_Based.SaaS.Billing.System.dto.UnsubscribeResponse;
import com.youssef.Subscription_Based.SaaS.Billing.System.dto.ViewSubscriptionHistoryResponse;
import com.youssef.Subscription_Based.SaaS.Billing.System.dto.ViewSubscriptionResponse;
import com.youssef.Subscription_Based.SaaS.Billing.System.services.subscription.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subscriptions")
@Tag(name = "Subscription", description = "Operations related to subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @Autowired
    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @Operation(
            summary = "Cancel active subscription",
            description = "Cancels the currently active Stripe subscription for the authenticated user.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Subscription successfully cancelled",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = UnsubscribeResponse.class)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "No active subscription found to cancel"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
                    @ApiResponse(responseCode = "500", description = "Internal error while communicating with Stripe")
            }
    )
    @PostMapping("/cancel")
    public ResponseEntity<UnsubscribeResponse> unsubscribe(@RequestHeader("Authorization") String ignoredAuthorizationHeader) throws StripeException {
        UnsubscribeResponse response = subscriptionService.cancelActiveSubscription();
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "View current subscription",
            description = "Returns the currently active subscription for the authenticated user.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Current subscription details",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ViewSubscriptionResponse.class)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "No active subscription found"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
            }
    )
    @GetMapping("/me")
    public ResponseEntity<ViewSubscriptionResponse> getSubscriptions(@RequestHeader("Authorization") String ignoredAuthorizationHeader){
        ViewSubscriptionResponse response = subscriptionService.getSubscriptions();
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "View subscription history",
            description = "Returns a list of all past and current subscriptions for the authenticated user.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Subscription history retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = ViewSubscriptionHistoryResponse.class))
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT token")
            }
    )
    @GetMapping("/history")
    public ResponseEntity<List<ViewSubscriptionHistoryResponse>> getSubscriptionsHistory(@RequestHeader("Authorization") String ignoredAuthorizationHeader){
        List<ViewSubscriptionHistoryResponse> response = subscriptionService.getSubscriptionsHistory();
        return ResponseEntity.ok(response);
    }

}
