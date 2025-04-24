package com.youssef.Subscription_Based.SaaS.Billing.System.controllers.api.v1;

import com.youssef.Subscription_Based.SaaS.Billing.System.entities.plan.Plan;
import com.youssef.Subscription_Based.SaaS.Billing.System.services.plan.PlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/plans")
public class PlanController {

    private final PlanService planService;

    @Autowired
    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @GetMapping("{id}")
    public ResponseEntity<Plan> getPlanById(@PathVariable("id")Long id){
        return planService.findPlanById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Plan>> getPlans(@RequestParam(value = "billing_cycle", required = false)String billingCycle){
        List<Plan> plans = planService.getAllActivePlans(billingCycle);
        return ResponseEntity.ok(plans);
    }

}
