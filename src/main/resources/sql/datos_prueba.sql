-- Carga opcional del perfil demo. El hash BCrypt lo suministra la aplicación.
INSERT INTO usuario (correo, contrasena, rol) VALUES
    ('admin@ecomoda.example.invalid', @demo_hash, 'ADMIN'),
    ('cliente@ecomoda.example.invalid', @demo_hash, 'CLIENTE')
ON DUPLICATE KEY UPDATE correo = usuario.correo;

SET @demo_cliente = (SELECT id_usuario FROM usuario WHERE correo = 'cliente@ecomoda.example.invalid');

SET @demo_camiseta = (SELECT MIN(id_prenda) FROM prenda WHERE nombre = 'Camiseta de algodón');

INSERT INTO carrito_linea (id_usuario, id_prenda, talla, cantidad)
VALUES (@demo_cliente, @demo_camiseta, 'M', 1)
ON DUPLICATE KEY UPDATE talla = carrito_linea.talla;

INSERT INTO consulta (id_usuario, correo, asunto, mensaje, estado, respuesta)
SELECT @demo_cliente, 'cliente@ecomoda.example.invalid', 'Guía de tallas',
    '¿La camiseta de algodón usa tallas estándar?', 'RESPONDIDA', 'Sí, las tallas son estándar.'
WHERE NOT EXISTS (SELECT 1 FROM consulta WHERE id_usuario = @demo_cliente AND asunto = 'Guía de tallas');
INSERT INTO consulta (id_usuario, correo, asunto, mensaje)
SELECT @demo_cliente, 'cliente@ecomoda.example.invalid', 'Disponibilidad',
    '¿Volverá a estar disponible la talla L?'
WHERE NOT EXISTS (SELECT 1 FROM consulta WHERE id_usuario = @demo_cliente AND asunto = 'Disponibilidad');

INSERT INTO direccion_envio (id_usuario, provincia, canton, distrito, direccion_exacta, telefono)
SELECT @demo_cliente, 'San José', 'Montes de Oca', 'San Pedro', 'Dirección ficticia', '0000-0000'
WHERE NOT EXISTS (SELECT 1 FROM direccion_envio
    WHERE id_usuario = @demo_cliente AND direccion_exacta = 'Dirección ficticia');

INSERT INTO metodo_pago (id_usuario, tipo, titular, ultimos_cuatro)
SELECT @demo_cliente, 'TARJETA', 'Titular ficticio', '0042'
WHERE NOT EXISTS (SELECT 1 FROM metodo_pago WHERE id_usuario = @demo_cliente AND tipo = 'TARJETA'
    AND titular = 'Titular ficticio');
INSERT INTO metodo_pago (id_usuario, tipo)
SELECT @demo_cliente, 'CONTRA_ENTREGA'
WHERE NOT EXISTS (SELECT 1 FROM metodo_pago WHERE id_usuario = @demo_cliente AND tipo = 'CONTRA_ENTREGA');

INSERT INTO cupon (codigo, porcentaje, fecha_vencimiento, activo) VALUES
    ('ESTILO10', 10, DATE_ADD(CURRENT_DATE, INTERVAL 1 YEAR), TRUE),
    ('OFERTA-VENCIDA', 20, DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), TRUE)
ON DUPLICATE KEY UPDATE codigo = cupon.codigo;

INSERT INTO promocion (nombre, descripcion, porcentaje, fecha_inicio, fecha_fin)
SELECT 'Estrena básicos', 'Descuento de temporada en la colección de básicos.', 15,
    CURRENT_DATE, DATE_ADD(CURRENT_DATE, INTERVAL 1 YEAR)
WHERE NOT EXISTS (SELECT 1 FROM promocion WHERE nombre = 'Estrena básicos');
