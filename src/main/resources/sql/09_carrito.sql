USE ecomoda;

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
