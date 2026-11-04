-- Base de datos y usuario local para Ecomoda (MySQL 8+).
-- Reemplace CAMBIAR_CLAVE por una clave elegida localmente antes de ejecutar.
CREATE DATABASE IF NOT EXISTS ecomoda
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'ecomoda_dev'@'localhost'
    IDENTIFIED BY 'CAMBIAR_CLAVE';

GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, DROP
    ON ecomoda.* TO 'ecomoda_dev'@'localhost';

FLUSH PRIVILEGES;
