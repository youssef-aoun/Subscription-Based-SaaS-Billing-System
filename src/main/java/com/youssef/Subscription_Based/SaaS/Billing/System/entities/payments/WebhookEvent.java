package com.youssef.Subscription_Based.SaaS.Billing.System.entities.payments;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "webhook_events")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WebhookEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "event_id", unique = true, nullable = false)
    private String eventId;

    @Column(name = "event_type")
    private String eventType;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    @Column(name = "received_at")
    private LocalDateTime receivedAt = LocalDateTime.now();

    @Column(name = "processed")
    private boolean processed = false;
}
