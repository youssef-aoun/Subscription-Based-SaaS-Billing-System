package com.youssef.Subscription_Based.SaaS.Billing.System.controllers.api.v1;

import com.youssef.Subscription_Based.SaaS.Billing.System.entities.plan.Plan;
import com.youssef.Subscription_Based.SaaS.Billing.System.services.plan.PlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/plans")
@Tag(name = "Plan", description = "Operations related to plans")
public class PlanController {

    private final PlanService planService;

    @Autowired
    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @Operation(
            summary = "Get plan by ID",
            description = "Retrieves a specific plan by its unique ID",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Plan found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Plan.class)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Plan not found")
            }
    )
    @GetMapping("{id}")
    public ResponseEntity<Plan> getPlanById(@PathVariable("id")Long id, @RequestHeader("Authorization") String ignoredAuthorizationHeader){
        return planService.findPlanById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Get all active plans",
            description = "Returns all non-deleted plans. Optionally filter by billing cycle (e.g., 'MONTHLY' or 'YEARLY').",
            parameters = {
                    @Parameter(
                            name = "billingCycle",
                            in = ParameterIn.QUERY,
                            description = "Billing cycle to filter plans (MONTHLY or YEARLY)",
                            schema = @Schema(type = "string", example = "MONTHLY")
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of active plans",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = Plan.class))
                            )
                    )
            }
    )
    @GetMapping
    public ResponseEntity<List<Plan>> getPlans(@RequestParam(value = "billing_cycle", required = false)String billingCycle, @RequestHeader("Authorization") String ignoredAuthorizationHeader){
        List<Plan> plans = planService.getAllActivePlans(billingCycle);
        return ResponseEntity.ok(plans);
    }

}
