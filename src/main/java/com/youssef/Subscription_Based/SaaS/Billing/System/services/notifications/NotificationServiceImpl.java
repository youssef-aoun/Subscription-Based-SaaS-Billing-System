package com.youssef.Subscription_Based.SaaS.Billing.System.services.notifications;

import com.youssef.Subscription_Based.SaaS.Billing.System.dto.NotificationMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService{

    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public NotificationServiceImpl(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void sendToUser(String username, String title, String message) {
        NotificationMessage payload = new NotificationMessage(title, message);
        messagingTemplate.convertAndSendToUser(username, "/queue/notifications", payload);
    }
}
