\# Proyecto: Ecomoda, tienda de ropa (curso Aplicaciones Web y Patrones, Fidélitas)



\## Stack

Spring Boot 4.1.x, Thymeleaf, Spring Data JPA, MySQL, Spring Security,

Lombok, Bootstrap 5 (WebJars), Firebase Storage para imágenes.



\## Arquitectura (paquete base: com.tienda)

Capas: controller -> service -> repository -> domain. Inyección por constructor.

Plantillas por módulo en templates/<modulo>/ con listado.html, modifica.html

y fragmentos.html. Mensajes al usuario con RedirectAttributes ("todoOk"/"error").



\## Convenciones

\- Nombres de clases, tablas y campos en español (Prenda, id\_prenda).

\- Todo texto visible va en messages\*.properties con prefijo por módulo

&#x20; (categoria.\*, prenda.\*, cupon.\*, ...). Nunca texto fijo en las plantillas.

\- Comentarios didácticos y cortos en el código, en español.

\- Borrado real con confirmación; si hay datos asociados, capturar

&#x20; DataIntegrityViolationException y mostrar mensaje de error.

\- Los datos del cliente se filtran por el usuario autenticado, nunca por

&#x20; un id recibido en la URL.

\- Tarjetas: solo últimos 4 dígitos. Contraseñas: solo hash BCrypt.



\## Reglas de seguridad del repo

\- NUNCA escribir credenciales, claves ni JSON de Firebase en archivos versionados.

\- Config sensible por variables de entorno o application-local.properties (ignorado).

\- No hacer git commit ni git push: los hace el humano.



\## Archivos con riesgo de conflicto (editar solo lo mínimo)

templates/general/fragmentos.html, messages\*.properties, ProjectConfig.java

