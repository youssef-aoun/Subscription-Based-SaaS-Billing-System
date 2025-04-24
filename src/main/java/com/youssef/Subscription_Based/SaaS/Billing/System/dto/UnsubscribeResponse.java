package com.youssef.Subscription_Based.SaaS.Billing.System.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UnsubscribeResponse {
    private Long subscriptionId;
    private String message;
    private LocalDateTime canceledAt;
}
