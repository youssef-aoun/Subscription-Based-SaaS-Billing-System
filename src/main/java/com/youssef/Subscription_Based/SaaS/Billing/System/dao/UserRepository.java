package com.youssef.Subscription_Based.SaaS.Billing.System.dao;

import com.youssef.Subscription_Based.SaaS.Billing.System.entities.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findUserByUsername(String username);
    Optional<User> findUserByEmail(String email);
}
