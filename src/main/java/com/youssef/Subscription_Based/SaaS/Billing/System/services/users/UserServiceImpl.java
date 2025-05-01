package com.youssef.Subscription_Based.SaaS.Billing.System.services.users;

import com.youssef.Subscription_Based.SaaS.Billing.System.config.JwtUtil;
import com.youssef.Subscription_Based.SaaS.Billing.System.dao.UserRepository;
import com.youssef.Subscription_Based.SaaS.Billing.System.dto.ProfileResponse;
import com.youssef.Subscription_Based.SaaS.Billing.System.dto.SuccessResponse;
import com.youssef.Subscription_Based.SaaS.Billing.System.dto.UpdateProfileRequest;
import com.youssef.Subscription_Based.SaaS.Billing.System.entities.user.User;
import com.youssef.Subscription_Based.SaaS.Billing.System.exceptions.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final JwtUtil jwtUtil;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, JavaMailSender mailSender, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.mailSender = mailSender;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public ResponseEntity<ProfileResponse> profile() {

        User user = getCurrentAuthenticatedUser();

        ProfileResponse response = new ProfileResponse(
                user.getName(),
                user.getUsername(),
                user.getEmail(),
                user.getGender()
        );
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<SuccessResponse> updateProfile(UpdateProfileRequest request) {

        User user = getCurrentAuthenticatedUser();

        if(request.getName() != null) user.setName(request.getName());
        if(request.getEmail() != null) user.setEmail(request.getEmail());
        if(request.getGender() != null) user.setGender(request.getGender());

        userRepository.save(user);

        return ResponseEntity.ok(new SuccessResponse("Profile updated successfully", 200));
    }

    @Override
    public ResponseEntity<SuccessResponse> sendPasswordResetEmail(String email) {
        User user = userRepository.findUserByEmail(email).orElseThrow(
                () -> new IllegalStateException("No user found with email: " + email)
        );

        String token = jwtUtil.generateToken(user);
        String resetLink = "http://localhost:8080/api/v1/users/reset-password?token="+token;

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Password Reset Request");
            message.setText("Click on the link to reset your password: " + resetLink);

            mailSender.send(message);
        } catch (MailException e) {
            throw new IllegalStateException("Failed to send email");
        }

        return ResponseEntity.ok(new SuccessResponse("Password reset link has been sent to your email", 200));
    }

    @Override
    public User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal().equals("anonymousUser")) {
            throw new UnauthorizedException("Unauthorized access");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return (User) userDetails;
    }

}
