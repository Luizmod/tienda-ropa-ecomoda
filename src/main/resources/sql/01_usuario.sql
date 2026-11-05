-- Ejecutar manualmente sobre ecomoda antes de iniciar HU-2.
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
