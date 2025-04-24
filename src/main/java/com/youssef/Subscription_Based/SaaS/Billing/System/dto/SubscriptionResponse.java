package com.youssef.Subscription_Based.SaaS.Billing.System.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SubscriptionResponse {
    private Long subscriptionId;
    private String status;
    private String planName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
