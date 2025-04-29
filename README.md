# Subscription-Based SaaS Billing System (Java + Spring Boot + Stripe)

This is a **production-grade SaaS microservice** built with **Java Spring Boot** and integrated with **Stripe** for subscription-based billing. It handles full lifecycle management of plans, checkouts, and webhooks, and can be plugged into any backend needing reliable subscription billing.

---

Tech Stack

- Java 17
- Spring Boot 3.2.5
- Stripe SDK v29.0.0
- Spring Security (JWT-based)
- JPA/Hibernate + MySQL
- MailSender (for password reset)
- Lombok, MapStruct
- Stripe Webhooks
- Ngrok (for local webhook testing)

---

## Features Implemented

### Authentication
- JWT-based secure login
- Role-based access control (`USER`, `ADMIN`)
- Password reset via email

### User System
- Register / Login
- Profile view & update
- Role assignment (`user_roles`)

### Plan Management
- CRUD operations on plans
- Features per plan (via `feature_flags`)
- Monthly / Yearly billing cycles
- Plan visibility toggle (`isDeleted` flag)

### Stripe Checkout Integration
- Create Stripe Checkout Sessions dynamically
- Send metadata (`userId`, `subscriptionId`) to Stripe
- One-click subscription activation flow
- Stripe Price IDs mapped to DB plans

### Webhook Events
- Listens to:
  - `checkout.session.completed`
  - `customer.subscription.deleted`
  - `invoice.payment_failed` (coming next)
- Stores webhook payloads in `webhook_events` table
- Ensures idempotent processing (skip duplicates)
- Automatically updates local `subscriptions` table

### Subscription Lifecycle
- Statuses: `PENDING`, `ACTIVE`, `CANCEL_AT_PERIOD_END`, `CANCELED`
- Stripe subscription ID stored for full sync
- Start and End dates calculated and saved
- Cancel requests scheduled at period end
- Automatic Stripe → DB updates via webhooks

---

## Database Design (Key Tables)

| Table                | Purpose                            |
|----------------------|------------------------------------|
| `users`              | Registered users                   |
| `roles`, `user_roles`| Role-based auth                    |
| `plans`              | Subscription plans (monthly/yearly)|
| `subscriptions`      | User subscriptions                 |
| `webhook_events`     | Stores raw Stripe webhook payloads |
| `invoices`           | Stripe invoice tracking (WIP)      |
| `feature_flags`      | Features per plan (add-ons)        |

---

## Webhook Testing with Ngrok

To test Stripe webhooks locally:

```bash
ngrok http 8080
```

Set your Stripe webhook endpoint to: https://{your-ngrok-domain}/api/v1/payments/webhook


Make sure your Spring app has:
stripe.secret.key=sk_test_...
stripe.success.url=https://{ngrok}/payment-success
stripe.cancel.url=https://{ngrok}/payment-cancel


Business Rules & Smart Logic:
- Cannot subscribe to the same or lower-tier plan
- Cannot subscribe again if one is already active or pending
- Subscriptions canceled at period end (clean UX)
- Auto-activation via checkout.session.completed
- Auto-finalization via customer.subscription.deleted
- Subscription status fully reflects Stripe state

Upcoming Features:
- Webhook: invoice.payment_failed → mark PAST_DUE
- Upgrade/Downgrade logic with proration
- Swagger / OpenAPI docs
- Admin API (manage plans/users)
- CI/CD-ready build
- Environment-based config switching (dev/prod)
- Deploy on Render / Railway / AWS

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


