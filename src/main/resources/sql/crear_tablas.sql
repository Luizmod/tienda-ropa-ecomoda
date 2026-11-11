-- Esquema local de Ecomoda (HU-1 a HU-15) para MySQL 8.0.16 o posterior.
CREATE DATABASE IF NOT EXISTS ecomoda
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE ecomoda;

CREATE TABLE IF NOT EXISTS usuario (
    id_usuario BIGINT NOT NULL AUTO_INCREMENT,
    correo VARCHAR(254) NOT NULL,
    contrasena VARCHAR(60) NOT NULL,
    rol VARCHAR(7) NOT NULL DEFAULT 'CLIENTE',
    PRIMARY KEY (id_usuario),
    CONSTRAINT uk_usuario_correo UNIQUE (correo),
    CONSTRAINT ck_usuario_rol CHECK (rol IN ('CLIENTE', 'ADMIN'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- B y C: referenciar usuario(id_usuario) con BIGINT y ON DELETE RESTRICT.
-- ADMIN se asigna manualmente a una cuenta existente; no hay claves de ejemplo.

CREATE TABLE IF NOT EXISTS categoria (
    id_categoria BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(120) NOT NULL,
    descripcion VARCHAR(500) NOT NULL,
    PRIMARY KEY (id_categoria),
    CONSTRAINT uk_categoria_nombre UNIQUE (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS prenda (
    id_prenda BIGINT NOT NULL AUTO_INCREMENT,
    id_categoria BIGINT,
    nombre VARCHAR(120) NOT NULL,
    descripcion VARCHAR(500) NOT NULL,
    precio DECIMAL(12, 2) NOT NULL,
    ruta_imagen VARCHAR(2048),
    PRIMARY KEY (id_prenda),
    CONSTRAINT fk_prenda_categoria FOREIGN KEY (id_categoria)
        REFERENCES categoria (id_categoria) ON DELETE RESTRICT,
    CONSTRAINT ck_prenda_precio CHECK (precio >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Agrega la categoría a instalaciones que ya usaban el catálogo del avance 2.
SET @ddl_prenda = IF(
    EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'prenda' AND column_name = 'id_categoria'
    ),
    'DO 0',
    'ALTER TABLE prenda ADD COLUMN id_categoria BIGINT,
        ADD CONSTRAINT fk_prenda_categoria FOREIGN KEY (id_categoria)
        REFERENCES categoria (id_categoria) ON DELETE RESTRICT'
);
PREPARE ddl_prenda FROM @ddl_prenda;
EXECUTE ddl_prenda;
DEALLOCATE PREPARE ddl_prenda;

CREATE TABLE IF NOT EXISTS inventario (
    id_inventario BIGINT NOT NULL AUTO_INCREMENT,
    id_prenda BIGINT NOT NULL,
    talla VARCHAR(20) NOT NULL,
    cantidad INT NOT NULL,
    PRIMARY KEY (id_inventario),
    CONSTRAINT uk_inventario_prenda_talla UNIQUE (id_prenda, talla),
    CONSTRAINT fk_inventario_prenda FOREIGN KEY (id_prenda)
        REFERENCES prenda (id_prenda) ON DELETE RESTRICT,
    CONSTRAINT ck_inventario_cantidad CHECK (cantidad >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS cupon (
    id_cupon BIGINT NOT NULL AUTO_INCREMENT,
    id_usuario BIGINT,
    codigo VARCHAR(40) NOT NULL,
    porcentaje DECIMAL(5, 2) NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id_cupon),
    CONSTRAINT uk_cupon_codigo UNIQUE (codigo),
    CONSTRAINT fk_cupon_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuario (id_usuario) ON DELETE CASCADE,
    CONSTRAINT ck_cupon_porcentaje CHECK (porcentaje > 0 AND porcentaje <= 100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Agrega la asignación a instalaciones que ya tienen cupones.
SET @ddl_cupon_usuario = IF(
    EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'cupon' AND column_name = 'id_usuario'
    ),
    'DO 0',
    'ALTER TABLE cupon ADD COLUMN id_usuario BIGINT,
        ADD CONSTRAINT fk_cupon_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuario (id_usuario) ON DELETE CASCADE'
);
PREPARE ddl_cupon_usuario FROM @ddl_cupon_usuario;
EXECUTE ddl_cupon_usuario;
DEALLOCATE PREPARE ddl_cupon_usuario;

CREATE TABLE IF NOT EXISTS carrito_linea (
    id_linea BIGINT NOT NULL AUTO_INCREMENT,
    id_usuario BIGINT NOT NULL,
    id_prenda BIGINT NOT NULL,
    talla VARCHAR(20) NOT NULL,
    cantidad INT NOT NULL,
    PRIMARY KEY (id_linea),
    CONSTRAINT uk_carrito_usuario_prenda_talla UNIQUE (id_usuario, id_prenda, talla),
    CONSTRAINT fk_carrito_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuario (id_usuario) ON DELETE RESTRICT,
    CONSTRAINT fk_carrito_inventario FOREIGN KEY (id_prenda, talla)
        REFERENCES inventario (id_prenda, talla) ON DELETE RESTRICT,
    CONSTRAINT ck_carrito_cantidad CHECK (cantidad > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS consulta (
    id_consulta BIGINT NOT NULL AUTO_INCREMENT,
    id_usuario BIGINT NOT NULL,
    correo VARCHAR(254) NOT NULL,
    asunto VARCHAR(150) NOT NULL,
    mensaje TEXT NOT NULL,
    estado VARCHAR(12) NOT NULL DEFAULT 'PENDIENTE',
    respuesta TEXT,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_consulta),
    CONSTRAINT fk_consulta_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuario (id_usuario) ON DELETE RESTRICT,
    CONSTRAINT ck_consulta_estado CHECK (estado IN ('PENDIENTE', 'RESPONDIDA'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS direccion_envio (
    id_direccion BIGINT NOT NULL AUTO_INCREMENT,
    id_usuario BIGINT NOT NULL,
    provincia VARCHAR(80) NOT NULL,
    canton VARCHAR(80) NOT NULL,
    distrito VARCHAR(80) NOT NULL,
    direccion_exacta VARCHAR(500) NOT NULL,
    telefono VARCHAR(20) NOT NULL,
    PRIMARY KEY (id_direccion),
    CONSTRAINT uk_direccion_usuario UNIQUE (id_usuario, id_direccion),
    CONSTRAINT fk_direccion_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuario (id_usuario) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS metodo_pago (
    id_metodo_pago BIGINT NOT NULL AUTO_INCREMENT,
    id_usuario BIGINT NOT NULL,
    tipo VARCHAR(16) NOT NULL,
    titular VARCHAR(120),
    ultimos_cuatro CHAR(4),
    telefono_sinpe VARCHAR(20),
    PRIMARY KEY (id_metodo_pago),
    CONSTRAINT uk_metodo_pago_usuario UNIQUE (id_usuario, id_metodo_pago),
    CONSTRAINT fk_metodo_pago_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuario (id_usuario) ON DELETE RESTRICT,
    CONSTRAINT ck_metodo_pago_datos CHECK (
        (tipo = 'TARJETA' AND titular IS NOT NULL AND ultimos_cuatro IS NOT NULL
            AND ultimos_cuatro REGEXP '^[0-9]{4}$' AND telefono_sinpe IS NULL)
        OR (tipo = 'SINPE_MOVIL' AND telefono_sinpe IS NOT NULL AND ultimos_cuatro IS NULL)
        OR (tipo = 'CONTRA_ENTREGA' AND ultimos_cuatro IS NULL AND telefono_sinpe IS NULL)
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS pedido (
    id_pedido BIGINT NOT NULL AUTO_INCREMENT,
    id_usuario BIGINT NOT NULL,
    id_direccion BIGINT NOT NULL,
    id_metodo_pago BIGINT NOT NULL,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total DECIMAL(14, 2) NOT NULL,
    estado VARCHAR(12) NOT NULL DEFAULT 'PENDIENTE',
    PRIMARY KEY (id_pedido),
    CONSTRAINT fk_pedido_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuario (id_usuario) ON DELETE RESTRICT,
    CONSTRAINT fk_pedido_direccion FOREIGN KEY (id_usuario, id_direccion)
        REFERENCES direccion_envio (id_usuario, id_direccion) ON DELETE RESTRICT,
    CONSTRAINT fk_pedido_metodo_pago FOREIGN KEY (id_usuario, id_metodo_pago)
        REFERENCES metodo_pago (id_usuario, id_metodo_pago) ON DELETE RESTRICT,
    CONSTRAINT ck_pedido_total CHECK (total >= 0),
    CONSTRAINT ck_pedido_estado CHECK (
        estado IN ('PENDIENTE', 'PROCESANDO', 'ENVIADO', 'ENTREGADO', 'CANCELADO')
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS pedido_detalle (
    id_detalle BIGINT NOT NULL AUTO_INCREMENT,
    id_pedido BIGINT NOT NULL,
    id_prenda BIGINT NOT NULL,
    talla VARCHAR(20) NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(12, 2) NOT NULL,
    PRIMARY KEY (id_detalle),
    CONSTRAINT uk_detalle_pedido_variante UNIQUE (id_pedido, id_prenda, talla),
    CONSTRAINT fk_detalle_pedido FOREIGN KEY (id_pedido)
        REFERENCES pedido (id_pedido) ON DELETE RESTRICT,
    CONSTRAINT fk_detalle_prenda FOREIGN KEY (id_prenda)
        REFERENCES prenda (id_prenda) ON DELETE RESTRICT,
    CONSTRAINT ck_detalle_cantidad CHECK (cantidad > 0),
    CONSTRAINT ck_detalle_precio CHECK (precio_unitario >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS seguimiento_pedido (
    id_seguimiento BIGINT NOT NULL AUTO_INCREMENT,
    id_pedido BIGINT NOT NULL,
    estado VARCHAR(12) NOT NULL,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_seguimiento),
    CONSTRAINT fk_seguimiento_pedido FOREIGN KEY (id_pedido)
        REFERENCES pedido (id_pedido) ON DELETE RESTRICT,
    CONSTRAINT ck_seguimiento_estado CHECK (
        estado IN ('PENDIENTE', 'PROCESANDO', 'ENVIADO', 'ENTREGADO', 'CANCELADO')
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS resena (
    id_resena BIGINT NOT NULL AUTO_INCREMENT,
    id_usuario BIGINT NOT NULL,
    id_prenda BIGINT NOT NULL,
    calificacion TINYINT NOT NULL,
    comentario TEXT NOT NULL,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_resena),
    CONSTRAINT uk_resena_usuario_prenda UNIQUE (id_usuario, id_prenda),
    CONSTRAINT fk_resena_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuario (id_usuario) ON DELETE RESTRICT,
    CONSTRAINT fk_resena_prenda FOREIGN KEY (id_prenda)
        REFERENCES prenda (id_prenda) ON DELETE RESTRICT,
    CONSTRAINT ck_resena_calificacion CHECK (calificacion BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS lista_deseos (
    id_deseo BIGINT NOT NULL AUTO_INCREMENT,
    id_usuario BIGINT NOT NULL,
    id_prenda BIGINT NOT NULL,
    talla VARCHAR(20) NOT NULL,
    PRIMARY KEY (id_deseo),
    CONSTRAINT uk_deseos_usuario_variante UNIQUE (id_usuario, id_prenda, talla),
    CONSTRAINT fk_deseos_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuario (id_usuario) ON DELETE RESTRICT,
    CONSTRAINT fk_deseos_inventario FOREIGN KEY (id_prenda, talla)
        REFERENCES inventario (id_prenda, talla) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS preferencia_notificacion (
    id_usuario BIGINT NOT NULL,
    tipo VARCHAR(12) NOT NULL,
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id_usuario, tipo),
    CONSTRAINT fk_preferencia_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuario (id_usuario) ON DELETE RESTRICT,
    CONSTRAINT ck_preferencia_tipo CHECK (tipo IN ('PEDIDOS', 'PROMOCIONES', 'NOVEDADES')),
    CONSTRAINT ck_preferencia_activa CHECK (activa IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS notificacion (
    id_notificacion BIGINT NOT NULL AUTO_INCREMENT,
    id_usuario BIGINT NOT NULL,
    tipo VARCHAR(12) NOT NULL,
    titulo VARCHAR(150) NOT NULL,
    mensaje TEXT NOT NULL,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_lectura DATETIME,
    PRIMARY KEY (id_notificacion),
    CONSTRAINT fk_notificacion_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuario (id_usuario) ON DELETE RESTRICT,
    CONSTRAINT ck_notificacion_tipo CHECK (tipo IN ('PEDIDOS', 'PROMOCIONES', 'NOVEDADES'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS promocion (
    id_promocion BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(120) NOT NULL,
    descripcion VARCHAR(500) NOT NULL,
    porcentaje DECIMAL(5, 2) NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    PRIMARY KEY (id_promocion),
    CONSTRAINT ck_promocion_porcentaje CHECK (porcentaje > 0 AND porcentaje <= 100),
    CONSTRAINT ck_promocion_vigencia CHECK (fecha_inicio <= fecha_fin)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
