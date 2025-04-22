package com.youssef.Subscription_Based.SaaS.Billing.System.services.auth;

import com.youssef.Subscription_Based.SaaS.Billing.System.dto.LoginRequest;
import com.youssef.Subscription_Based.SaaS.Billing.System.dto.RegisterRequest;
import org.springframework.http.ResponseEntity;

public interface AuthService {
    ResponseEntity<?> register(RegisterRequest request);
    ResponseEntity<?> login(LoginRequest request);
}
