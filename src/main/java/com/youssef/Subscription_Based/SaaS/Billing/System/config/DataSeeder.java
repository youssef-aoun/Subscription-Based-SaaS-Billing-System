package com.youssef.Subscription_Based.SaaS.Billing.System.config;

import com.youssef.Subscription_Based.SaaS.Billing.System.dao.RoleRepository;
import com.youssef.Subscription_Based.SaaS.Billing.System.dao.UserRepository;
import com.youssef.Subscription_Based.SaaS.Billing.System.entities.user.Role;
import com.youssef.Subscription_Based.SaaS.Billing.System.entities.user.User;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public DataSeeder(RoleRepository roleRepository, UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedRoles();
        seedAdminUser();
    }

    private void seedRoles() {
        if (roleRepository.findRoleByName(Role.ADMIN).isEmpty()) {
            roleRepository.save(new Role(Role.ADMIN));
        }
        if (roleRepository.findRoleByName(Role.USER).isEmpty()) {
            roleRepository.save(new Role(Role.USER));
        }
    }

    private void seedAdminUser() {
        if (userRepository.findUserByUsername("admin").isEmpty()) {
            Role adminRole = roleRepository.findRoleByName(Role.ADMIN).orElseThrow();
            User admin = new User();
            admin.setName("Super Admin");
            admin.setUsername("admin");
            admin.setEmail("admin@billing.com");
            admin.setPassword(passwordEncoder.encode("Admin@123"));
            admin.setGender("Male");
            admin.setRoles(List.of(adminRole));
            userRepository.save(admin);
        }
    }

}
