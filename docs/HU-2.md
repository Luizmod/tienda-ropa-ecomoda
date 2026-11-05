# HU-2: credenciales

## Preparación

- Ejecutar manualmente `src/main/resources/sql/01_usuario.sql` sobre MySQL local.
  La aplicación no crea tablas ni ejecuta scripts automáticamente.
- No contiene cuentas ni contraseñas de ejemplo. Registrar un CLIENTE desde
  `/registro/nuevo`. El rol ADMIN solo se asigna manualmente a una cuenta existente
  mediante administración de la base de datos.
- `/login`: acceso por correo; `/perfil`: cuenta del usuario autenticado.

## Contrato compartido con B y C

Tabla `usuario`: `id_usuario BIGINT` autoincremental, `correo VARCHAR(254)` único,
`contrasena VARCHAR(60)` (solo BCrypt), `rol VARCHAR(7)` (`CLIENTE` o `ADMIN`).
Correos normalizados con minúsculas y sin espacios en los extremos.
Las claves foráneas deben usar `BIGINT` con `ON DELETE RESTRICT`, sin cascadas.
El esquema queda propuesto para la integración; comunicar estos nombres al equipo.

Los módulos propios deben obtener el ID desde `UsuarioAutenticado.getIdUsuario()`
con `@AuthenticationPrincipal`, nunca desde un ID de usuario enviado por el cliente.
`UsuarioService.consultar(id)` devuelve solo `PerfilVista`, sin hash.

Las rutas `/categoria/**`, `/prenda/**`, `/cupon/**`, `/admin/**` requieren ADMIN.
El catálogo público de B debe usar `/catalogo/**`. El resto requiere autenticación,
salvo inicio, registro, login, errores y recursos estáticos.

## Comportamiento y seguridad

- Registro público: siempre CLIENTE, con confirmación y BCrypt; no se enlazan
  entidades a formularios. Nunca se pone el hash en el modelo o HTML.
- Modificar correo o contraseña requiere contraseña actual. La nueva es opcional
  para cambiar solo el correo. Tras guardar se cierra la sesión actual.
- Borrado real por POST con CSRF, precedido de Confirmar/Cancelar. Si una FK impide
  borrar, se captura `DataIntegrityViolationException` fuera de la transacción y
  se conserva la cuenta. GET solo muestra la confirmación.
- Formularios con errores redirigen sin conservar contraseñas en atributos flash.
- Pestañas de direcciones, métodos y consultas apuntan a los módulos de C, aún pendientes.
- No hay recuperación por correo, pedidos, pagos ni selección libre de rol.

## Comprobación manual (base local)

1. Registrar cuenta, intentar correo duplicado y confirmación distinta.
2. Entrar con contraseña errónea y correcta; salir mediante POST.
3. Visitar `/perfil` sin sesión; debe redirigir a login.
4. Intentar acceder a cupones como CLIENTE; debe responder 403.
5. Modificar con contraseña actual errónea; comprobar que no cambia nada.
6. Cambiar solo correo y luego contraseña; comprobar nuevo acceso y rechazo del anterior.
7. Cancelar borrado; comprobar permanencia. Confirmarlo; comprobar que no permite login.
8. Con una FK restrictiva de C, intentar borrar; debe conservar cuenta y asociaciones.
9. Inspeccionar HTML: nunca debe incluir hashes ni contraseñas enviadas anteriormente.
