# Guía de Aprendizaje: Cómo funciona Factugest
### Java + Spring Boot para principiantes absolutos

---

## ¿Qué es este software?

Factugest es un sistema de facturación electrónica para Colombia. Te permite:
- Crear facturas de venta (FV), notas débito (ND) y notas crédito (NC)
- Gestionar clientes, productos, empresas y usuarios
- Generar PDFs de facturas
- Ver estadísticas del negocio en un dashboard

---

## 1. El lenguaje: Java

Java es un lenguaje orientado a objetos. Todo en Java vive dentro de una **clase**. Una clase es como un molde o plantilla que describe cómo es un objeto.

```java
// Esto es una clase. Describe cómo es un Producto.
public class Producto {
    private String nombre;      // atributo: cómo se llama el producto
    private BigDecimal precio;  // atributo: cuánto cuesta
}
```

Un **objeto** es una instancia de esa clase — la cosa real creada a partir del molde:

```java
// Aquí creamos un objeto real a partir del molde Producto
Producto p = new Producto();
p.setNombre("Leche");
p.setPrecio(new BigDecimal("3500.00"));
```

---

## 2. El framework: Spring Boot

Spring Boot es una plataforma que hace todo el trabajo pesado por ti:
- Levanta un servidor web (Tomcat) automáticamente
- Conecta la aplicación a la base de datos
- Maneja la seguridad (login, sesiones, permisos)
- Genera el HTML y lo manda al navegador

Sin Spring Boot, tendrías que escribir miles de líneas de código para hacer todo eso manualmente.

---

## 3. La arquitectura: capas separadas

El software está organizado en capas. Cada capa tiene una responsabilidad específica:

```
Navegador (Chrome/Firefox)
        ↕ HTTP
   CONTROLADOR  ←→  recibe peticiones, llama al servicio, devuelve la vista
        ↕
   SERVICIO      ←→  lógica de negocio, cálculos, reglas del sistema
        ↕
REPOSITORIO/JDBC ←→  habla con la base de datos
        ↕
  POSTGRESQL     ←→  donde viven los datos realmente
```

Esta separación hace que el código sea más fácil de mantener. Si cambias la base de datos de PostgreSQL a MySQL, solo tocas la capa de acceso a datos. Los controladores y servicios no necesitan cambiar.

---

## 4. Las anotaciones: el lenguaje de Spring

Las anotaciones son instrucciones para Spring. Empiezan con `@`. Son como etiquetas que le dicen a Spring qué hacer con cada clase o método.

```java
@Controller          // "Esta clase maneja peticiones HTTP del navegador"
@Service             // "Esta clase contiene lógica de negocio"
@Repository          // "Esta clase accede a la base de datos"
@Entity              // "Esta clase mapea a una tabla de la BD"
@Autowired           // "Spring, inyecta aquí el objeto que necesito"
```

---

## 5. Entidades JPA: el puente entre Java y la base de datos

JPA (Java Persistence API) es la tecnología que permite guardar objetos Java directamente en la base de datos sin escribir SQL manualmente.

Mira la entidad `Producto.java`:

```java
@Entity              // Le dice a Hibernate: "esta clase = una tabla en BD"
@Data                // Lombok genera automáticamente getters y setters
@Table(name = "productos")  // nombre exacto de la tabla en PostgreSQL
public class Producto {

    @Id                                          // esta es la clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // auto-incremento (SERIAL en postgres)
    @Column(name = "cod_producto")               // nombre de la columna en BD
    private Integer codProducto;

    private String nombre;                       // columna "nombre" en BD

    @Column(name = "precio_unitario")            // columna "precio_unitario" en BD
    private BigDecimal precioUnitario;           // BigDecimal = dinero exacto (no float)
}
```

**¿Por qué BigDecimal en vez de double?**
Prueba esto en cualquier calculadora de programación: `0.1 + 0.2 = 0.30000000000000004`. Los números float tienen errores de precisión en binario. BigDecimal hace cuentas exactas, crítico para facturación.

**¿Qué hace @Data de Lombok?**
Sin Lombok tendrías que escribir esto a mano para cada campo:
```java
public String getNombre() { return nombre; }
public void setNombre(String nombre) { this.nombre = nombre; }
// ... repetir para cada campo
```
Lombok lo genera automáticamente al compilar.

---

## 6. Repositorios: el CRUD gratis

Spring Data JPA te da operaciones de base de datos gratis con solo definir una interfaz:

```java
public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    // JpaRepository<Entidad, TipoDeClavePrimaria>
    // Solo con esto ya tienes:
    //   repo.findAll()         → SELECT * FROM productos
    //   repo.findById(5)       → SELECT * FROM productos WHERE cod_producto = 5
    //   repo.save(producto)    → INSERT o UPDATE automáticamente
    //   repo.deleteById(5)     → DELETE FROM productos WHERE cod_producto = 5

    // Puedes agregar métodos personalizados con nombres descriptivos:
    List<Producto> findAllByOrderByNombreAsc();
    // Spring genera: SELECT * FROM productos ORDER BY nombre ASC
}
```

Spring lee el nombre del método y construye el SQL. Si el método se llama `findByNombreContaining(String texto)`, Spring genera `WHERE nombre LIKE %texto%` automáticamente.

---

## 7. Controladores: el recepcionista de peticiones

Un controlador recibe las peticiones HTTP del navegador y decide qué hacer:

```java
@Controller
@RequestMapping("/customer")   // todas las rutas de este controlador empiezan con /customer
public class CustomerController {

    @GetMapping            // GET /customer → mostrar lista
    public String list(Model model) {
        // Model es el "maletín de datos" que enviamos a la vista
        model.addAttribute("all_customers", customerService.getAll());
        return "customer/index";  // nombre de la plantilla Thymeleaf a renderizar
    }

    @PostMapping("/new")   // POST /customer/new → guardar nuevo cliente
    public String create(@RequestParam String full_name, ...) {
        // @RequestParam extrae el valor del campo "full_name" del formulario HTML
        Customer c = new Customer();
        c.setFullName(full_name);
        customerService.save(c);
        return "redirect:/customer";  // evita doble envío al recargar la página
    }
}
```

**GET vs POST:**
- **GET**: el navegador pide ver algo (listar clientes, ver un formulario vacío)
- **POST**: el navegador envía datos del formulario para guardar

**¿Por qué `redirect:/customer` en lugar de return "customer/index"?**
Si devuelves directamente la vista después de un POST y el usuario recarga la página, el navegador reenvía el formulario y crea un duplicado. Con `redirect:`, el navegador hace un GET nuevo, así la recarga solo muestra la lista. Esto se llama patrón **Post-Redirect-Get**.

---

## 8. Servicios: donde vive la lógica del negocio

Los servicios contienen las reglas del negocio — lo que tu sistema "sabe hacer" más allá de guardar y leer datos.

```java
@Service
public class InvoiceService {

    @Autowired
    private JdbcTemplate jdbc;  // herramienta para ejecutar SQL directamente

    // Este servicio calcula totales, junta datos de 5 tablas,
    // y aplica las reglas de la DIAN para facturación colombiana.

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // queryForObject ejecuta un SELECT que devuelve UN SOLO valor
        stats.put("total_facturas",
            jdbc.queryForObject("SELECT COUNT(*) FROM facturas", Long.class));

        // queryForList ejecuta un SELECT que devuelve MÚLTIPLES filas
        stats.put("facturas_recientes",
            jdbc.queryForList("SELECT ... FROM facturas ORDER BY fecha DESC LIMIT 8"));

        return stats;
    }
}
```

**¿Por qué InvoiceService usa JdbcTemplate y no JPA?**

Las consultas de facturas unen 5-6 tablas y devuelven campos mixtos de varias entidades. JPA está pensado para trabajar con entidades limpias de una tabla. Para queries complejas, JdbcTemplate da más control:

```java
// JPA: sencillo para una tabla
List<Producto> productos = repo.findAll();

// JdbcTemplate: poderoso para queries complejas con múltiples tablas
List<Map<String, Object>> facturas = jdbc.queryForList("""
    SELECT f.cod_factura, c.full_name, e.nombre, pf.status
    FROM facturas f
        LEFT JOIN customers c ON f.cod_cliente = c.customer_id
        LEFT JOIN empresas e ON f.cod_empresa = e.cod_empresa
        LEFT JOIN pagos_factura pf ON f.cod_pago = pf.cod_pago_factura
    ORDER BY f.fecha DESC
""");
```

---

## 9. Inyección de dependencias: @Autowired

Este es uno de los conceptos más importantes de Spring. En lugar de crear objetos manualmente (`new InvoiceService()`), le pides a Spring que te los entregue:

```java
@Controller
public class InvoiceController {

    // "Spring, dame el InvoiceService que ya creaste"
    @Autowired
    private InvoiceService invoiceService;

    // Ahora puedo usar invoiceService sin haberlo creado yo mismo
}
```

**¿Por qué es útil?**
Imagina que `InvoiceService` necesita `JdbcTemplate`, que necesita `DataSource`, que necesita las credenciales de la BD... Si tuvieras que crear todo eso manualmente en cada controlador, sería un caos. Spring gestiona todas esas dependencias automáticamente — eso se llama **contenedor de inversión de control (IoC)**.

---

## 10. Thymeleaf: plantillas HTML dinámicas

Thymeleaf es el motor de plantillas. Permite mezclar HTML con datos de Java:

```html
<!-- th:each = un bucle forEach en HTML -->
<tr th:each="c : ${all_customers}">
    <!-- th:text = escribe el texto del elemento con el valor de Java -->
    <td th:text="${c.fullName}"></td>
    <td th:text="${c.documentNumber}"></td>
</tr>

<!-- th:if = mostrar/ocultar según una condición -->
<div th:if="${param.error}">
    Contraseña incorrecta
</div>

<!-- th:action = la URL del formulario procesada por Spring -->
<form th:action="@{/customer/new}" method="post">
    <!-- @{} procesa la URL y agrega automáticamente el token CSRF de seguridad -->
</form>
```

**¿Cómo llegan los datos de Java al HTML?**
El controlador pone datos en el `Model`:
```java
model.addAttribute("all_customers", customerService.getAll());
// La clave "all_customers" queda disponible en el HTML como ${all_customers}
```

---

## 11. Spring Security: el sistema de seguridad

Spring Security intercepta CADA petición HTTP antes de que llegue al controlador.

```
Navegador → [Filtro de Seguridad] → ¿Está logueado? ¿Tiene permiso? → Controlador
```

La configuración en `SecurityConfig.java`:

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/login", "/css/**").permitAll()   // estas rutas son públicas
    .requestMatchers("/users/**").hasRole("ADMIN")       // solo para ADMIN
    .anyRequest().authenticated()                        // todo lo demás: requiere login
)
```

**El flujo de login:**

1. Usuario escribe correo y contraseña en `/login`
2. Spring Security llama a `CustomUserDetailsService.loadUserByUsername(correo)`
3. `CustomUserDetailsService` busca el usuario en BD por correo
4. Spring Security compara la contraseña enviada contra el hash BCrypt en BD
5. Si coincide → crea la sesión y redirige a `/`
6. Si no coincide → redirige a `/login?error=true`

**¿Qué es BCrypt?**
BCrypt es un algoritmo de hashing para contraseñas. "Hashear" significa transformar la contraseña en una cadena irreversible:
- `"mi_clave"` → `"$2a$10$xyz...abc"` (60 caracteres)
- No se puede "deshacer" (irreversible por diseño)
- Dos hashes de la misma contraseña son diferentes (usa "salt" aleatorio)
- Por eso en BD nunca se guarda "mi_clave", sino su hash

---

## 12. Transacciones: todo o nada

Cuando creamos una factura, hacemos varios INSERT:
1. INSERT en `facturas` (la cabecera)
2. INSERT en `detalle_factura` (una fila por producto)
3. INSERT en `factura_descuento` (si hay descuento de factura)

¿Qué pasa si el paso 2 falla? Sin transacción, quedaía una factura en BD sin líneas de detalle — datos inconsistentes. Con `@Transactional`, si cualquier paso falla, **todos los cambios se revierten** y la BD queda como si nada hubiera ocurrido:

```java
@Transactional
public Integer createInvoice(...) {
    // Si cualquier línea aquí lanza una excepción,
    // el INSERT de arriba también se revierte automáticamente
    jdbc.update("INSERT INTO facturas ...", ...);
    jdbc.update("INSERT INTO factura_descuento ...", ...);
    // Al salir del método sin errores → COMMIT (los cambios se guardan)
}
```

---

## 13. El flujo completo al crear una factura

Aquí está lo que ocurre desde que haces clic en "Guardar Factura" hasta que ves la factura creada:

```
1. El navegador envía POST /invoice/new con todos los datos del formulario

2. Spring Security verifica: ¿estás autenticado? ✓

3. InvoiceController.createInvoice() recibe los parámetros:
   - @RequestParam Integer cod_cliente   ← Spring extrae "cod_cliente" del form
   - @RequestParam List<Integer> cod_producto  ← lista de todos los productos

4. JavaScript eliminó las filas vacías antes de enviar (la que se agrega automática)

5. InvoiceController recalcula los totales en el servidor:
   - Para cada producto: bruto = precio × cantidad
   - descuento_valor = bruto × (descuento_pct / 100)
   - base_gravable = bruto - descuento_valor
   - iva_valor = base_gravable × (iva_pct / 100)
   - Suma todos los IVAs → total_impuestos
   - Suma todos los descuentos → total_descuentos
   - total = base_gravable_total + total_impuestos

6. InvoiceService.createInvoice():
   - INSERT en facturas, PostgreSQL devuelve el cod_factura nuevo (RETURNING)

7. InvoiceService.createInvoiceDetail() (una vez por producto):
   - INSERT en detalle_factura con cod_factura recién creado

8. Todo envuelto en @Transactional: si falla cualquier INSERT → rollback

9. return "redirect:/invoice/42"  ← 42 es el ID de la factura creada

10. El navegador hace GET /invoice/42

11. InvoiceController.viewInvoice() carga la factura con todos sus datos

12. Thymeleaf renderiza invoice/view.html con los datos

13. El navegador muestra la factura lista
```

---

## 14. La base de datos: estructura y relaciones

### Tablas principales y cómo se relacionan:

```
usuarios (cod_usuario, nombre, correo, contrasena, rol)
    ↑ cod_usuario
facturas (cod_factura, fecha, cod_cliente, cod_usuario, cod_empresa, ...)
    ↑ cod_factura
detalle_factura (cod_destalle, cod_factura, cod_producto, cantidad, precio_unitario, ...)

customers (customer_id, full_name, document_type, document_number, ...)
empresas (cod_empresa, nombre, nit, dv, ...)
productos (cod_producto, sku, nombre, precio_unitario, cod_impuesto, activo, ...)
    ↑ cod_impuesto
impuestos (cod_impuesto, porcentaje, descripcion, codigo_dian)
metodos_pago (cod_pago, descripcion, nombre)
pagos_factura (cod_pago_factura, status)  ← "pagada", "pendiente", "vencida"
descuentos (cod_descuento, descripcion, porcentaje, aplica_a_producto, aplica_a_factura)
producto_descuento (cod_producto, cod_descuento)  ← tabla puente M:N
factura_descuento (cod_factura, cod_descuento, valor_descuento)
```

### ¿Qué es una clave foránea (FK)?
Es un campo en una tabla que apunta a la clave primaria de otra tabla. En `facturas`:
- `cod_cliente` apunta a `customers.customer_id`
- `cod_empresa` apunta a `empresas.cod_empresa`

Si intentas crear una factura con `cod_cliente=999` y no existe ningún cliente con ese ID, PostgreSQL rechaza el INSERT. Esto garantiza integridad de datos.

### ¿Qué es un LEFT JOIN?
Cuando consultamos facturas necesitamos el nombre del cliente, de la empresa, etc. Todas esas tablas están separadas. Para traerlas juntas usamos JOIN:

```sql
-- INNER JOIN: solo facturas que TIENEN cliente registrado
SELECT f.cod_factura, c.full_name
FROM facturas f INNER JOIN customers c ON f.cod_cliente = c.customer_id

-- LEFT JOIN: TODAS las facturas, aunque no tengan cliente (el nombre sería NULL)
SELECT f.cod_factura, c.full_name
FROM facturas f LEFT JOIN customers c ON f.cod_cliente = c.customer_id
```

Usamos LEFT JOIN porque si algún cliente fue eliminado, la factura histórica sigue siendo válida.

### ¿Qué es SERIAL en PostgreSQL?
SERIAL es un tipo especial que auto-incrementa el número:
```sql
cod_factura SERIAL PRIMARY KEY
-- Al insertar: si la última factura fue la 41, la siguiente es automáticamente 42
-- RETURNING cod_factura devuelve ese 42 para que Java lo use
```

---

## 15. JavaScript en el formulario de facturas

El formulario de facturas usa JavaScript para crear una experiencia fluida sin recargar la página.

### Búsqueda de productos en tiempo real (fetch API)

```javascript
// Cuando el usuario escribe en el campo de producto...
searchInput.addEventListener('input', function() {
    clearTimeout(timer);
    // Esperar 300ms después de que deje de escribir (evita consultas en cada tecla)
    timer = setTimeout(() => fetchProductos(this.value, row, dropdown), 300);
});

async function fetchProductos(q, row, dropdown) {
    // fetch hace una petición HTTP al servidor sin recargar la página
    const res  = await fetch('/invoice/api/products/search?q=' + encodeURIComponent(q));
    const data = await res.json();  // convierte el JSON de respuesta a array de objetos

    // Construye los botones del dropdown con los resultados
    data.forEach(p => {
        const btn = document.createElement('button');
        btn.textContent = p.nombre;
        btn.addEventListener('mousedown', () => selectProducto(p, row, dropdown));
        dropdown.appendChild(btn);
    });
}
```

**¿Qué es async/await?**
Las peticiones a internet son lentas. `async/await` permite esperar la respuesta sin "congelar" el navegador. Mientras espera, el usuario puede seguir usando la página.

### Cálculo automático de totales

```javascript
function calcRow(el) {
    const row   = el.closest('tr');     // encuentra la fila del producto
    const precio = parseFloat(row.querySelector('.price-input').value) || 0;
    const cant   = parseFloat(row.querySelector('.qty-input').value)   || 0;
    const desc   = parseFloat(row.querySelector('.desc-select').value) || 0;
    const tax    = parseFloat(row.dataset.tax || 0);  // IVA guardado en data-tax

    const bruto   = precio * cant;
    const descVal = bruto * desc / 100;
    const base    = bruto - descVal;    // base gravable DIAN
    const iva     = base * tax / 100;   // IVA calculado sobre la base

    // Actualiza los campos de pantalla
    row.querySelector('.base-display').value = '$ ' + base.toFixed(2);
    row.querySelector('.tax-display').value  = '$ ' + iva.toFixed(2);

    // Guarda en data-attributes para que calcTotales() los sume
    row.dataset.base = base;
    row.dataset.iva  = iva;

    calcTotales();  // recalcula el resumen total
}
```

### Por qué se elimina la fila vacía al guardar

Cuando seleccionas un producto, el código agrega automáticamente una fila vacía nueva para el siguiente producto. Al enviar el formulario, esa última fila vacía no tiene producto. El validador la elimina así:

```javascript
document.getElementById('invoiceForm').addEventListener('submit', function(e) {
    // Recorre TODAS las filas y elimina las que no tienen producto seleccionado
    document.querySelectorAll('.product-row').forEach(row => {
        if (!row.querySelector('.cod-producto').value) {
            row.remove();  // quita el elemento del DOM → no se envía al servidor
        }
    });

    // Si no queda ninguna fila → error
    if (document.querySelectorAll('.product-row').length === 0) {
        e.preventDefault();  // cancela el envío del formulario
        alert('Debe agregar al menos un producto.');
        addRow();  // agrega una fila vacía para que el usuario pueda empezar
    }
    // Si hay filas válidas → el form se envía normalmente al servidor
});
```

---

## 16. Ciclo de vida completo de la aplicación

```
1. main() en FactugestApplication.java
2. SpringApplication.run() arranca:
   a. Crea el contenedor de Spring (ApplicationContext)
   b. Escanea com.factugest buscando @Component, @Service, @Controller, etc.
   c. Crea todos los beans (objetos gestionados por Spring)
   d. Inyecta dependencias (@Autowired)
   e. Verifica el esquema de BD (ddl-auto=validate en application.properties)
   f. Ejecuta PasswordMigrationRunner (ApplicationRunner)
   g. Levanta Tomcat en el puerto 8080
3. La app está lista para recibir peticiones HTTP
```

---

## 17. Conceptos de Java usados en el código

### Optional<T>
Cuando buscas algo en la BD que podría no existir, en vez de devolver null (que causa NullPointerException), se devuelve Optional:

```java
Optional<Producto> opt = productoService.getById(999);

// Verificar si encontró algo:
if (opt.isEmpty()) return "redirect:/products/product";

// Si existe, obtenerlo:
Producto p = opt.get();
```

Optional hace explícito en el código que "este resultado podría no existir", obligando a manejarlo.

### Pattern Matching (instanceof)
Java moderno (21) permite verificar y convertir el tipo al mismo tiempo:

```java
// Forma vieja:
if (tp instanceof BigDecimal) {
    BigDecimal bd = (BigDecimal) tp;
    taxPct = bd;
}

// Java 16+: pattern matching, más conciso
if (tp instanceof BigDecimal bd) taxPct = bd;
// Si tp es BigDecimal → lo convierte a bd automáticamente
```

### Text Blocks (""")
Java 13+ permite strings multilínea legibles:

```java
// Sin text block: difícil de leer
String sql = "SELECT f.cod_factura, c.full_name\n" +
             "FROM facturas f\n" +
             "LEFT JOIN customers c ON f.cod_cliente = c.customer_id\n";

// Con text block: se ve exactamente como lo escribirías en un editor de SQL
String sql = """
    SELECT f.cod_factura, c.full_name
    FROM facturas f
    LEFT JOIN customers c ON f.cod_cliente = c.customer_id
    """;
```

### List, Map, HashMap
Las colecciones más usadas en el código:

```java
// List: colección ordenada de elementos (como un array flexible)
List<Integer> codProductos = List.of(1, 5, 12);
codProductos.forEach(id -> System.out.println(id));

// Map: colección de pares clave-valor (como un diccionario)
Map<String, Object> factura = new HashMap<>();
factura.put("cod_factura", 42);
factura.put("total", new BigDecimal("150000"));
String nombre = (String) factura.get("cod_factura");  // obtener por clave
```

### RoundingMode.HALF_UP
Cuando divides números en Java puede haber fracciones infinitas. Para truncar a 2 decimales:

```java
// HALF_UP = redondeo estándar: 2.345 → 2.35 (no 2.34)
BigDecimal descVal = bruto.multiply(descPct)
    .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
//                                ↑ 2 decimales, redondeo HALF_UP
```

---

## 18. Preguntas frecuentes de principiantes

**¿Por qué hay dos formas de acceder a la BD (JPA y JdbcTemplate)?**

JPA es conveniente para operaciones simples (CRUD de una tabla). JdbcTemplate es necesario para queries complejas con múltiples JOINs que devuelven datos mixtos. En este sistema, se usa JPA para los catálogos (productos, clientes, usuarios) y JdbcTemplate para las consultas de facturas.

**¿Por qué el controlador no hace la lógica directamente?**

Separación de responsabilidades. Si pones lógica de negocio en el controlador, y luego quieres también una API REST, un job programado o una importación en batch que haga lo mismo, tendrías que duplicar el código. En el servicio, la lógica está en un solo lugar y cualquiera la puede usar.

**¿Qué pasa si dos usuarios crean facturas al mismo tiempo?**

PostgreSQL maneja la concurrencia con SERIAL: aunque 100 usuarios inserten simultáneamente, cada uno recibe un ID único. Las transacciones (@Transactional) garantizan que si dos cambios chocan, uno espera al otro.

**¿Por qué redirect: en vez de devolver la vista directamente?**

Si devuelves la vista directamente después de un POST y el usuario recarga la página, el navegador pregunta "¿reenviar los datos del formulario?". Si dice sí, crea una factura duplicada. Con `redirect:`, el navegador hace un GET nuevo que no tiene ese problema.

**¿Cómo sabe Spring qué método del controlador llamar?**

Por la combinación de URL + método HTTP:
- `GET /invoice` → `listInvoices()`
- `GET /invoice/new` → `newInvoiceForm()`
- `POST /invoice/new` → `createInvoice()`
- `GET /invoice/42` → `viewInvoice(id=42)`

Las anotaciones `@GetMapping`, `@PostMapping` y `@PathVariable` hacen ese mapeo.

---

*Documento generado para el proyecto Factugest — Sistema de Facturación Electrónica Colombia*
*Stack: Java 21 · Spring Boot 3.3.5 · Thymeleaf · PostgreSQL · Spring Security*
