-- ============================================================
-- V5 — Add description to product_images + product_attributes
-- ============================================================

-- Add optional description column to gallery images
ALTER TABLE product_images
    ADD COLUMN IF NOT EXISTS description TEXT;

-- ── Product attributes ────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS product_attributes (
    id          BIGSERIAL     PRIMARY KEY,
    product_id  BIGINT        NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    name        VARCHAR(100)  NOT NULL,
    value       VARCHAR(500)  NOT NULL,
    sort_order  INT           NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_product_attributes_product_id
    ON product_attributes(product_id);
