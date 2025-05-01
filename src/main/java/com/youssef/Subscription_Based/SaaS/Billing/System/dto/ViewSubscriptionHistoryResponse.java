package com.youssef.Subscription_Based.SaaS.Billing.System.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ViewSubscriptionHistoryResponse {
    private Long subscriptionId;
    private String planName;
    private String billingCycle;
    private String status; // e.g., ACTIVE, CANCELED, etc.
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean cancelAtPeriodEnd;
}
