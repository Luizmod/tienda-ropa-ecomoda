USE ecomoda;

-- Ejecutar con una cuenta que tenga permisos de modificación del esquema.
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
