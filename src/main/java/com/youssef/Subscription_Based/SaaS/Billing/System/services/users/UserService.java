package com.youssef.Subscription_Based.SaaS.Billing.System.services.users;

import com.youssef.Subscription_Based.SaaS.Billing.System.dto.UpdateProfileRequest;
import com.youssef.Subscription_Based.SaaS.Billing.System.entities.user.User;
import org.springframework.http.ResponseEntity;

public interface UserService {

    ResponseEntity<?> profile();
    ResponseEntity<?> updateProfile(UpdateProfileRequest request);
    ResponseEntity<?> sendPasswordResetEmail(String email);
    User getCurrentAuthenticatedUser();

}
