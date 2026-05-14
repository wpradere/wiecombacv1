-- =============================================================
-- V1__create_tables.sql
-- WIIMY Store — initial schema
-- =============================================================

-- ---------------------------------------------------------------
-- PRODUCTS
-- ---------------------------------------------------------------
CREATE TABLE products (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(200)   NOT NULL,
    description TEXT,
    price       NUMERIC(10, 2) NOT NULL,
    image_url   VARCHAR(500),
    category    VARCHAR(20)    NOT NULL,   -- APPAREL | TECH | MUGS
    stock       INTEGER        NOT NULL DEFAULT 0,
    edition     VARCHAR(20),               -- e.g. "01/20"
    featured    BOOLEAN        NOT NULL DEFAULT FALSE,
    active      BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_products_category ON products (category);
CREATE INDEX idx_products_active   ON products (active);
CREATE INDEX idx_products_featured ON products (featured);

-- ---------------------------------------------------------------
-- CARTS  (guest carts, identified by browser-session UUID)
-- ---------------------------------------------------------------
CREATE TABLE carts (
    id         BIGSERIAL    PRIMARY KEY,
    session_id VARCHAR(36)  NOT NULL UNIQUE,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_carts_session_id ON carts (session_id);

-- ---------------------------------------------------------------
-- CART ITEMS
-- ---------------------------------------------------------------
CREATE TABLE cart_items (
    id         BIGSERIAL      PRIMARY KEY,
    cart_id    BIGINT         NOT NULL REFERENCES carts(id)    ON DELETE CASCADE,
    product_id BIGINT         NOT NULL REFERENCES products(id),
    quantity   INTEGER        NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(10, 2) NOT NULL,
    UNIQUE (cart_id, product_id)          -- one row per product per cart
);

CREATE INDEX idx_cart_items_cart_id ON cart_items (cart_id);

-- ---------------------------------------------------------------
-- ORDERS
-- ---------------------------------------------------------------
CREATE TABLE orders (
    id               BIGSERIAL      PRIMARY KEY,
    order_number     VARCHAR(30)    NOT NULL UNIQUE,  -- WIM-2026-XXXXXXXX
    session_id       VARCHAR(36)    NOT NULL,
    customer_email   VARCHAR(255)   NOT NULL,
    customer_name    VARCHAR(200)   NOT NULL,
    customer_phone   VARCHAR(30),
    -- Embedded shipping address
    shipping_street  VARCHAR(300)   NOT NULL,
    shipping_city    VARCHAR(100)   NOT NULL,
    shipping_state   VARCHAR(100),
    shipping_zip     VARCHAR(20),
    shipping_country CHAR(2)        NOT NULL,   -- ISO 3166-1 alpha-2
    -- Status & pricing
    status           VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    subtotal         NUMERIC(10, 2) NOT NULL,
    shipping_cost    NUMERIC(10, 2) NOT NULL DEFAULT 0,
    total            NUMERIC(10, 2) NOT NULL,
    notes            TEXT,
    created_at       TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_orders_order_number    ON orders (order_number);
CREATE INDEX idx_orders_session_id      ON orders (session_id);
CREATE INDEX idx_orders_customer_email  ON orders (customer_email);
CREATE INDEX idx_orders_status          ON orders (status);

-- ---------------------------------------------------------------
-- ORDER ITEMS
-- ---------------------------------------------------------------
CREATE TABLE order_items (
    id           BIGSERIAL      PRIMARY KEY,
    order_id     BIGINT         NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id   BIGINT         NOT NULL REFERENCES products(id),
    product_name VARCHAR(200)   NOT NULL,   -- snapshot
    quantity     INTEGER        NOT NULL CHECK (quantity > 0),
    unit_price   NUMERIC(10, 2) NOT NULL    -- snapshot
);

CREATE INDEX idx_order_items_order_id ON order_items (order_id);

-- =============================================================
-- SEED DATA  — demo products matching the frontend mock data
-- =============================================================
INSERT INTO products (name, description, price, image_url, category, stock, edition, featured, active)
VALUES
    ('Remera Evangelion Unit-01',
     'Sublimación full print. Material premium 100% polyester. Edición limitada numerada.',
     4500.00,
     'https://images.unsplash.com/photo-1503342217505-b0a15ec3261c?w=800',
     'APPAREL', 50, '01/20', TRUE, TRUE),

    ('Mug Akira Neo-Tokyo',
     'Cerámica de alta resistencia con diseño full wrap 360°. Apto lavavajillas.',
     2200.00,
     'https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?w=800',
     'MUGS', 30, '01/15', TRUE, TRUE),

    ('Mousepad XXL Ghost in the Shell',
     'Base antideslizante de goma. Sublimación full surface, bordes cosidos.',
     3800.00,
     'https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=800',
     'TECH', 20, '02/10', FALSE, TRUE),

    ('Hoodie Totoro Forest',
     'Buzo con capucha. Diseño bordado al frente + sublimación en espalda completa.',
     8900.00,
     'https://images.unsplash.com/photo-1556821840-3a63f15732ce?w=800',
     'APPAREL', 15, '01/10', TRUE, TRUE),

    ('Mug Demon Slayer Hashiras',
     'Taza mágica: cambia de imagen con el calor. Colección aniversario 2026.',
     2800.00,
     'https://images.unsplash.com/photo-1572635196237-14b3f281503f?w=800',
     'MUGS', 40, NULL, FALSE, TRUE),

    ('Desk Mat Cyberpunk City 900×400',
     'Pad de escritorio extra-large. Sublimación 4K, superficie de tela de alta precisión.',
     5500.00,
     'https://images.unsplash.com/photo-1593640495253-23196b27a87f?w=800',
     'TECH', 25, '03/20', TRUE, TRUE);
