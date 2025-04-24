package com.youssef.Subscription_Based.SaaS.Billing.System.controllers.api.v1;

import com.youssef.Subscription_Based.SaaS.Billing.System.dto.PasswordResetRequest;
import com.youssef.Subscription_Based.SaaS.Billing.System.dto.UpdateProfileRequest;
import com.youssef.Subscription_Based.SaaS.Billing.System.services.users.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String ignoredAuthorizationHeader){
        return userService.profile();
    }

    @PutMapping("")
    public ResponseEntity<?> updateProfile(@RequestBody UpdateProfileRequest request){
        return userService.updateProfile(request);
    }

    @PostMapping("/password-reset-request")
    public ResponseEntity<?> passwordResetRequest(@RequestBody PasswordResetRequest request) {
        System.out.println("Received password reset request: " + request.getEmail());
        return userService.sendPasswordResetEmail(request.getEmail());
    }
}
