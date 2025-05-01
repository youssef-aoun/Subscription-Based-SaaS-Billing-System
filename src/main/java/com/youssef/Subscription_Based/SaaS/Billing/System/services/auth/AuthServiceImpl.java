package com.youssef.Subscription_Based.SaaS.Billing.System.services.auth;

import com.youssef.Subscription_Based.SaaS.Billing.System.config.JwtUtil;
import com.youssef.Subscription_Based.SaaS.Billing.System.dao.RoleRepository;
import com.youssef.Subscription_Based.SaaS.Billing.System.dao.UserRepository;
import com.youssef.Subscription_Based.SaaS.Billing.System.dto.*;
import com.youssef.Subscription_Based.SaaS.Billing.System.entities.user.Role;
import com.youssef.Subscription_Based.SaaS.Billing.System.entities.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthServiceImpl implements AuthService{

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(RoleRepository roleRepository,
                          UserRepository userRepository,
                          BCryptPasswordEncoder bCryptPasswordEncoder,
                          AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil
    ) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }


    @Override
    public ResponseEntity<?> register(RegisterRequest request){
        if(userRepository.findUserByUsername(request.getUsername()).isPresent())
            throw new IllegalStateException("Username is taken");
        if(userRepository.findUserByEmail(request.getEmail()).isPresent())
            throw new IllegalStateException("Email is taken");

        User user = new User();
        user.setName(request.getName());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setRoles(List.of(roleRepository.findRoleByName(Role.USER).orElseThrow()));
        user.setGender(request.getGender());
        user.setPassword(bCryptPasswordEncoder.encode(request.getPassword()));

        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponse("User registered successfully", 201));
    }

    @Override
    public ResponseEntity<?> login(LoginRequest userLoginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        userLoginRequest.getUsername(),
                        userLoginRequest.getPassword()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwtToken = jwtUtil.generateToken(userDetails);

        LoginResponse response = new LoginResponse(jwtToken, "Bearer", jwtUtil.getExpirationInSeconds());

        return ResponseEntity.ok(response);
    }

}
