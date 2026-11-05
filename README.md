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
`ecomoda_dev` y sus permisos. Ejecute también `01_usuario.sql` para habilitar
el registro y el inicio de sesión, y `07_cupon.sql` para habilitar los cupones.
Ambos están en `src/main/resources/sql` y se ejecutan manualmente.

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

Para probar HU-12, entre con una cuenta de rol `ADMIN` y abra
`/cupon/listado`. Puede crear, modificar y eliminar cupones desde esa pantalla.

## Reglas de Git del equipo

- Crear una rama por cada HU (historia de usuario).
- Escribir los mensajes de commit en español.
- Abrir un pull request obligatorio para revisar e integrar cada HU.
- No subir credenciales, claves, archivos `application-local.properties` ni
  JSON de Firebase.
