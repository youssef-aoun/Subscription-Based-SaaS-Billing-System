-- Roles
INSERT INTO roles (role_name) VALUES ('ADMIN'), ('USER');

-- Users
INSERT INTO users (name, username, email, password_hash, gender)
VALUES
    ('John Doe', 'johndoe', 'john@example.com', 'hashed_password_123', 'Male'),
    ('Jane Smith', 'janesmith', 'jane@example.com', 'hashed_password_456', 'Female');

-- Assign Roles
INSERT INTO user_roles (user_id, role_id)
VALUES
    (1, 1),  -- John Doe as ADMIN
    (2, 2);  -- Jane Smith as USER

-- Plans
INSERT INTO plans (name, price, billing_cycle, features_json)
VALUES
    ('Basic', 1000, 'MONTHLY', '["Feature A", "Feature B"]'),
    ('Pro', 2500, 'MONTHLY', '["Feature A", "Feature B", "Feature C"]'),
    ('Enterprise', 5000, 'YEARLY', '["All Features", "Priority Support"]');

-- Feature Flags
INSERT INTO feature_flags (plan_id, feature_name)
VALUES
    (1, 'STANDARD_SUPPORT'),
    (2, 'ADVANCED_REPORTS'),
    (3, 'PRIORITY_SUPPORT');

-- Subscriptions
INSERT INTO subscriptions (status, user_id, plan_id, start_date, end_date)
VALUES
    ('ACTIVE', 2, 1, NOW(), DATE_ADD(NOW(), INTERVAL 1 MONTH));

-- Invoices
INSERT INTO invoices (user_id, subscription_id, amount, status)
VALUES
    (2, 1, 1000, 'PAID');

-- Webhook Events Example
INSERT INTO webhook_events (event_id, event_type, payload)
VALUES
    ('evt_test_123', 'invoice.paid', '{ "example": "data" }');

-- Audit Log Example
INSERT INTO audit_log (user_id, action, entity_type, entity_id, details)
VALUES
    (1, 'USER_CREATED', 'User', 2, '{ "createdBy": "Admin" }');
