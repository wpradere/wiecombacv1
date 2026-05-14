-- Expand price columns from NUMERIC(10,2) to NUMERIC(12,2) to support ARS amounts
ALTER TABLE products    ALTER COLUMN price        TYPE NUMERIC(12, 2);
ALTER TABLE cart_items  ALTER COLUMN unit_price   TYPE NUMERIC(12, 2);
ALTER TABLE order_items ALTER COLUMN unit_price   TYPE NUMERIC(12, 2);
ALTER TABLE orders      ALTER COLUMN subtotal     TYPE NUMERIC(12, 2);
ALTER TABLE orders      ALTER COLUMN shipping_cost TYPE NUMERIC(12, 2);
ALTER TABLE orders      ALTER COLUMN total        TYPE NUMERIC(12, 2);
