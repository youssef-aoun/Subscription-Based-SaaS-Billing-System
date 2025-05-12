# Subscription-Based SaaS Billing System (Java + Spring Boot + Stripe)

A **production-grade SaaS backend microservice** built using **Java Spring Boot** and integrated with **Stripe** for subscription billing. It manages full lifecycle operations including plans, checkouts, webhooks, and secure user management. Designed to be easily integrated with any frontend or backend system.

---

## Tech Stack

- **Java 17**
- **Spring Boot 3.2.5**
- **Stripe SDK v29.0.0**
- **Spring Security (JWT)**
- **JPA/Hibernate + MySQL**
- **Bucket4j Rate Limiting (per IP/token)**
- **MailSender** for password reset flows
- **Lombok**, **MapStruct**
- **Ngrok** (for local webhook testing)
- **Springdoc OpenAPI** (Swagger UI)
- **Docker & Docker Compose**

---

## Table of Contents

- [Features Implemented]
- [Rate Limiting]
- [Database Design]
- [Webhook Testing with Ngrok]
- [Business Rules]
- [Dockerization]
- [Security]
- [Test Cards]

---

## Features Implemented

### Authentication
- JWT-based secure login
- Role-based access control (`USER`, `ADMIN`)
- Password reset via email token

### User System
- Register / Login
- Profile view & update
- Role assignment via `user_roles` table

### Plan Management
- Create, update, delete subscription plans
- Support for monthly & yearly billing cycles
- Plan visibility toggle (`isDeleted`)
- Plan features managed via `feature_flags`

### Stripe Integration
- Create **Checkout Sessions** dynamically
- Pass metadata (`userId`, `subscriptionId`) to Stripe
- One-click activation after successful payment
- Maps Stripe Price IDs to DB-managed plans

### Webhooks
- Listens to Stripe events:
  - `checkout.session.completed`
  - `customer.subscription.deleted`
  - `invoice.payment_failed` (upcoming)
- Webhook payloads stored in `webhook_events`
- Idempotent handling (skip duplicates)
- Syncs `subscriptions` table based on webhook events

### Subscription Lifecycle
- Statuses: `PENDING`, `ACTIVE`, `PAST_DUE`, `CANCEL_AT_PERIOD_END`, `CANCELED`
- Stripe subscription ID stored and fully synced
- Start and End dates tracked
- Auto-cancellation scheduled at period end
- Webhooks trigger state transitions:
  - `checkout.session.completed` → activate subscription
  - `customer.subscription.deleted` → cancel locally
  - `invoice.payment_failed` → mark as `PAST_DUE`

### Invoice Generation & PDF Receipts
- Generate user-facing invoices for completed payments
- Download invoices via the user dashboard
- Store invoice metadata in the invoices table

### API Documentation
- Swagger UI available at `/swagger-ui/index.html`
- Full OpenAPI 3 spec auto-generated
- Each endpoint grouped by tag: Auth, User, Plan, Subscription, Payment


---

## Rate Limiting

Implemented using **Bucket4j** for fine-grained control:

| Endpoint                              | Limit            | Scope          |
|---------------------------------------|------------------|----------------|
| `POST /api/v1/auth/login`             | 5 req/min        | per IP address |
| `POST /api/v1/auth/register`          | 3 req/min        | per IP address |
| `POST /api/v1/users/reset-password`   | 2 req/min        | per IP address |
| `POST /api/v1/payments/checkout-session` | 5 req/min     | per JWT token  |

Custom `OncePerRequestFilter`s intercept sensitive endpoints **before Spring Security**, ensuring brute force protection even for failed logins.

---

## Database Design (Key Tables)

| Table                | Purpose                            |
|----------------------|------------------------------------|
| `users`              | Registered user accounts           |
| `roles`, `user_roles`| Role-based access control          |
| `plans`              | Monthly/Yearly subscription plans  |
| `subscriptions`      | User–plan relationship             |
| `webhook_events`     | Stripe webhook logs                |
| `invoices`           | Stripe invoice data                |
| `feature_flags`      | Add-on features per plan           |

---

## Webhook Testing with Ngrok

Run ngrok tunnel for local testing:

```bash
ngrok http 8080

```

Set your Stripe webhook endpoint to: https://{your-ngrok-domain}/api/v1/payments/webhook


Make sure your Spring app has:
stripe.secret.key=sk_test_...
stripe.success.url=https://{ngrok}/payment-success
stripe.cancel.url=https://{ngrok}/payment-cancel

stripe login
stripe listen --forward-to localhost:8080/api/v1/payments/webhook
stripe trigger checkout.session.completed
stripe trigger invoice.payment_failed


Business Rules & Smart Logic:
- Cannot subscribe to the same or lower-tier plan
- Cannot subscribe again if one is already active or pending
- Subscriptions canceled at period end (clean UX)
- `PAST_DUE` status triggered by `invoice.payment_failed`
- Webhook-driven lifecycle: local DB stays in sync with Stripe
- Auto-cancellation at period end for clean user experience

Upcoming Features:
- Environment-based config switching (dev/prod)
- Deploy on AWS

Security
- Passwords securely hashed
- JWT Authentication
- Authenticated routes only (via Spring Security)
- Admin/user separation via roles
- Stripe webhook signature verification


| Card                  | Description            |
|-----------------------|------------------------|
| `4242 4242 4242 4242` | Successful payment     |
| `4000 0000 0000 9995` | Payment declined       |
| `4000 0000 0000 0341` | Card expired           |


