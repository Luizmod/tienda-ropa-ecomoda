# Ecomoda

Base de una tienda de ropa desarrollada con Spring Boot 4.1.x, Java 21,
Thymeleaf, Spring Data JPA, MySQL y Spring Security. Firebase Storage es
opcional y permanece desactivado por defecto.

## Requisitos

- JDK 21.
- Apache Maven 3.9 o posterior.
- MySQL 8.0.16 o posterior, iniciado localmente.
- Git.

## Crear la base de datos local

En MySQL Workbench, conéctese con una cuenta administradora, abra
`src/main/resources/sql/crear_tablas.sql` y ejecute todo el archivo. Crea
`ecomoda` con las 17 tablas de las 15 historias de usuario, incluidas las
de los módulos que aún están pendientes en la aplicación. Se puede volver a
ejecutar sobre el esquema del avance 2 y conserva los datos existentes.

Pedidos y seguimiento usan los mismos estados: `PENDIENTE`, `PROCESANDO`,
`ENVIADO`, `ENTREGADO` y `CANCELADO`. La categoría se puede asignar a las
prendas que ya estaban en el catálogo.

Si necesita una cuenta para la aplicación, créela aparte en **Users and
Privileges** de Workbench. Conceda permisos `SELECT`, `INSERT`, `UPDATE` y
`DELETE` sobre `ecomoda`.

## Configuración local

Copie `src/main/resources/application-example.properties` como
`src/main/resources/application-local.properties` y configure allí el usuario,
la clave y la URL de MySQL de su entorno. Este archivo está excluido de Git.

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

Después de crear las tablas, agregue una prenda y una variante de inventario.
Por ejemplo, en la misma conexión:

```sql
USE ecomoda;

INSERT INTO prenda (nombre, descripcion, precio)
VALUES ('Camiseta', 'Camiseta de algodón', 7500.00);

INSERT INTO inventario (id_prenda, talla, cantidad)
VALUES (LAST_INSERT_ID(), 'M', 5);
```

Inicie sesión como `CLIENTE`, abra `/catalogo/listado` y agregue la prenda.
En `/carrito/listado` puede revisar el total, cambiar la cantidad o confirmar
la eliminación de la línea. Agregar al carrito valida existencias. El
descuento de inventario al confirmar un pedido corresponde a HU-9.

## Probar cupones (HU-12)

Inicie sesión con una cuenta de rol `ADMIN` y abra `/cupon/listado`.
Puede crear, modificar y eliminar cupones desde esa pantalla.

## Reglas de Git del equipo

- Crear una rama por cada HU (historia de usuario).
- Escribir los mensajes de commit en español.
- Abrir un pull request obligatorio para revisar e integrar cada HU.
- No subir credenciales, claves, archivos `application-local.properties` ni
  JSON de Firebase.
