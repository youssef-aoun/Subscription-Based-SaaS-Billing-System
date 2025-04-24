package com.youssef.Subscription_Based.SaaS.Billing.System.services.plan;

import com.youssef.Subscription_Based.SaaS.Billing.System.dao.PlanRepository;
import com.youssef.Subscription_Based.SaaS.Billing.System.entities.plan.Plan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PlanServiceImpl implements PlanService{

    private final PlanRepository planRepository;

    @Autowired
    public PlanServiceImpl(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }


    @Override
    public Optional<Plan> findPlanById(Long id) {
        return planRepository.findPlanById(id);
    }

    @Override
    public List<Plan> getAllActivePlans(String billingCycle) {
        if(billingCycle == null || billingCycle.isEmpty())
            return planRepository.findByIsDeletedFalse();
        return planRepository.findPlansByBillingCycleAndIsDeletedFalse(billingCycle);
    }
}
