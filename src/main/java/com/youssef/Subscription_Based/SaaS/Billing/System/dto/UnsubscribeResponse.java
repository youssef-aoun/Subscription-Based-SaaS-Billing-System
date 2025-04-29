package com.youssef.Subscription_Based.SaaS.Billing.System.dto;

import lombok.Data;


@Data
public class UnsubscribeResponse {
    private Long subscriptionId;
    private String message;
}
