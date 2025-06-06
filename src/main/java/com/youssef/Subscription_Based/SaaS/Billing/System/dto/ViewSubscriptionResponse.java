package com.youssef.Subscription_Based.SaaS.Billing.System.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ViewSubscriptionResponse {
    private Long subscriptionId;
    private String planName;
    private String billingCycle;
    private String status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean cancelAtPeriodEnd;
}
