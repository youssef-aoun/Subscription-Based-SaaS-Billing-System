package com.youssef.Subscription_Based.SaaS.Billing.System.controllers.api.v1;

import com.youssef.Subscription_Based.SaaS.Billing.System.dto.PasswordResetRequest;
import com.youssef.Subscription_Based.SaaS.Billing.System.dto.ProfileResponse;
import com.youssef.Subscription_Based.SaaS.Billing.System.dto.UpdateProfileRequest;
import com.youssef.Subscription_Based.SaaS.Billing.System.services.users.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User", description = "Operations related to user account")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(
            summary = "Get Profile",
            description = "Allows an authenticated user to view their profile",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Profile returned successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ProfileResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - Invalid or missing JWT token"
                    )
            }
    )
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String ignoredAuthorizationHeader){
        return userService.profile();
    }

    @Operation(
            summary = "Update user profile",
            description = "Allows an authenticated user to update their name, email, or gender",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Profile updated successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @PutMapping("")
    public ResponseEntity<?> updateProfile(@RequestBody UpdateProfileRequest request, @RequestHeader("Authorization") String ignoredAuthorizationHeader){
        return userService.updateProfile(request);
    }

    @Operation(
            summary = "Request password reset",
            description = "Sends a password reset email to the given address. This endpoint is public and does not require authentication."
    )
    @ApiResponse(responseCode = "200", description = "Password reset email sent successfully")
    @ApiResponse(responseCode = "404", description = "Email not found (optional)")
    @PostMapping("/password-reset-request")
    public ResponseEntity<?> passwordResetRequest(@RequestBody PasswordResetRequest request) {
        return userService.sendPasswordResetEmail(request.getEmail());
    }
}
