-- =============================================================
-- V9__categories_table.sql
-- Replaces the category VARCHAR column on products with a FK
-- to a new categories table.
-- =============================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ── CATEGORIES ────────────────────────────────────────────────────────────────
CREATE TABLE categories (
    id     UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    name   VARCHAR(50)  NOT NULL UNIQUE,
    label  VARCHAR(100),
    active BOOLEAN      NOT NULL DEFAULT TRUE
);

-- Seed the four initial categories
INSERT INTO categories (name, label) VALUES
    ('APPAREL', 'Indumentaria'),
    ('TECH',    'Tech'),
    ('MUGS',    'Cerámica'),
    ('TOY',     'Juguetes');

-- ── MIGRATE products.category ─────────────────────────────────────────────────
ALTER TABLE products ADD COLUMN category_id UUID REFERENCES categories(id);

UPDATE products p
SET category_id = c.id
FROM categories c
WHERE p.category = c.name;

-- Any product whose category string didn't match defaults to APPAREL
UPDATE products
SET category_id = (SELECT id FROM categories WHERE name = 'APPAREL')
WHERE category_id IS NULL;

ALTER TABLE products ALTER COLUMN category_id SET NOT NULL;

DROP INDEX IF EXISTS idx_products_category;
ALTER TABLE products DROP COLUMN category;

CREATE INDEX idx_products_category_id ON products (category_id);
