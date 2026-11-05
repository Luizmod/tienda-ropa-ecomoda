USE ecomoda;

CREATE TABLE IF NOT EXISTS cupon (
    id_cupon BIGINT NOT NULL AUTO_INCREMENT,
    codigo VARCHAR(40) NOT NULL,
    porcentaje DECIMAL(5, 2) NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id_cupon),
    CONSTRAINT uk_cupon_codigo UNIQUE (codigo),
    CONSTRAINT ck_cupon_porcentaje CHECK (porcentaje > 0 AND porcentaje <= 100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
