package com.youssef.Subscription_Based.SaaS.Billing.System.exceptions;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}

