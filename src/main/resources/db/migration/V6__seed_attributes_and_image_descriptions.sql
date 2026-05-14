-- =============================================================
-- V6 — Seed product attributes + image descriptions
-- =============================================================

-- ── Image descriptions ────────────────────────────────────────

-- Product 1: Remera Evangelion Unit-01
UPDATE product_images SET description = 'Sublimación full back con el diseño completo del Evangelion Unit-01'
    WHERE product_id = 1 AND sort_order = 0;
UPDATE product_images SET description = 'Primer plano de la tela premium 100% polyester — textura suave y transpirable'
    WHERE product_id = 1 AND sort_order = 1;

-- Product 2: Mug Akira Neo-Tokyo
UPDATE product_images SET description = 'Vista lateral mostrando el diseño wrap 360° con Neo-Tokyo de fondo'
    WHERE product_id = 2 AND sort_order = 0;
UPDATE product_images SET description = 'Detalle del arte Akira en alta resolución sobre cerámica blanca mate'
    WHERE product_id = 2 AND sort_order = 1;

-- Product 3: Mousepad XXL Ghost in the Shell
UPDATE product_images SET description = 'Pad en un setup real — 900×400mm cubre teclado y mouse con espacio de sobra'
    WHERE product_id = 3 AND sort_order = 0;
UPDATE product_images SET description = 'Primer plano de la superficie de tela de alta precisión y el cosido perimetral'
    WHERE product_id = 3 AND sort_order = 1;

-- Product 4: Hoodie Totoro Forest
UPDATE product_images SET description = 'Vista frontal con bordado Totoro sobre pecho izquierdo'
    WHERE product_id = 4 AND sort_order = 0;
UPDATE product_images SET description = 'Detalle de la capucha forrada con interior suave antipelusa'
    WHERE product_id = 4 AND sort_order = 1;
UPDATE product_images SET description = 'Sublimación full back — bosque de Totoro en colores pastel'
    WHERE product_id = 4 AND sort_order = 2;

-- Product 5: Mug Demon Slayer Hashiras
UPDATE product_images SET description = 'Con café caliente — el diseño aparece completamente revelado a partir de 45°C'
    WHERE product_id = 5 AND sort_order = 0;
UPDATE product_images SET description = 'Comparativa frío/caliente mostrando el efecto de cambio de imagen'
    WHERE product_id = 5 AND sort_order = 1;

-- Product 6: Desk Mat Cyberpunk City
UPDATE product_images SET description = 'Setup completo: el pad ocupa el escritorio entero con espacio para teclado 100% y mouse'
    WHERE product_id = 6 AND sort_order = 0;
UPDATE product_images SET description = 'Detalle del borde cosido reforzado — no se deshilacha con el uso diario'
    WHERE product_id = 6 AND sort_order = 1;

-- ── Product attributes ────────────────────────────────────────

-- Product 1: Remera Evangelion Unit-01
INSERT INTO product_attributes (product_id, name, value, sort_order) VALUES
    (1, 'Material',    '100% polyester premium',         0),
    (1, 'Técnica',     'Sublimación full print',          1),
    (1, 'Talles',      'S / M / L / XL / XXL',           2),
    (1, 'Edición',     'Limitada 01/20 — numerada',       3),
    (1, 'Cuidado',     'Lavar al revés a 30°C, no secar en secadora', 4);

-- Product 2: Mug Akira Neo-Tokyo
INSERT INTO product_attributes (product_id, name, value, sort_order) VALUES
    (2, 'Capacidad',       '350 ml',                    0),
    (2, 'Material',        'Cerámica de alta resistencia', 1),
    (2, 'Técnica',         'Sublimación wrap 360°',      2),
    (2, 'Apto lavavajillas', 'Sí',                      3),
    (2, 'Apto microondas', 'No',                         4),
    (2, 'Edición',         'Limitada 01/15',             5);

-- Product 3: Mousepad XXL Ghost in the Shell
INSERT INTO product_attributes (product_id, name, value, sort_order) VALUES
    (3, 'Dimensiones',  '900 × 400 mm',                  0),
    (3, 'Grosor',       '4 mm',                           1),
    (3, 'Base',         'Goma antideslizante',             2),
    (3, 'Superficie',   'Tela de microfibra',              3),
    (3, 'Bordes',       'Cosidos perimetrales',            4),
    (3, 'Edición',      'Limitada 02/10',                  5);

-- Product 4: Hoodie Totoro Forest
INSERT INTO product_attributes (product_id, name, value, sort_order) VALUES
    (4, 'Material',     '80% algodón / 20% poliéster',    0),
    (4, 'Técnica',      'Bordado al frente + sublimación espalda', 1),
    (4, 'Talles',       'S / M / L / XL / XXL',           2),
    (4, 'Bolsillo',     'Canguro frontal',                 3),
    (4, 'Interior',     'Polar suave antipelusa',          4),
    (4, 'Edición',      'Limitada 01/10',                  5),
    (4, 'Cuidado',      'Lavar a 30°C, no planchar el bordado', 6);

-- Product 5: Mug Demon Slayer Hashiras
INSERT INTO product_attributes (product_id, name, value, sort_order) VALUES
    (5, 'Capacidad',    '300 ml',                          0),
    (5, 'Tipo',         'Mágica — cambia con el calor',    1),
    (5, 'Activación',   'Líquidos a partir de 45°C',       2),
    (5, 'Material',     'Cerámica',                        3),
    (5, 'Colección',    'Aniversario Demon Slayer 2026',   4),
    (5, 'Apto lavavajillas', 'No — lavado a mano',         5);

-- Product 6: Desk Mat Cyberpunk City 900×400
INSERT INTO product_attributes (product_id, name, value, sort_order) VALUES
    (6, 'Dimensiones',  '900 × 400 mm',                   0),
    (6, 'Grosor',       '3 mm',                            1),
    (6, 'Superficie',   'Tela de alta precisión',          2),
    (6, 'Resolución',   'Impresión 4K',                    3),
    (6, 'Bordes',       'Cosidos reforzados',               4),
    (6, 'Edición',      'Limitada 03/20',                   5);
