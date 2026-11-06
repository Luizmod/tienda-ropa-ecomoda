# Ecomoda

Base de una tienda de ropa desarrollada con Spring Boot 4.1.x, Java 21,
Thymeleaf, Spring Data JPA, MySQL y Spring Security. Firebase Storage es
opcional y permanece desactivado por defecto.

## Requisitos

- JDK 21.
- Apache Maven 3.9 o posterior.
- MySQL 8 o posterior, iniciado localmente.
- Git.

## Crear la base de datos local

1. Abra `src/main/resources/sql/00_base.sql` y sustituya
   `CAMBIAR_CLAVE` por una clave propia para su entorno local. No use ni
   comparta esa clave en archivos versionados.
2. Ejecute el script con una cuenta administradora de MySQL, por ejemplo:

   ```bash
   mysql -u root -p < src/main/resources/sql/00_base.sql
   ```

El script crea la base `ecomoda`, codificada en `utf8mb4`, el usuario local
`ecomoda_dev` y sus permisos. Los scripts de módulos restantes contienen solo
comentarios de cabecera por ahora.

## Configuración local

Copie `src/main/resources/application-example.properties` como
`src/main/resources/application-local.properties` y configure allí el usuario,
la clave y la URL de MySQL de su entorno. Use la clave que asignó al usuario
`ecomoda_dev` en el paso anterior. Este archivo está excluido de Git.

Para iniciar con ese perfil, ejecute:

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local"
```

También puede definir `DB_URL`, `DB_USER` y `DB_PASSWORD` como variables de
entorno. La URL local predeterminada es
`jdbc:mysql://localhost:3306/ecomoda`.

Firebase es opcional. Para habilitarlo, configure `FIREBASE_ENABLED=true`,
`FIREBASE_BUCKET_NAME` y `FIREBASE_CREDENTIALS_PATH` apuntando a un JSON de
credenciales externo al proyecto. No agregue ese JSON al classpath ni al repo.

## Ejecutar

Con la base local y el perfil configurados:

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local"
```

Abra <http://localhost:8080>. Para compilar el proyecto:

```bash
mvn compile
```

## Probar el carrito (HU-5)

Sobre la base local `ecomoda`, ejecute los scripts `01_usuario.sql`,
`03_prenda.sql` y `09_carrito.sql`, en ese orden, desde
`src/main/resources/sql`. Para probar con el catálogo mínimo, agregue una
prenda y una variante de inventario. Por ejemplo, en la misma conexión:

```sql
INSERT INTO prenda (nombre, descripcion, precio)
VALUES ('Camiseta', 'Camiseta de algodón', 7500.00);

INSERT INTO inventario (id_prenda, talla, cantidad)
VALUES (LAST_INSERT_ID(), 'M', 5);
```

Inicie sesión como `CLIENTE`, abra `/catalogo/listado` y agregue la prenda.
En `/carrito/listado` puede revisar el total, cambiar la cantidad o confirmar
la eliminación de la línea. Agregar al carrito valida existencias. El
descuento de inventario al confirmar un pedido corresponde a HU-9.

## Reglas de Git del equipo

- Crear una rama por cada HU (historia de usuario).
- Escribir los mensajes de commit en español.
- Abrir un pull request obligatorio para revisar e integrar cada HU.
- No subir credenciales, claves, archivos `application-local.properties` ni
  JSON de Firebase.
