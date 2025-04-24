package com.youssef.Subscription_Based.SaaS.Billing.System.services.plan;

import com.youssef.Subscription_Based.SaaS.Billing.System.entities.plan.Plan;

import java.util.List;
import java.util.Optional;

public interface PlanService {
    Optional<Plan> findPlanById(Long id);
    List<Plan> getAllActivePlans(String billingCycle);
}
