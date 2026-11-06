-- Catálogo mínimo para consultar prendas y existencias desde HU-5.
USE ecomoda;

CREATE TABLE IF NOT EXISTS prenda (
    id_prenda BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(120) NOT NULL,
    descripcion VARCHAR(500) NOT NULL,
    precio DECIMAL(12, 2) NOT NULL,
    ruta_imagen VARCHAR(2048),
    PRIMARY KEY (id_prenda),
    CONSTRAINT ck_prenda_precio CHECK (precio >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
