CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'MANAGER', 'WAITER', 'CASHIER', 'KITCHEN')),
    active BOOLEAN NOT NULL DEFAULT true
);

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_active ON users(active);
