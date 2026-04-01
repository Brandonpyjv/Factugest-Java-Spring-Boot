# Curso 03 — Spring Boot y Spring Security
### Para el equipo de Factugest | Arquitectura, anotaciones, controladores y seguridad

---

## Introducción

Spring Boot es el framework sobre el que está construido Factugest. Se encarga de recibir las peticiones HTTP del navegador, procesarlas con lógica de negocio y devolver respuestas (páginas HTML o datos JSON). Spring Security controla quién puede acceder a qué.

En este curso aprenderás la arquitectura completa del proyecto, cómo funcionan los controladores (con Thymeleaf y con API REST), y cómo está implementada la seguridad.

> **Prerequisito:** Haber completado los Cursos 01 y 02.

---

## Índice

1. ¿Qué es Spring Boot y por qué existe?
2. Estructura del proyecto Factugest
3. Anotaciones esenciales de Spring
4. La arquitectura MVC en Factugest
5. `@Controller` — Para vistas con Thymeleaf
6. `@RestController` — Para APIs JSON
7. Controller vs RestController — Diferencias y cuándo usar cada uno
8. Conexión con el Frontend (Thymeleaf + Fetch/AJAX)
9. Spring Data JPA — La capa de datos
10. Spring Security — Autenticación y autorización
11. Flujo completo de una petición en Factugest
12. Ejercicios de Cierre

---

## Sección 1 — ¿Qué es Spring Boot y por qué existe?

### El problema antes de Spring Boot

Para crear una aplicación web en Java puro necesitabas:
1. Configurar un servidor (Tomcat, JBoss)
2. Escribir archivos XML de configuración enormes
3. Manejar manualmente las conexiones a base de datos
4. Configurar la seguridad desde cero
5. Gestionar dependencias a mano

### La solución: Spring Boot

Spring Boot es un framework que:
- **Autoconfiguración**: detecta las dependencias que tienes y las configura automáticamente
- **Servidor embebido**: Tomcat viene incluido, no necesitas instalarlo aparte
- **Convención sobre configuración**: hace "lo más sensato" por defecto; tú solo configuras lo que cambia
- **Starters**: paquetes de dependencias preconfiguradas (ej: `spring-boot-starter-web` te da todo para hacer una web)

### ¿Cómo arranca Factugest?

```java
// FactugestApplication.java
@SpringBootApplication  // ← Esta anotación lo hace todo
public class FactugestApplication {
    public static void main(String[] args) {
        SpringApplication.run(FactugestApplication.class, args);
        // Spring Boot:
        // 1. Escanea todos los paquetes en busca de clases anotadas
        // 2. Configura la conexión a PostgreSQL (desde application.properties)
        // 3. Inicia Tomcat en el puerto 8080
        // 4. Registra todos los endpoints (URLs)
        // 5. Configura Spring Security
    }
}
```

`@SpringBootApplication` es en realidad 3 anotaciones en una:

| Anotación interna | Qué hace |
|-------------------|---------|
| `@SpringBootConfiguration` | Marca la clase como configuración de Spring |
| `@EnableAutoConfiguration` | Activa la autoconfiguración según las dependencias |
| `@ComponentScan` | Escanea el paquete y subpaquetes buscando componentes |

---

## Sección 2 — Estructura del proyecto Factugest

```
src/main/java/com/factugest/
├── FactugestApplication.java    ← Punto de entrada (main)
│
├── controller/                  ← Capa de presentación (recibe HTTP, devuelve respuesta)
│   ├── HomeController.java
│   ├── InvoiceController.java
│   ├── CustomerController.java
│   └── ... (13 controllers)
│
├── service/                     ← Capa de negocio (lógica, reglas, cálculos)
│   ├── InvoiceService.java
│   ├── CustomerService.java
│   └── ... (12 services)
│
├── repository/                  ← Capa de datos (acceso a base de datos)
│   ├── CustomerRepository.java
│   ├── ProductoRepository.java
│   └── ... (11 repositories)
│
├── entity/                      ← Modelo de datos (mapeo a tablas SQL)
│   ├── Customer.java
│   ├── Factura.java
│   └── ... (11 entities)
│
└── security/                    ← Configuración de seguridad
    ├── SecurityConfig.java
    ├── CustomUserDetailsService.java
    └── CustomUserPrincipal.java
```

### El flujo de una petición (resumen rápido)

```
Navegador
    │
    ▼ HTTP Request (GET /customer)
Controller  ← recibe la petición
    │
    ▼ llama a
Service     ← ejecuta la lógica de negocio
    │
    ▼ llama a
Repository  ← consulta la base de datos usando Entity
    │
    ▼ retorna datos
Service     ← procesa y devuelve al controller
    │
    ▼ retorna respuesta
Controller  ← prepara la vista o JSON
    │
    ▼ HTTP Response
Navegador   ← muestra el HTML o procesa el JSON
```

---

## Sección 3 — Anotaciones esenciales de Spring

Las anotaciones son metadatos que le dicen a Spring qué hace cada clase o método.

### 3.1 Anotaciones de componentes (le dicen a Spring qué "tipo" de clase es)

| Anotación | Capa | Descripción |
|-----------|------|-------------|
| `@Controller` | Presentación | Maneja peticiones HTTP, devuelve vistas HTML |
| `@RestController` | Presentación | Maneja peticiones HTTP, devuelve datos JSON/XML |
| `@Service` | Negocio | Contiene la lógica de negocio |
| `@Repository` | Datos | Accede a la base de datos |
| `@Component` | Cualquiera | Componente genérico de Spring |
| `@Configuration` | Config | Clase de configuración de Spring |

Todas estas anotaciones le indican a Spring que esas clases son **beans**: Spring los crea, los gestiona y los inyecta donde se necesiten.

### 3.2 Inyección de dependencias — `@Autowired` y constructor

En lugar de hacer `new ServicioX()`, Spring inyecta automáticamente los objetos que necesitas:

```java
// Forma 1: @Autowired en el atributo (funciona pero no recomendada)
@Controller
class CustomerController {
    @Autowired
    private CustomerService customerService;
}

// Forma 2: Constructor (recomendada — la que usa Factugest)
@Controller
class CustomerController {
    private final CustomerService customerService;

    // Spring detecta que necesitas un CustomerService y lo inyecta aquí
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }
}
```

> En Factugest, todos los controllers usan inyección por constructor (la forma 2). Spring Boot con Lombok puede usar `@RequiredArgsConstructor` para generarlo automáticamente, pero en Factugest está escrito explícitamente para claridad.

### 3.3 Anotaciones de mapeo de rutas

| Anotación | Método HTTP | Uso |
|-----------|-------------|-----|
| `@GetMapping("/ruta")` | GET | Obtener datos / mostrar página |
| `@PostMapping("/ruta")` | POST | Crear / enviar formulario |
| `@PutMapping("/ruta")` | PUT | Actualizar completamente |
| `@PatchMapping("/ruta")` | PATCH | Actualizar parcialmente |
| `@DeleteMapping("/ruta")` | DELETE | Eliminar |
| `@RequestMapping("/ruta")` | Cualquiera | Anotación base (el prefijo de la clase) |

### 3.4 Otras anotaciones importantes

```java
@PathVariable   // Extrae un valor de la URL: /customer/{id}
@RequestParam   // Extrae un parámetro de query: /search?q=carlos
@RequestBody    // Lee el cuerpo JSON de la petición (para REST)
@ModelAttribute // Vincula un formulario HTML a un objeto Java
@ResponseBody   // El retorno del método es el cuerpo de la respuesta (JSON/text)
```

---

## Sección 4 — La arquitectura MVC en Factugest

**MVC = Model - View - Controller**

| Capa | En Factugest | Tecnología |
|------|-------------|------------|
| **Model** | Entidades + Services | Java + JPA |
| **View** | Plantillas HTML | Thymeleaf |
| **Controller** | Controllers | Spring MVC |

### 4.1 El objeto `Model` — cómo pasar datos a la vista

El `Model` es un contenedor que el controller llena con datos, y la vista (Thymeleaf) los lee:

```java
@GetMapping("/customer")
public String listarClientes(Model model) {
    List<Customer> clientes = customerService.getAll();
    model.addAttribute("customers", clientes); // nombre que usará Thymeleaf
    return "customer/lista";                   // nombre del archivo HTML
}
```

En la vista `templates/customer/lista.html`:
```html
<!-- Thymeleaf lee el atributo "customers" del Model -->
<tr th:each="customer : ${customers}">
    <td th:text="${customer.fullName}">Nombre</td>
</tr>
```

---

## Sección 5 — `@Controller` — Para vistas con Thymeleaf

`@Controller` se usa cuando la respuesta es una **página HTML** generada en el servidor. Spring toma el nombre de la plantilla que retornas, la procesa con Thymeleaf y devuelve HTML al navegador.

### 5.1 Estructura básica de un Controller

```java
@Controller
@RequestMapping("/customer")  // Prefijo de todas las rutas de este controller
public class CustomerController {

    private final CustomerService customerService;

    // Constructor injection
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    // GET /customer → muestra lista de clientes
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("customers", customerService.getAll());
        return "customer/lista"; // → templates/customer/lista.html
    }

    // GET /customer/create → muestra formulario vacío
    @GetMapping("/create")
    public String formularioCrear(Model model) {
        model.addAttribute("customer", new Customer()); // Objeto vacío para el form
        return "customer/form";
    }

    // POST /customer/create → procesa el formulario
    @PostMapping("/create")
    public String crear(@ModelAttribute Customer customer) {
        customerService.save(customer); // Guarda en BD
        return "redirect:/customer";   // Redirige al listado (patrón PRG)
    }

    // GET /customer/{id} → muestra detalle de un cliente
    @GetMapping("/{id}")
    public String detalle(@PathVariable Integer id, Model model) {
        Optional<Customer> customer = customerService.getById(id);
        if (customer.isPresent()) {
            model.addAttribute("customer", customer.get());
            return "customer/detalle";
        }
        return "redirect:/customer"; // Si no existe, redirige
    }

    // POST /customer/delete/{id} → elimina un cliente
    @PostMapping("/delete/{id}")
    public String eliminar(@PathVariable Integer id) {
        customerService.delete(id);
        return "redirect:/customer";
    }
}
```

### 5.2 Patrón PRG (Post/Redirect/Get)

Este patrón **previene el reenvío del formulario** cuando el usuario recarga la página después de un POST:

```
1. Usuario llena el formulario y hace clic en "Guardar"
2. Navegador envía POST /customer/create
3. Controller guarda los datos
4. Controller retorna "redirect:/customer"   ← CLAVE
5. Navegador hace GET /customer (nueva petición)
6. Usuario ve la lista actualizada
```

Sin este patrón, si el usuario recargara la página haría otro POST y duplicaría el registro.

### 5.3 `RedirectAttributes` — mensajes flash

Para mostrar un mensaje de éxito/error después de redirigir:

```java
@PostMapping("/create")
public String crear(@ModelAttribute Customer customer,
                    RedirectAttributes redirectAttributes) {
    try {
        customerService.save(customer);
        redirectAttributes.addFlashAttribute("success", "Cliente creado correctamente");
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", "Error al crear el cliente");
    }
    return "redirect:/customer";
}
```

En el HTML con Thymeleaf:
```html
<div th:if="${success}" class="alert alert-success" th:text="${success}"></div>
<div th:if="${error}" class="alert alert-danger" th:text="${error}"></div>
```

### 5.4 Formularios con `@ModelAttribute`

`@ModelAttribute` vincula los campos de un formulario HTML a los atributos de un objeto Java automáticamente:

```html
<!-- HTML: templates/customer/form.html -->
<form th:action="@{/customer/create}" method="post" th:object="${customer}">
    <input type="text" th:field="*{fullName}" placeholder="Nombre completo">
    <input type="text" th:field="*{documentNumber}" placeholder="Número de documento">
    <select th:field="*{documentType}">
        <option value="CC">Cédula de Ciudadanía</option>
        <option value="NIT">NIT</option>
    </select>
    <button type="submit">Guardar</button>
</form>
```

```java
// Java: Spring vincula automáticamente el formulario al objeto Customer
@PostMapping("/create")
public String crear(@ModelAttribute Customer customer) {
    // customer.getFullName() ya tiene el valor que escribió el usuario
    // customer.getDocumentNumber() también
    // customer.getDocumentType() también
    customerService.save(customer);
    return "redirect:/customer";
}
```

### 5.5 Ejemplo completo: CustomerController de Factugest

```java
// src/main/java/com/factugest/controller/CustomerController.java
@Controller
@RequestMapping("/customer")
public class CustomerController {

    private final CustomerService customerService;
    private final LogService logService;

    public CustomerController(CustomerService customerService, LogService logService) {
        this.customerService = customerService;
        this.logService = logService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("customers", customerService.getAll());
        return "customer/customers";
    }

    @GetMapping("/create")
    public String formularioCrear(Model model) {
        model.addAttribute("customer", new Customer());
        return "customer/customer_form";
    }

    @PostMapping("/save")
    public String guardar(@ModelAttribute Customer customer,
                          Authentication auth) {
        customerService.save(customer);
        // Registrar en log de auditoría
        CustomUserPrincipal user = (CustomUserPrincipal) auth.getPrincipal();
        logService.createLog(user.getCodUsuario(), "CREAR CLIENTE",
                             "Cliente creado: " + customer.getFullName());
        return "redirect:/customer";
    }

    @GetMapping("/edit/{id}")
    public String formularioEditar(@PathVariable Integer id, Model model) {
        Optional<Customer> customer = customerService.getById(id);
        customer.ifPresent(c -> model.addAttribute("customer", c));
        return "customer/customer_form";
    }

    @PostMapping("/delete/{id}")
    public String eliminar(@PathVariable Integer id) {
        customerService.delete(id);
        return "redirect:/customer";
    }
}
```

---

## Sección 6 — `@RestController` — Para APIs JSON

`@RestController` se usa cuando la respuesta es **datos en formato JSON** (no HTML). Es la combinación de `@Controller` + `@ResponseBody` en todos los métodos.

Es el tipo de controller que necesitarías si el frontend fuera una aplicación separada (React, Vue, Angular, una app móvil, etc.).

### 6.1 Diferencia fundamental

```java
// @Controller — retorna el nombre de una vista (HTML)
@Controller
class MiController {
    @GetMapping("/clientes")
    public String listar(Model model) {
        model.addAttribute("lista", service.getAll());
        return "customer/lista"; // ← nombre del template HTML
    }
}

// @RestController — retorna datos serializados a JSON automáticamente
@RestController
class MiApiController {
    @GetMapping("/api/clientes")
    public List<Customer> listar() {
        return service.getAll(); // ← Spring lo convierte a JSON automáticamente
    }
}
```

### 6.2 `ResponseEntity<T>` — control total de la respuesta HTTP

Con `ResponseEntity` puedes controlar el código de estado HTTP, los headers y el cuerpo:

```java
@RestController
@RequestMapping("/api/customers")
public class CustomerApiController {

    private final CustomerService customerService;

    // GET /api/customers → lista todos los clientes
    @GetMapping
    public ResponseEntity<List<Customer>> getAll() {
        List<Customer> lista = customerService.getAll();
        return ResponseEntity.ok(lista); // 200 OK + JSON
    }

    // GET /api/customers/5 → obtiene un cliente por ID
    @GetMapping("/{id}")
    public ResponseEntity<Customer> getById(@PathVariable Integer id) {
        Optional<Customer> customer = customerService.getById(id);

        if (customer.isPresent()) {
            return ResponseEntity.ok(customer.get()); // 200 OK
        } else {
            return ResponseEntity.notFound().build(); // 404 Not Found
        }
    }

    // POST /api/customers → crea un nuevo cliente
    @PostMapping
    public ResponseEntity<Customer> crear(@RequestBody Customer customer) {
        // @RequestBody lee el JSON del cuerpo de la petición y lo convierte a Customer
        Customer guardado = customerService.save(customer);
        return ResponseEntity.status(201).body(guardado); // 201 Created
    }

    // PUT /api/customers/5 → actualiza un cliente
    @PutMapping("/{id}")
    public ResponseEntity<Customer> actualizar(@PathVariable Integer id,
                                               @RequestBody Customer customer) {
        Optional<Customer> existente = customerService.getById(id);

        if (existente.isEmpty()) {
            return ResponseEntity.notFound().build(); // 404
        }

        customer.setCustomerId(id);
        Customer actualizado = customerService.save(customer);
        return ResponseEntity.ok(actualizado); // 200 OK
    }

    // DELETE /api/customers/5 → elimina un cliente
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        Optional<Customer> existente = customerService.getById(id);

        if (existente.isEmpty()) {
            return ResponseEntity.notFound().build(); // 404
        }

        customerService.delete(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}
```

### 6.3 `@RequestBody` — recibir JSON

Cuando el frontend envía datos en JSON, `@RequestBody` los convierte automáticamente al objeto Java correspondiente:

```
Frontend envía:
POST /api/customers
Content-Type: application/json
{
    "fullName": "Carlos López",
    "documentType": "CC",
    "documentNumber": "12345678"
}

Spring lo convierte automáticamente a:
Customer customer = new Customer();
customer.setFullName("Carlos López");
customer.setDocumentType("CC");
customer.setDocumentNumber("12345678");
```

### 6.4 `@RequestParam` — parámetros de query

```java
// URL: /api/customers/search?q=carlos&limite=10
@GetMapping("/search")
public ResponseEntity<List<Customer>> buscar(
        @RequestParam String q,
        @RequestParam(defaultValue = "5") int limite) {

    List<Customer> resultados = customerService.buscar(q, limite);
    return ResponseEntity.ok(resultados);
}
```

### 6.5 Los endpoints REST en Factugest — InvoiceController

Factugest mezcla `@Controller` con algunos endpoints REST para el autocompletado del formulario de facturas:

```java
// InvoiceController.java — Los endpoints de autocompletado son REST dentro de un @Controller
@Controller
@RequestMapping("/invoice")
public class InvoiceController {

    // Estos métodos tienen @ResponseBody → devuelven JSON aunque el controller sea @Controller

    // GET /invoice/api/customers/search?q=carlos
    @GetMapping("/api/customers/search")
    @ResponseBody
    public List<Map<String, Object>> buscarClientes(@RequestParam String q) {
        return invoiceService.searchCustomers(q);
        // Retorna: [{"customerId": 1, "fullName": "Carlos López"}, ...]
    }

    // GET /invoice/api/products/search?q=laptop
    @GetMapping("/api/products/search")
    @ResponseBody
    public List<Map<String, Object>> buscarProductos(@RequestParam String q) {
        return invoiceService.searchProductos(q);
        // Retorna: [{"codProducto": 1, "nombre": "Laptop", "precioUnitario": 2500000}, ...]
    }
}
```

> **Tip:** Puedes mezclar `@Controller` con `@ResponseBody` en métodos puntuales. `@RestController` es simplemente un `@Controller` que aplica `@ResponseBody` a TODOS sus métodos.

---

## Sección 7 — Controller vs RestController — Diferencias y cuándo usar cada uno

### Tabla comparativa

| Característica | `@Controller` | `@RestController` |
|----------------|---------------|-------------------|
| Retorna | Nombre de vista (String) | Objeto Java → JSON/XML |
| Necesita `@ResponseBody` | Solo si quieres JSON en un método | No, ya está incluido en todos |
| Usa `Model` | Sí, para pasar datos a la vista | No |
| Usa Thymeleaf | Sí | No |
| `redirect:` | Sí | No (usa HTTP 302 con ResponseEntity) |
| Ideal para | Aplicaciones web con servidor de vistas | APIs consumidas por frontend externo o móvil |

### Cuándo usar cada uno en Factugest

```
Factugest es una aplicación MVC tradicional (full-stack en el servidor):
→ Usa @Controller + Thymeleaf para todas las páginas

Si en el futuro se refactoriza para tener un frontend separado (React/Vue):
→ Los @Controller se convierten en @RestController
→ Thymeleaf ya no se necesita
→ El frontend hace peticiones fetch/axios y recibe JSON
```

### Ejemplo del mismo módulo con ambos enfoques

**Con `@Controller` (actual en Factugest):**

```java
@Controller
@RequestMapping("/customer")
public class CustomerController {

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("customers", customerService.getAll());
        return "customer/customers"; // Thymeleaf genera el HTML
    }

    @PostMapping("/save")
    public String guardar(@ModelAttribute Customer customer) {
        customerService.save(customer);
        return "redirect:/customer"; // Redirige tras guardar
    }
}
```

**Con `@RestController` (si el frontend fuera React/Vue):**

```java
@RestController
@RequestMapping("/api/customers")
public class CustomerApiController {

    @GetMapping
    public ResponseEntity<List<Customer>> listar() {
        return ResponseEntity.ok(customerService.getAll()); // JSON directo
    }

    @PostMapping
    public ResponseEntity<Customer> guardar(@RequestBody Customer customer) {
        return ResponseEntity.status(201).body(customerService.save(customer));
        // El frontend recibe el objeto creado con su ID
    }
}
```

---

## Sección 8 — Conexión con el Frontend

### 8.1 Frontend con Thymeleaf (server-side rendering — lo que usa Factugest)

Thymeleaf procesa el HTML **en el servidor** antes de enviarlo al navegador. El navegador recibe HTML ya listo.

**Flujo:**
```
Navegador solicita GET /customer
    → Controller llena el Model con datos
    → Thymeleaf toma customer/lista.html + datos
    → Genera HTML completo
    → Envía HTML al navegador
```

**Sintaxis de Thymeleaf más usada en Factugest:**

```html
<!-- th:text — mostrar texto -->
<td th:text="${customer.fullName}">Nombre por defecto</td>

<!-- th:each — iterar una lista -->
<tr th:each="customer : ${customers}">
    <td th:text="${customer.customerId}"></td>
    <td th:text="${customer.fullName}"></td>
</tr>

<!-- th:if / th:unless — condicionales -->
<span th:if="${customer.tipoPersona == 'Natural'}">Persona Natural</span>
<span th:unless="${customer.activo}">INACTIVO</span>

<!-- th:href — enlaces dinámicos -->
<a th:href="@{/customer/edit/{id}(id=${customer.customerId})}">Editar</a>

<!-- th:action — acción del formulario -->
<form th:action="@{/customer/save}" method="post">

<!-- th:field — campo vinculado al objeto -->
<input type="text" th:field="*{fullName}">

<!-- th:object — objeto del formulario -->
<form th:object="${customer}">

<!-- Expresiones de seguridad -->
<div th:if="${#authorization.expression('hasRole(''ADMIN'')')}">
    Solo ADMIN ve esto
</div>
```

### 8.2 Frontend desacoplado con Fetch API (JavaScript → REST)

Esta es la forma en que funciona el **autocompletado de clientes y productos** en Factugest. El formulario de factura hace peticiones en tiempo real al servidor sin recargar la página:

```javascript
// static/js/invoice.js (simplificado del comportamiento real de Factugest)

// Cuando el usuario escribe en el campo de búsqueda de clientes
async function buscarCliente(query) {
    if (query.length < 2) return;

    // Llama al endpoint REST del InvoiceController
    const response = await fetch(`/invoice/api/customers/search?q=${query}`);
    const clientes = await response.json();

    // clientes = [{customerId: 1, fullName: "Carlos López"}, ...]

    // Mostrar resultados en un dropdown
    mostrarSugerencias(clientes);
}

// Cuando el usuario escribe en el campo de producto
async function buscarProducto(query) {
    const response = await fetch(`/invoice/api/products/search?q=${query}`);
    const productos = await response.json();

    // productos = [{codProducto: 1, nombre: "Laptop", precioUnitario: 2500000}, ...]

    mostrarProductos(productos);
}
```

**El controller que responde (REST dentro de @Controller):**

```java
@GetMapping("/api/customers/search")
@ResponseBody
public List<Map<String, Object>> buscarClientes(@RequestParam String q) {
    return invoiceService.searchCustomers(q);
    // Spring Jackson convierte automáticamente la List<Map> a JSON
}
```

### 8.3 Cómo conectar un frontend completamente separado (React/Vue/Angular)

Si Factugest tuviera el frontend en React, la comunicación sería así:

**Backend (Spring Boot `@RestController`):**

```java
// Configuración CORS necesaria para frontend externo
@RestController
@RequestMapping("/api/v1/customers")
@CrossOrigin(origins = "http://localhost:3000") // URL del frontend React
public class CustomerApiController {

    @GetMapping
    public ResponseEntity<List<Customer>> getAll() {
        return ResponseEntity.ok(customerService.getAll());
    }

    @PostMapping
    public ResponseEntity<Customer> create(@RequestBody Customer customer) {
        return ResponseEntity.status(201).body(customerService.save(customer));
    }
}
```

**Frontend React (consumiendo la API):**

```javascript
// Obtener clientes
const response = await fetch('http://localhost:8080/api/v1/customers');
const clientes = await response.json();

// Crear cliente
const response = await fetch('http://localhost:8080/api/v1/customers', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
        fullName: 'Carlos López',
        documentType: 'CC',
        documentNumber: '12345678'
    })
});
const nuevoCliente = await response.json();
```

### 8.4 DTOs — Objetos de Transferencia de Datos (buena práctica en REST)

Cuando tienes APIs REST públicas, se recomienda usar DTOs para no exponer directamente las entidades (con todos sus campos, incluyendo contraseñas hasheadas, etc.):

```java
// En lugar de exponer Usuario directamente (contiene la contraseña hasheada!)
// Se crea un DTO con solo los campos necesarios:

public class UsuarioDTO {
    private Integer codUsuario;
    private String nombre;
    private String correo;
    private String rol;
    // No incluye contrasena ← seguridad

    // Constructor desde entidad
    public UsuarioDTO(Usuario usuario) {
        this.codUsuario = usuario.getCodUsuario();
        this.nombre = usuario.getNombre();
        this.correo = usuario.getCorreo();
        this.rol = usuario.getRol();
    }
}

// En el RestController:
@GetMapping("/api/usuarios")
public ResponseEntity<List<UsuarioDTO>> getAll() {
    List<UsuarioDTO> dtos = usuarioService.getAll().stream()
        .map(UsuarioDTO::new)
        .collect(Collectors.toList());
    return ResponseEntity.ok(dtos);
}
```

---

## Sección 9 — Spring Data JPA — La capa de datos

### 9.1 Entidades (`@Entity`)

Las entidades son clases Java que mapean tablas de base de datos. Cada objeto es una fila.

```java
@Entity                          // Marca la clase como entidad JPA
@Table(name = "customers")       // Nombre de la tabla en BD
@Data                            // Lombok: genera getters, setters, etc.
public class Customer {

    @Id                                                    // Clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY)    // Auto-increment
    @Column(name = "customer_id")                          // Nombre de la columna
    private Integer customerId;

    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    @Column(name = "document_type")
    private String documentType;

    @Column(name = "document_number", unique = true)
    private String documentNumber;
}
```

### 9.2 Repositorios (`JpaRepository`)

```java
// Solo con extender JpaRepository, Spring genera todo el CRUD automáticamente
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    //                                                        ^       ^
    //                                               Tipo entidad   Tipo del ID

    // Métodos que Spring JPA genera automáticamente (sin escribir código):
    // findAll()              → SELECT * FROM customers
    // findById(id)           → SELECT * FROM customers WHERE customer_id = id
    // save(customer)         → INSERT o UPDATE automáticamente
    // deleteById(id)         → DELETE FROM customers WHERE customer_id = id
    // count()                → SELECT COUNT(*) FROM customers
    // existsById(id)         → SELECT COUNT(*) > 0 WHERE customer_id = id

    // Métodos derivados del nombre (Spring los genera consultando el nombre):
    List<Customer> findByTipoPersona(String tipoPersona);
    // → SELECT * FROM customers WHERE tipo_persona = ?

    List<Customer> findByFullNameContainingIgnoreCase(String nombre);
    // → SELECT * FROM customers WHERE LOWER(full_name) LIKE LOWER('%nombre%')

    Optional<Customer> findByDocumentNumber(String numero);
    // → SELECT * FROM customers WHERE document_number = ?

    List<Customer> findByRegimenTributarioAndTipoPersona(String regimen, String tipo);
    // → SELECT * WHERE regimen_tributario = ? AND tipo_persona = ?
}
```

### 9.3 Servicios

El servicio es el intermediario entre el controller y el repository. Contiene la lógica de negocio:

```java
@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<Customer> getAll() {
        return customerRepository.findAll();
    }

    public Optional<Customer> getById(Integer id) {
        return customerRepository.findById(id);
    }

    public Customer save(Customer customer) {
        // Aquí puedes agregar validaciones antes de guardar
        if (customer.getFullName() == null || customer.getFullName().isBlank()) {
            throw new IllegalArgumentException("El nombre del cliente es obligatorio");
        }
        return customerRepository.save(customer);
    }

    public void delete(Integer id) {
        customerRepository.deleteById(id);
    }
}
```

### 9.4 `@Transactional` — operaciones atómicas

Cuando necesitas que varias operaciones de BD sean "todo o nada":

```java
@Service
public class InvoiceService {

    @Transactional  // Si algo falla, se deshace TODO (rollback automático)
    public Factura createInvoice(Factura factura, List<DetalleFactura> detalles) {
        // Paso 1: guardar la factura
        Factura saved = facturaRepository.save(factura);

        // Paso 2: guardar todos los detalles
        for (DetalleFactura detalle : detalles) {
            detalle.setCodFactura(saved.getCodFactura());
            detalleRepository.save(detalle);
            // Si esto falla, la factura también se revierte ← @Transactional
        }

        // Paso 3: actualizar el stock
        actualizarStock(detalles);
        // Si esto falla, todo se revierte ← @Transactional

        return saved;
    }
}
```

---

## Sección 10 — Spring Security — Autenticación y Autorización

### 10.1 Conceptos básicos

| Término | Significado |
|---------|-------------|
| **Autenticación** | ¿Quién eres? (login con correo/contraseña) |
| **Autorización** | ¿Qué puedes hacer? (roles, permisos) |
| **Sesión** | Spring guarda que estás autenticado para no pedir login en cada petición |
| **BCrypt** | Algoritmo para hashear contraseñas (nunca se guardan en texto plano) |

### 10.2 SecurityConfig — El corazón de la seguridad

```java
// security/SecurityConfig.java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // ── Reglas de acceso por URL ──────────────────────────────────
            .authorizeHttpRequests(auth -> auth
                // Estos recursos son públicos (sin login)
                .requestMatchers("/css/**", "/img/**", "/js/**").permitAll()
                .requestMatchers("/login").permitAll()
                // Solo ADMIN puede ver usuarios y logs
                .requestMatchers("/users/**", "/logs/**").hasRole("ADMIN")
                // Todo lo demás requiere estar autenticado
                .anyRequest().authenticated()
            )

            // ── Configuración del formulario de login ─────────────────────
            .formLogin(form -> form
                .loginPage("/login")                  // URL de la página de login
                .loginProcessingUrl("/login")         // URL que procesa el POST del form
                .usernameParameter("correo")          // Nombre del campo correo en el form
                .passwordParameter("contrasena")      // Nombre del campo contraseña en el form
                .defaultSuccessUrl("/", true)         // A dónde ir si el login es exitoso
                .failureUrl("/login?error=true")      // A dónde ir si falla
                .permitAll()
            )

            // ── Configuración del logout ──────────────────────────────────
            .logout(logout -> logout
                .logoutUrl("/logout")                 // URL para hacer logout
                .logoutSuccessUrl("/login")           // A dónde ir después
                .invalidateHttpSession(true)          // Destruye la sesión
                .clearAuthentication(true)            // Limpia la autenticación
                .permitAll()
            );

        return http.build();
    }

    // Bean que indica cómo hashear/verificar contraseñas
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10); // Factor de costo: 10 rondas
    }
}
```

### 10.3 CustomUserDetailsService — Cargar el usuario desde la BD

Spring Security llama a este servicio cuando alguien intenta hacer login:

```java
// security/CustomUserDetailsService.java
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository,
                                     PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        // 1. Buscar el usuario en la BD por su correo
        Usuario usuario = usuarioRepository.findByCorreo(correo)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + correo));

        // 2. Crear el objeto de roles/permisos
        List<GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("ROLE_" + usuario.getRol())
            // Si rol = "ADMIN" → autoridad = "ROLE_ADMIN"
        );

        // 3. Retornar un UserDetails (Spring lo compara con la contraseña enviada)
        return new CustomUserPrincipal(
            usuario.getCorreo(),
            usuario.getContrasena(), // Hash BCrypt guardado en BD
            authorities,
            usuario.getNombre(),
            usuario.getRol()
        );
        // Spring hace: BCrypt.verify(contraseñaEnviada, hash) → true/false
    }
}
```

### 10.4 CustomUserPrincipal — El usuario autenticado enriquecido

```java
// security/CustomUserPrincipal.java
public class CustomUserPrincipal extends User {

    private final String nombre;
    private final String rol;
    private final Integer codUsuario;

    public CustomUserPrincipal(String username, String password,
                                Collection<? extends GrantedAuthority> authorities,
                                String nombre, String rol, Integer codUsuario) {
        super(username, password, authorities);
        this.nombre = nombre;
        this.rol = rol;
        this.codUsuario = codUsuario;
    }

    // Getters
    public String getNombre() { return nombre; }
    public String getRol() { return rol; }
    public Integer getCodUsuario() { return codUsuario; }
}
```

### 10.5 Obtener el usuario actual en un Controller

```java
@Controller
public class HomeController {

    @GetMapping("/")
    public String dashboard(Model model, Authentication authentication) {
        // Obtener el usuario autenticado
        CustomUserPrincipal user = (CustomUserPrincipal) authentication.getPrincipal();

        model.addAttribute("nombreUsuario", user.getNombre());
        model.addAttribute("rolUsuario", user.getRol());

        return "index";
    }
}
```

En Thymeleaf, acceder al usuario autenticado:
```html
<!-- Con thymeleaf-extras-springsecurity6 -->
<span th:text="${#authentication.principal.nombre}">Nombre</span>
<div th:if="${#authorization.expression('hasRole(''ADMIN'')')}">
    Solo ADMIN ve esto
</div>
```

### 10.6 Flujo completo del login en Factugest

```
1. Usuario visita /login
   → LoginController devuelve templates/login.html

2. Usuario llena correo y contraseña y hace clic en "Entrar"
   → POST /login con campos "correo" y "contrasena"

3. Spring Security intercepta el POST /login
   → Llama a CustomUserDetailsService.loadUserByUsername(correo)

4. CustomUserDetailsService busca el usuario en la BD por su correo
   → Si no existe: lanza UsernameNotFoundException → falla el login

5. Si existe, Spring compara:
   BCrypt.verify(contraseñaEnviada, hashEnBD)
   → Si no coincide: falla el login → redirect /login?error=true
   → Si coincide: login exitoso

6. Login exitoso:
   → Spring crea una sesión HTTP con la autenticación
   → Redirige a "/" (dashboard)

7. Peticiones posteriores:
   → Spring revisa la sesión antes de cada petición
   → Si la sesión es válida y tiene permisos: permite el acceso
   → Si no: redirige a /login
```

### 10.7 BCrypt — Por qué nunca guardamos contraseñas en texto plano

```java
// NUNCA hagas esto
usuario.setContrasena("miContrasena123"); // Texto plano ← PELIGRO

// SIEMPRE hashea antes de guardar
BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
String hash = encoder.encode("miContrasena123");
// hash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
usuario.setContrasena(hash);

// Para verificar (login):
boolean correcto = encoder.matches("miContrasena123", hash); // true
boolean incorrecto = encoder.matches("otraContrasena", hash); // false
```

---

## Sección 11 — Flujo completo de una petición en Factugest

Ejemplo: el usuario abre `/customer` para ver la lista de clientes.

```
1. Navegador → GET /customer

2. Spring Security intercepta:
   → ¿Tiene sesión válida? Sí → continúa
   → ¿Tiene permisos para /customer? Sí (cualquier autenticado) → continúa

3. DispatcherServlet (Spring MVC) recibe la petición
   → Busca qué método maneja GET /customer
   → Encuentra CustomerController.listar()

4. CustomerController.listar() se ejecuta:
   → Llama a customerService.getAll()

5. CustomerService.getAll() se ejecuta:
   → Llama a customerRepository.findAll()

6. CustomerRepository.findAll():
   → Hibernate genera: SELECT * FROM customers
   → Ejecuta la query en PostgreSQL
   → Convierte cada fila en un objeto Customer
   → Retorna List<Customer>

7. La lista vuelve al Controller
   → model.addAttribute("customers", lista)
   → return "customer/customers"

8. Thymeleaf procesa templates/customer/customers.html:
   → Reemplaza th:each="${customers}" con las filas de la tabla
   → Genera HTML completo

9. Spring devuelve el HTML al navegador
   → 200 OK + HTML

10. El navegador muestra la página al usuario
```

---

## Sección 12 — Ejercicios de Cierre

### Ejercicio 12.1 — Controller con Thymeleaf (módulo de Productos)

Crea un `ProductController` completo que maneje:
1. `GET /products` → lista todos los productos activos
2. `GET /products/create` → muestra el formulario de creación
3. `POST /products/save` → guarda el producto y redirige
4. `GET /products/edit/{id}` → muestra el formulario con datos del producto a editar
5. `POST /products/delete/{id}` → desactiva el producto (cambia `activo = false`, no borra)

Para cada método, escribe qué pondría en el `Model` y qué vista retornaría.

---

### Ejercicio 12.2 — RestController para API de Productos

Crea un `ProductApiController` en `/api/products` con:
1. `GET /api/products` → retorna todos los productos como JSON
2. `GET /api/products/{id}` → retorna un producto por ID (404 si no existe)
3. `POST /api/products` → crea un nuevo producto desde JSON
4. `PUT /api/products/{id}` → actualiza un producto
5. `DELETE /api/products/{id}` → elimina un producto (204 si OK, 404 si no existe)

Usa `ResponseEntity` para todos los métodos.

---

### Ejercicio 12.3 — Fetch API desde el frontend

Dado que existe el endpoint `GET /api/products/search?q=texto`, escribe el código JavaScript que:
1. Escucha el evento `input` de un campo de texto con id `buscarProducto`
2. Cuando el usuario escribe 2 o más caracteres, llama al endpoint
3. Muestra los resultados en un `<ul>` con id `resultados`
4. Cada `<li>` debe mostrar el nombre del producto y su precio

---

### Ejercicio 12.4 — Seguridad por roles

Modifica (en papel/descripción) la `SecurityConfig` para que:
- `/api/**` requiera autenticación pero sea accesible por ADMIN y CAJERO
- `/products/delete/**` solo sea accesible por ADMIN
- `/logs/**` solo sea accesible por ADMIN
- `/api/products` (GET) sea público (sin login)
- Todo lo demás requiera estar autenticado

Escribe el bloque `.authorizeHttpRequests(...)` completo.

---

### Ejercicio Final — Mini módulo completo

Implementa el módulo de **Métodos de Pago** completamente:

**Entidad:** `MetodoPago` (ya existe en el proyecto — estúdiala)

**Service:** `MetodoPagoService` con:
- `getAll()`, `getById(Integer id)`, `save(MetodoPago mp)`, `delete(Integer id)`

**Controller MVC:** `PaymentMethodController` con:
- Listar, crear, editar y eliminar (con Thymeleaf)

**Controller REST:** `PaymentMethodApiController` con:
- GET todos, GET por ID, POST crear, PUT actualizar, DELETE eliminar

**Seguridad:**
- Solo ADMIN puede crear, editar y eliminar
- Cualquier usuario autenticado puede listar

Describe (en comentarios o pseudocódigo) cómo implementarías cada parte, referenciando los patrones que viste en el curso.

---

## Resumen del Curso 03

| Concepto | Descripción | Ejemplo en Factugest |
|----------|-------------|----------------------|
| `@SpringBootApplication` | Arranca todo Spring Boot | `FactugestApplication.java` |
| `@Controller` | Devuelve vistas HTML | `CustomerController`, `InvoiceController` |
| `@RestController` | Devuelve JSON | Endpoints `/api/` en `InvoiceController` |
| `@Service` | Lógica de negocio | `CustomerService`, `InvoiceService` |
| `@Repository` | Acceso a datos | `CustomerRepository extends JpaRepository` |
| `@Entity` | Mapeo tabla-clase | `Customer`, `Factura`, `Producto` |
| `Model` | Datos de controller → vista | `model.addAttribute("customers", lista)` |
| `@ModelAttribute` | Formulario HTML → objeto Java | Formulario de cliente |
| `@RequestBody` | JSON → objeto Java | Endpoints REST |
| `ResponseEntity` | Control HTTP en REST | Códigos 200, 201, 404 |
| `SecurityConfig` | Reglas de acceso | Roles ADMIN y CAJERO |
| `BCrypt` | Hash de contraseñas | `PasswordEncoder` en security |
| `UserDetailsService` | Cargar usuario para login | `CustomUserDetailsService` |
| `@Transactional` | Operaciones atómicas | Crear factura con detalles |

---

## ¡Felicitaciones!

Completaste los 3 cursos. Ahora tienes las bases para:

1. Leer y entender el código Java de Factugest
2. Entender las entidades como modelos de datos (POO)
3. Seguir el flujo de una petición desde el navegador hasta la base de datos
4. Agregar nuevos endpoints tanto para vistas como para API REST
5. Entender cómo funciona la autenticación y los roles

### Recursos recomendados para profundizar
- Documentación oficial de Spring Boot: `spring.io/projects/spring-boot`
- Guía de Spring Security: `spring.io/guides/gs/securing-web/`
- Thymeleaf docs: `thymeleaf.org/documentation.html`
- El propio código de Factugest: `src/main/java/com/factugest/`

---

*Factugest — Equipo de Desarrollo*
