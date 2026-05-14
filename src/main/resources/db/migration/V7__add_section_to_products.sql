-- =============================================================
-- V7 — Add section column to products
-- =============================================================

ALTER TABLE products
    ADD COLUMN IF NOT EXISTS section VARCHAR(20) NOT NULL DEFAULT 'SUBLIMACION';

CREATE INDEX IF NOT EXISTS idx_products_section ON products(section);

-- Existing products belong to the sublimation section (already defaulted above)
-- New ZONA_GEEK products will be assigned via the API
