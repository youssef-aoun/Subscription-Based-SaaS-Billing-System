package com.youssef.Subscription_Based.SaaS.Billing.System.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UpdateProfileRequest {
    private String name;
    private String email;
    private String gender;
}
