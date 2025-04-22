package com.youssef.Subscription_Based.SaaS.Billing.System.dao;

import com.youssef.Subscription_Based.SaaS.Billing.System.entities.user.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findRoleByName(String name);
}
