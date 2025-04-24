package com.youssef.Subscription_Based.SaaS.Billing.System.dao;

import com.youssef.Subscription_Based.SaaS.Billing.System.entities.plan.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlanRepository extends JpaRepository<Plan, Long> {
    Optional<Plan> findPlanById(Long id);
    List<Plan> findPlansByBillingCycleAndIsDeletedFalse(String billingCycle);
    List<Plan> findByIsDeletedFalse();

}
