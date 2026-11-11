-- Actualiza los ejemplos ya cargados conservando sus identificadores.
UPDATE categoria anterior
LEFT JOIN categoria actual ON actual.nombre = SUBSTRING(anterior.nombre, 8)
SET anterior.nombre = SUBSTRING(anterior.nombre, 8)
WHERE anterior.nombre IN ('Demo · Básicos', 'Demo · Abrigos', 'Demo · Pantalones', 'Demo · Vestidos y faldas')
    AND actual.id_categoria IS NULL;

UPDATE prenda
SET nombre = SUBSTRING(nombre, 8)
WHERE nombre IN ('Demo · Camiseta de algodón', 'Demo · Camisa de lino', 'Demo · Chaqueta ligera',
    'Demo · Sudadera con capucha', 'Demo · Jeans de corte recto', 'Demo · Pantalón cargo',
    'Demo · Vestido midi', 'Demo · Falda plisada');

UPDATE consulta c JOIN usuario u ON u.id_usuario = c.id_usuario
SET c.asunto = SUBSTRING(c.asunto, 8)
WHERE u.correo = 'cliente@ecomoda.example.invalid' AND c.asunto IN ('Demo · Guía de tallas', 'Demo · Disponibilidad');

UPDATE direccion_envio d JOIN usuario u ON u.id_usuario = d.id_usuario
SET d.direccion_exacta = 'Dirección ficticia'
WHERE u.correo = 'cliente@ecomoda.example.invalid' AND d.direccion_exacta = 'Demo · Dirección ficticia';

UPDATE metodo_pago m JOIN usuario u ON u.id_usuario = m.id_usuario
SET m.titular = 'Titular ficticio'
WHERE u.correo = 'cliente@ecomoda.example.invalid' AND m.tipo = 'TARJETA'
    AND m.titular = 'Demo · Titular ficticio';

UPDATE cupon anterior
LEFT JOIN cupon actual ON actual.codigo = CASE anterior.codigo
    WHEN 'DEMO10' THEN 'ESTILO10' WHEN 'DEMO-VENCIDO' THEN 'OFERTA-VENCIDA' END
SET anterior.codigo = CASE anterior.codigo
    WHEN 'DEMO10' THEN 'ESTILO10' WHEN 'DEMO-VENCIDO' THEN 'OFERTA-VENCIDA' END
WHERE anterior.codigo IN ('DEMO10', 'DEMO-VENCIDO') AND actual.id_cupon IS NULL;

UPDATE promocion SET nombre = 'Estrena básicos' WHERE nombre = 'Demo · Estrena básicos';
