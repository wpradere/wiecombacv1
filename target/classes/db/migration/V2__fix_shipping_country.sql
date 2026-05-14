-- Fix: shipping_country was CHAR(2) (bpchar) but Hibernate expects VARCHAR(2)
ALTER TABLE orders ALTER COLUMN shipping_country TYPE VARCHAR(2);
