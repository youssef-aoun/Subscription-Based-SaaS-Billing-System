package com.youssef.Subscription_Based.SaaS.Billing.System.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String tokenType = "Bearer";
    private long expiresIn;  // in seconds
}