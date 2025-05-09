package com.youssef.Subscription_Based.SaaS.Billing.System.services.notifications;

public interface NotificationService {
    void sendToUser(String username, String title, String message);
}
