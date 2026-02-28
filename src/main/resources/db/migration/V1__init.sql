-- Phase 1: Identity + Auth schema
-- PostgreSQL only; Flyway-managed

-- ENUMS
CREATE TYPE user_role AS ENUM ('OWNER', 'STAFF');
CREATE TYPE user_status AS ENUM ('ACTIVE', 'INACTIVE');

-- shop table
CREATE TABLE shop (
    id BIGSERIAL PRIMARY KEY,
    shop_name TEXT NOT NULL,
    owner_name TEXT NOT NULL,
    phone TEXT NOT NULL UNIQUE,
    address TEXT NULL,
    gst_number TEXT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT NOW() NOT NULL
);

-- users table (shop scoped)
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    shop_id BIGINT NOT NULL REFERENCES shop(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    mobile TEXT NOT NULL,
    password_hash TEXT NOT NULL,
    role user_role NOT NULL,
    status user_status DEFAULT 'ACTIVE' NOT NULL,
    active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    UNIQUE(mobile)
);

CREATE INDEX idx_users_shop_id ON users(shop_id);
CREATE INDEX idx_users_shop_id_role ON users(shop_id, role);

-- refresh_token table
CREATE TABLE refresh_token (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token TEXT UNIQUE NOT NULL,
    expiry TIMESTAMPTZ NOT NULL,
    revoked BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL
);
