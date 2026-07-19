-- TENCO baseline schema (Postgres). Column names follow Spring's default
-- CamelCase->snake_case physical naming; money is stored as BIGINT paise; timestamps as BIGINT epoch millis.

CREATE TABLE IF NOT EXISTS users (
    id          VARCHAR(64) PRIMARY KEY,
    phone       VARCHAR(20),
    name        VARCHAR(255),
    role        VARCHAR(20),
    created_at  BIGINT
);
CREATE UNIQUE INDEX IF NOT EXISTS ux_users_phone ON users (phone);

CREATE TABLE IF NOT EXISTS dealers (
    id          VARCHAR(64) PRIMARY KEY,
    name        VARCHAR(255),
    location    VARCHAR(255),
    updated_at  BIGINT
);

CREATE TABLE IF NOT EXISTS purchases (
    id              VARCHAR(64) PRIMARY KEY,
    dealer_id       VARCHAR(64),
    quantity        INTEGER,
    unit_cost_paise BIGINT,
    created_at      BIGINT,
    updated_at      BIGINT
);
CREATE INDEX IF NOT EXISTS ix_purchases_dealer ON purchases (dealer_id);

CREATE TABLE IF NOT EXISTS vendors (
    id           VARCHAR(64) PRIMARY KEY,
    name         VARCHAR(255),
    phone        VARCHAR(20),
    upi_vpa      VARCHAR(255),
    language_tag VARCHAR(10),
    updated_at   BIGINT
);
CREATE INDEX IF NOT EXISTS ix_vendors_phone ON vendors (phone);

CREATE TABLE IF NOT EXISTS prices (
    id               VARCHAR(64) PRIMARY KEY,
    vendor_id        VARCHAR(64),
    unit_price_paise BIGINT,
    effective_from   BIGINT,
    updated_at       BIGINT
);
CREATE INDEX IF NOT EXISTS ix_prices_vendor ON prices (vendor_id);

CREATE TABLE IF NOT EXISTS deliveries (
    id               VARCHAR(64) PRIMARY KEY,
    vendor_id        VARCHAR(64),
    quantity         INTEGER,
    unit_price_paise BIGINT,
    status           VARCHAR(20),
    created_at       BIGINT,
    confirmed_at     BIGINT,
    updated_at       BIGINT
);
CREATE INDEX IF NOT EXISTS ix_deliveries_vendor ON deliveries (vendor_id);

CREATE TABLE IF NOT EXISTS complaints (
    id               VARCHAR(64) PRIMARY KEY,
    delivery_id      VARCHAR(64),
    vendor_id        VARCHAR(64),
    reason           VARCHAR(255),
    photo_url        VARCHAR(1024),
    adjustment_paise BIGINT,
    status           VARCHAR(20),
    created_at       BIGINT,
    updated_at       BIGINT
);
CREATE INDEX IF NOT EXISTS ix_complaints_vendor ON complaints (vendor_id);

CREATE TABLE IF NOT EXISTS payments (
    id               VARCHAR(64) PRIMARY KEY,
    vendor_id        VARCHAR(64),
    amount_paise     BIGINT,
    method           VARCHAR(20),
    status           VARCHAR(30),
    gateway_order_id VARCHAR(255),
    upi_ref          VARCHAR(255),
    note             VARCHAR(1024),
    created_at       BIGINT,
    updated_at       BIGINT
);
CREATE INDEX IF NOT EXISTS ix_payments_vendor ON payments (vendor_id);

CREATE TABLE IF NOT EXISTS device_tokens (
    id         VARCHAR(64) PRIMARY KEY,
    user_id    VARCHAR(64),
    token      VARCHAR(512),
    updated_at BIGINT
);
CREATE INDEX IF NOT EXISTS ix_device_tokens_user ON device_tokens (user_id);
