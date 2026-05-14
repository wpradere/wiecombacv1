-- =============================================================
-- V4__add_product_images.sql
-- Product gallery — stores extra images per product
-- =============================================================

CREATE TABLE product_images (
    id         BIGSERIAL    PRIMARY KEY,
    product_id BIGINT       NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    image_url  VARCHAR(500) NOT NULL,
    alt_text   VARCHAR(200),
    sort_order INTEGER      NOT NULL DEFAULT 0
);

CREATE INDEX idx_product_images_product_id ON product_images (product_id);

-- =============================================================
-- SEED — 2–3 extra images per product (sort_order 0 = first extra)
-- =============================================================

-- Product 1: Remera Evangelion Unit-01
INSERT INTO product_images (product_id, image_url, alt_text, sort_order) VALUES
    (1, 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=800', 'Vista espalda', 0),
    (1, 'https://images.unsplash.com/photo-1583743814966-8936f5b7be1a?w=800', 'Detalle tela',  1);

-- Product 2: Mug Akira Neo-Tokyo
INSERT INTO product_images (product_id, image_url, alt_text, sort_order) VALUES
    (2, 'https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=800', 'Vista lateral',  0),
    (2, 'https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=800', 'Detalle diseño', 1);

-- Product 3: Mousepad XXL Ghost in the Shell
INSERT INTO product_images (product_id, image_url, alt_text, sort_order) VALUES
    (3, 'https://images.unsplash.com/photo-1593640495253-23196b27a87f?w=800', 'En escritorio',    0),
    (3, 'https://images.unsplash.com/photo-1616401784845-180882ba9ba8?w=800', 'Detalle superficie', 1);

-- Product 4: Hoodie Totoro Forest
INSERT INTO product_images (product_id, image_url, alt_text, sort_order) VALUES
    (4, 'https://images.unsplash.com/photo-1556821840-3a63f15732ce?w=800', 'Vista frontal',  0),
    (4, 'https://images.unsplash.com/photo-1521223890158-f9f7c3d5d504?w=800', 'Detalle capucha', 1),
    (4, 'https://images.unsplash.com/photo-1578587018452-892bacefd3f2?w=800', 'Detalle espalda', 2);

-- Product 5: Mug Demon Slayer Hashiras
INSERT INTO product_images (product_id, image_url, alt_text, sort_order) VALUES
    (5, 'https://images.unsplash.com/photo-1511920170033-f8396924c348?w=800', 'Con café caliente', 0),
    (5, 'https://images.unsplash.com/photo-1534040385115-33dcb3acba5b?w=800', 'Efecto térmico',    1);

-- Product 6: Desk Mat Cyberpunk City
INSERT INTO product_images (product_id, image_url, alt_text, sort_order) VALUES
    (6, 'https://images.unsplash.com/photo-1547082299-de196ea013d6?w=800', 'Setup completo',   0),
    (6, 'https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=800', 'Detalle bordes', 1);
