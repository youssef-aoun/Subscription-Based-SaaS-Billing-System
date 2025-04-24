package com.youssef.Subscription_Based.SaaS.Billing.System.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class PasswordResetRequest {
    private String email;

    @JsonCreator
    public PasswordResetRequest(@JsonProperty("email") String email) {
        this.email = email;
    }
}
