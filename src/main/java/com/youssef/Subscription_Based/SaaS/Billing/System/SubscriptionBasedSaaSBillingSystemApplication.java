package com.youssef.Subscription_Based.SaaS.Billing.System;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.Duration;

@SpringBootApplication
public class SubscriptionBasedSaaSBillingSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(SubscriptionBasedSaaSBillingSystemApplication.class, args);
	}

}
