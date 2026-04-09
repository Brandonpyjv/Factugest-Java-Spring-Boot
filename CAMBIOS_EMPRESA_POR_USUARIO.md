# Cambios: Empresa emisora preseleccionada en nueva factura

**Fecha:** 2026-04-07  
**Rama:** wilmer_pruebas  
**Objetivo:** Al crear una nueva factura, la empresa emisora ya aparece seleccionada automáticamente según el usuario que inició sesión. No se muestra un dropdown para elegir empresa.

---

## Problema que se resolvía

Antes, el formulario de nueva factura tenía un `<select>` donde el usuario podía elegir cualquier empresa registrada. Esto era incorrecto en un sistema multiusuario donde cada empleado pertenece a una sucursal específica.

---

## Solución implementada

Se optó por **agregar una columna `cod_empresa` a la tabla `usuarios`** (en lugar de una tabla intermedia), ya que la relación es simple: **cada usuario pertenece a exactamente una empresa/sucursal**. Una tabla intermedia sería sobrediseño para este caso.

No se creó una tabla nueva — la solución es puramente por consulta a la columna nueva.

---

## Cambios en base de datos

### Archivo: `migration_usuario_empresa.sql`

```sql
ALTER TABLE usuarios
    ADD COLUMN cod_empresa INTEGER REFERENCES empresas(cod_empresa);
```

- La columna es **nullable**: los usuarios ADMIN pueden existir sin empresa asignada.
- Después de ejecutar la migración, asigna manualmente `cod_empresa` a cada usuario existente.

---

## Cambios en código

### 1. `entity/Usuario.java`
- Se agregó el campo `private Integer codEmpresa` con `@Column(name = "cod_empresa")`.

### 2. `security/CustomUserPrincipal.java`
- Se agregaron los campos `codUsuario` y `codEmpresa` al principal de sesión.
- Ahora el controlador puede leer el ID del usuario y su empresa directamente desde la sesión, **sin hacer una consulta extra a la BD en cada request**.

### 3. `security/CustomUserDetailsService.java`
- Al construir el `CustomUserPrincipal` durante el login, ahora se pasan también `codUsuario` y `codEmpresa` tomados del registro del usuario en BD.

### 4. `controller/InvoiceController.java`
- **GET `/invoice/new`**: recibe `@AuthenticationPrincipal`, lee `codEmpresa` del principal, busca el objeto `Empresa` y lo pasa al modelo como `empresaUsuario`. Si el usuario no tiene empresa asignada, redirige a `/invoice?error=sin_empresa`.
- **POST `/invoice/new`**: ya no recibe `cod_empresa` ni `cod_usuario` como parámetros del formulario. Ambos valores se leen del principal autenticado — **esto previene manipulación por parte del cliente**.

### 5. `templates/invoice/form.html`
- Se reemplazó el `<select name="cod_empresa">` por un campo visual de solo lectura (muestra nombre y NIT de la empresa) más un `<input type="hidden" name="cod_empresa">` con el valor preestablecido.
- El usuario ya no puede cambiar la empresa desde el formulario.

### 6. `controller/UserController.java`
- Los métodos `newForm` y `editForm` ahora pasan la lista de empresas al modelo.
- Los métodos `create` y `update` aceptan el parámetro `cod_empresa` y lo persisten en el usuario.

### 7. `templates/users/form.html`
- Se agregó un `<select name="cod_empresa">` al formulario de crear/editar usuario, para que el administrador pueda asignar la empresa/sucursal correspondiente.

---

## Flujo completo después del cambio

```
1. Admin crea usuario → asigna "Empresa A" → guarda
2. Usuario inicia sesión → CustomUserDetailsService carga codEmpresa desde BD → lo almacena en el principal de sesión
3. Usuario va a /invoice/new → InvoiceController lee codEmpresa del principal → muestra empresa fija
4. Usuario crea factura → el controlador toma cod_empresa del principal (no del form) → guarda
```

---

## Notas para el equipo

- **Ejecutar la migración SQL antes de arrancar la aplicación** con los cambios. Hibernate tiene `ddl-auto=validate` y fallará si la columna no existe en BD.
- Usuarios ADMIN con `cod_empresa = NULL` no podrán crear facturas desde `/invoice/new` (serán redirigidos). Esto es intencional — los admins gestionan el sistema pero las facturas las crean los cajeros/vendedores asignados a una sucursal.
- Si se necesita que un ADMIN también cree facturas, simplemente asignarle una empresa desde la vista de edición de usuarios.
