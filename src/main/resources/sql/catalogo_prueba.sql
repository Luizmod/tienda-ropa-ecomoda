-- Catálogo de ejemplo. Conserva las prendas y las cantidades ya registradas.
INSERT INTO categoria (nombre, descripcion) VALUES
    ('Básicos', 'Camisetas y prendas de uso diario.'),
    ('Abrigos', 'Capas ligeras para clima fresco.'),
    ('Pantalones', 'Pantalones de mezclilla y gabardina.'),
    ('Vestidos y faldas', 'Prendas de corte fluido.')
ON DUPLICATE KEY UPDATE nombre = categoria.nombre;

INSERT INTO prenda (id_categoria, nombre, descripcion, precio)
SELECT c.id_categoria, muestra.nombre, muestra.descripcion, muestra.precio
FROM (
    SELECT 'Básicos' AS categoria, 'Camiseta de algodón' AS nombre,
        'Camiseta de corte recto.' AS descripcion, 8500.00 AS precio
    UNION ALL SELECT 'Básicos', 'Camisa de lino',
        'Camisa de manga larga con botones y cuello clásico.', 16500.00
    UNION ALL SELECT 'Abrigos', 'Chaqueta ligera',
        'Chaqueta con cierre y dos bolsillos.', 24900.00
    UNION ALL SELECT 'Abrigos', 'Sudadera con capucha',
        'Sudadera de algodón con bolsillo delantero.', 18900.00
    UNION ALL SELECT 'Pantalones', 'Jeans de corte recto',
        'Jeans de tiro medio en mezclilla azul.', 22900.00
    UNION ALL SELECT 'Pantalones', 'Pantalón cargo',
        'Pantalón de gabardina con bolsillos laterales.', 21500.00
    UNION ALL SELECT 'Vestidos y faldas', 'Vestido midi',
        'Vestido sin mangas con falda de caída fluida.', 27500.00
    UNION ALL SELECT 'Vestidos y faldas', 'Falda plisada',
        'Falda de largo medio con cintura elástica.', 15900.00
) AS muestra
JOIN categoria c ON c.nombre = muestra.categoria
WHERE NOT EXISTS (SELECT 1 FROM prenda p WHERE p.nombre = muestra.nombre);

INSERT INTO inventario (id_prenda, talla, cantidad)
SELECT prendas.id_prenda, variantes.talla, variantes.cantidad
FROM (
    SELECT 'Camiseta de algodón' AS nombre, 'S' AS talla, 12 AS cantidad
    UNION ALL SELECT 'Camiseta de algodón', 'M', 8
    UNION ALL SELECT 'Camiseta de algodón', 'L', 0
    UNION ALL SELECT 'Camisa de lino', 'S', 4
    UNION ALL SELECT 'Camisa de lino', 'M', 7
    UNION ALL SELECT 'Camisa de lino', 'L', 3
    UNION ALL SELECT 'Chaqueta ligera', 'M', 5
    UNION ALL SELECT 'Chaqueta ligera', 'L', 6
    UNION ALL SELECT 'Sudadera con capucha', 'M', 6
    UNION ALL SELECT 'Sudadera con capucha', 'L', 4
    UNION ALL SELECT 'Sudadera con capucha', 'XL', 2
    UNION ALL SELECT 'Jeans de corte recto', '28', 5
    UNION ALL SELECT 'Jeans de corte recto', '30', 8
    UNION ALL SELECT 'Jeans de corte recto', '32', 3
    UNION ALL SELECT 'Pantalón cargo', '30', 6
    UNION ALL SELECT 'Pantalón cargo', '32', 4
    UNION ALL SELECT 'Vestido midi', 'S', 3
    UNION ALL SELECT 'Vestido midi', 'M', 5
    UNION ALL SELECT 'Vestido midi', 'L', 2
    UNION ALL SELECT 'Falda plisada', 'S', 4
    UNION ALL SELECT 'Falda plisada', 'M', 6
) AS variantes
JOIN (SELECT nombre, MIN(id_prenda) AS id_prenda FROM prenda GROUP BY nombre) AS prendas
    ON prendas.nombre = variantes.nombre
ON DUPLICATE KEY UPDATE talla = inventario.talla;
