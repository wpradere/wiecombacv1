-- =============================================================
-- V8__uuid_schema.sql
-- Drops all tables and recreates them with UUID primary keys.
-- Run "flyway clean" locally before applying this migration.
-- =============================================================

-- Drop in reverse FK order
DROP TABLE IF EXISTS order_items        CASCADE;
DROP TABLE IF EXISTS orders             CASCADE;
DROP TABLE IF EXISTS cart_items         CASCADE;
DROP TABLE IF EXISTS carts              CASCADE;
DROP TABLE IF EXISTS product_attributes CASCADE;
DROP TABLE IF EXISTS product_images     CASCADE;
DROP TABLE IF EXISTS products           CASCADE;

-- Enable pgcrypto for gen_random_uuid() (idempotent)
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ── PRODUCTS ──────────────────────────────────────────────────────────────────
CREATE TABLE products (
    id          UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(200)   NOT NULL,
    description TEXT,
    price       NUMERIC(12, 2) NOT NULL,
    image_url   VARCHAR(500),
    category    VARCHAR(20)    NOT NULL,
    section     VARCHAR(20)    NOT NULL DEFAULT 'SUBLIMACION',
    stock       INTEGER        NOT NULL DEFAULT 0,
    edition     VARCHAR(20),
    featured    BOOLEAN        NOT NULL DEFAULT FALSE,
    active      BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_products_category ON products (category);
CREATE INDEX idx_products_active   ON products (active);
CREATE INDEX idx_products_featured ON products (featured);
CREATE INDEX idx_products_section  ON products (section);

-- ── PRODUCT IMAGES ────────────────────────────────────────────────────────────
CREATE TABLE product_images (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id  UUID         NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    image_url   VARCHAR(500) NOT NULL,
    alt_text    VARCHAR(200),
    description TEXT,
    sort_order  INTEGER      NOT NULL DEFAULT 0
);

CREATE INDEX idx_product_images_product_id ON product_images (product_id);

-- ── PRODUCT ATTRIBUTES ────────────────────────────────────────────────────────
CREATE TABLE product_attributes (
    id          UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id  UUID          NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    name        VARCHAR(100)  NOT NULL,
    value       VARCHAR(500)  NOT NULL,
    sort_order  INTEGER       NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_product_attributes_product_id ON product_attributes (product_id);

-- ── CARTS ─────────────────────────────────────────────────────────────────────
CREATE TABLE carts (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id VARCHAR(36)  NOT NULL UNIQUE,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_carts_session_id ON carts (session_id);

-- ── CART ITEMS ────────────────────────────────────────────────────────────────
CREATE TABLE cart_items (
    id         UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    cart_id    UUID           NOT NULL REFERENCES carts(id)    ON DELETE CASCADE,
    product_id UUID           NOT NULL REFERENCES products(id),
    quantity   INTEGER        NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(12, 2) NOT NULL,
    UNIQUE (cart_id, product_id)
);

CREATE INDEX idx_cart_items_cart_id ON cart_items (cart_id);

-- ── ORDERS ────────────────────────────────────────────────────────────────────
CREATE TABLE orders (
    id               UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    order_number     VARCHAR(30)    NOT NULL UNIQUE,
    session_id       VARCHAR(36)    NOT NULL,
    customer_email   VARCHAR(255)   NOT NULL,
    customer_name    VARCHAR(200)   NOT NULL,
    customer_phone   VARCHAR(30),
    shipping_street  VARCHAR(300)   NOT NULL,
    shipping_city    VARCHAR(100)   NOT NULL,
    shipping_state   VARCHAR(100),
    shipping_zip     VARCHAR(20),
    shipping_country VARCHAR(2)     NOT NULL,
    status           VARCHAR(20)    NOT NULL DEFAULT 'PENDING_PAYMENT',
    subtotal         NUMERIC(12, 2) NOT NULL,
    shipping_cost    NUMERIC(12, 2) NOT NULL DEFAULT 0,
    total            NUMERIC(12, 2) NOT NULL,
    notes            TEXT,
    created_at       TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_orders_order_number   ON orders (order_number);
CREATE INDEX idx_orders_session_id     ON orders (session_id);
CREATE INDEX idx_orders_customer_email ON orders (customer_email);
CREATE INDEX idx_orders_status         ON orders (status);

-- ── ORDER ITEMS ───────────────────────────────────────────────────────────────
CREATE TABLE order_items (
    id           UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id     UUID           NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id   UUID           NOT NULL REFERENCES products(id),
    product_name VARCHAR(200)   NOT NULL,
    quantity     INTEGER        NOT NULL CHECK (quantity > 0),
    unit_price   NUMERIC(12, 2) NOT NULL
);

CREATE INDEX idx_order_items_order_id ON order_items (order_id);
