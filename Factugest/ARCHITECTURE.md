# Factugest — Arquitectura Técnica

Documento técnico del sistema. Describe el flujo completo de una petición HTTP, la interacción entre capas, los patrones usados y un mapa visual del proyecto.

---

## Mapa General del Proyecto

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              NAVEGADOR (Cliente)                            │
│                        http://localhost:8080/...                            │
└──────────────────────────────────┬──────────────────────────────────────────┘
                                   │  HTTP Request (GET / POST)
                                   ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                         SPRING MVC DISPATCHER                               │
│              (DispatcherServlet — punto de entrada de Spring)               │
└──────────────────────────────────┬──────────────────────────────────────────┘
                                   │  Routing por @RequestMapping
                                   ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                         CAPA DE CONTROLADORES                               │
│                      com.factugest.controller.*                             │
│                                                                             │
│   HomeController        InvoiceController      CustomerController          │
│   ProductController     UserController         BranchController            │
│   PaymentMethodController  DiscountController  TaxController               │
│   InvoicePaymentController  LogController      ProductDiscountController   │
└───────────────┬─────────────────────────┬───────────────────────────────────┘
                │                         │
                │ Llama a Services        │ Inyecta datos al Model
                ▼                         ▼
┌───────────────────────────┐   ┌─────────────────────────────────────────────┐
│     CAPA DE SERVICIOS     │   │              THYMELEAF ENGINE               │
│  com.factugest.service.*  │   │         templates/*.html                    │
│                           │   │                                             │
│  InvoiceService           │   │   layout.html (plantilla base)             │
│  ProductoService          │   │   index.html, invoice/*, customer/*        │
│  CustomerService          │   │   product/*, users/*, branches/*           │
│  EmpresaService           │   │   payment_methods/*, discount/*            │
│  MetodoPagoService        │   │   invoice_taxes/*, invoice_payments/*      │
│  PagoFacturaService       │   │   logs/*, settings/*                       │
│  DiscountService          │   └─────────────────────────────────────────────┘
│  ImpuestoService          │
│  UsuarioService           │
│  LogService               │
│  ProductDiscountService   │
│  PdfService               │
└───────────┬───────────────┘
            │
     ┌──────┴────────┐
     │               │
     ▼               ▼
┌─────────┐   ┌─────────────┐
│   JPA   │   │ JdbcTemplate│
│Reposit. │   │  (SQL raw)  │
└────┬────┘   └──────┬──────┘
     │               │
     └───────┬────────┘
             │
             ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                         BASE DE DATOS PostgreSQL                            │
│                    localhost:5432 / database: factugest                     │
│                                                                             │
│  customers   facturas   detalle_factura   productos   impuestos            │
│  descuentos  usuarios   empresas          metodos_pago  pagos_factura      │
│  logs        producto_descuento                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Ciclo de Vida de una Petición HTTP

### Ejemplo: Ver lista de facturas `GET /invoice`

```
1. Navegador               GET http://localhost:8080/invoice
        │
        ▼
2. DispatcherServlet       Recibe la petición, busca @RequestMapping("/invoice")
        │
        ▼
3. InvoiceController       @GetMapping → listInvoices(Model model)
        │
        │  invoiceService.getAllInvoicesDetailed()
        ▼
4. InvoiceService          Ejecuta SQL con JdbcTemplate:
        │                  SELECT f.cod_factura, f.fecha, f.total,
        │                         c.full_name AS cliente,
        │                         p.status AS estado_pago, ...
        │                  FROM facturas f
        │                  LEFT JOIN customers c ON f.cod_cliente = c.customer_id
        │                  LEFT JOIN pagos_factura p ON f.cod_pago = p.cod_pago_factura
        │                  ORDER BY f.cod_factura DESC
        │
        │  Retorna: List<Map<String, Object>>
        ▼
5. InvoiceController       model.addAttribute("all_invoices", result)
        │                  return "invoice/index"
        ▼
6. Thymeleaf Engine        Busca: templates/invoice/index.html
        │                  Aplica layout:decorate="~{layout}"
        │                  Inyecta variables del Model
        │                  Genera HTML final
        ▼
7. Navegador               Renderiza la página con la tabla de facturas
```

---

### Ejemplo: Crear una factura `POST /invoice/new`

```
1. Usuario llena el formulario en /invoice/new y hace clic en "Guardar"
        │
        ▼
2. Navegador               POST /invoice/new
                           Body (form-data):
                             cod_cliente=5, cod_empresa=1,
                             cod_producto[]=3, cod_producto[]=7,
                             precio_unitario[]=50000, precio_unitario[]=120000,
                             cantidad[]=2, cantidad[]=1,
                             descuento_porcentaje[]=0, descuento_porcentaje[]=10, ...
        │
        ▼
3. InvoiceController       @PostMapping("/new") → createInvoice(
                             @RequestParam List<Integer> codProductos,
                             @RequestParam List<BigDecimal> precios,
                             @RequestParam List<Integer> cantidades,
                             @RequestParam List<BigDecimal> descuentos, ...)
        │
        │  Para cada producto:
        │    invoiceService.getProductTaxInfo(codProd) → obtiene % IVA
        │    Calcula: bruto, descuento_valor, base_gravable, iva_valor
        │
        │  invoiceService.createInvoice(cod_cliente, ..., totalFinal, ...)
        ▼
4. InvoiceService          INSERT INTO facturas (...) VALUES (...)
        │                  SELECT currval('facturas_cod_factura_seq') → invoiceId
        │
        │  Para cada línea:
        │  invoiceService.createInvoiceDetail(invoiceId, cod_producto, ...)
        ▼
5. InvoiceService          INSERT INTO detalle_factura (...) VALUES (...)
        │
        ▼
6. InvoiceController       return "redirect:/invoice/42"
        │
        ▼
7. Navegador               GET /invoice/42 → vista detalle de la factura creada
```

---

### Ejemplo: Descargar PDF `GET /invoice/42/pdf`

```
1. Navegador               GET /invoice/42/pdf
        │
        ▼
2. InvoiceController       @GetMapping("/{id}/pdf") → downloadPdf(response)
        │
        │  invoiceService.getInvoiceById(42)    → Map<String, Object> invoice
        │  invoiceService.getInvoiceDetails(42) → List<Map<String,Object>> details
        ▼
3. PdfService              generateInvoicePdf(invoice, details)
        │                  ┌─ iText7: crea PdfDocument en ByteArrayOutputStream
        │                  ├─ Sección empresa: NIT, nombre, régimen, membrete
        │                  ├─ Sección cliente: nombre, documento, dirección
        │                  ├─ Tabla de productos: cant, descripción, valor unit,
        │                  │    descuento, subtotal, IVA, total línea
        │                  └─ Sección totales DIAN: subtotal, descuentos,
        │                       base gravable, IVA, TOTAL
        │
        │  Retorna: byte[]
        ▼
4. InvoiceController       response.setContentType("application/pdf")
                           response.setHeader("Content-Disposition",
                             "attachment; filename=factura_42.pdf")
                           response.getOutputStream().write(pdfBytes)
        │
        ▼
5. Navegador               Descarga automática del archivo PDF
```

---

## Descripción de Capas

### 1. Capa de Controladores — `controller/`

Responsabilidad: recibir peticiones HTTP, validar parámetros, delegar al service, inyectar datos al Model, retornar el nombre de la vista o redirect.

| Clase | Ruta base | Responsabilidades |
|---|---|---|
| `HomeController` | `/`, `/settings` | Dashboard stats, settings |
| `InvoiceController` | `/invoice` | CRUD facturas, PDF, cambio estado |
| `CustomerController` | `/customer` | CRUD clientes |
| `ProductController` | `/products/product` | CRUD productos |
| `UserController` | `/users` | CRUD usuarios |
| `BranchController` | `/branches` | CRUD empresas |
| `PaymentMethodController` | `/payment_methods` | CRUD métodos de pago |
| `DiscountController` | `/discount` | CRUD descuentos |
| `TaxController` | `/invoice_taxes` | CRUD impuestos |
| `InvoicePaymentController` | `/invoice_payments` | CRUD estados de pago |
| `LogController` | `/logs` | Ver logs del sistema |
| `ProductDiscountController` | `/product_discounts` | Ver relaciones producto-descuento |

Patrón de método típico:
```java
@GetMapping("/{id}")
public String view(@PathVariable Integer id, Model model) {
    model.addAttribute("entity", service.getById(id));
    return "module/view";
}
```

---

### 2. Capa de Servicios — `service/`

Responsabilidad: lógica de negocio, cálculos, transformaciones, acceso a la base de datos.

Existen **dos mecanismos de acceso a datos** según la complejidad:

#### JPA Repository (CRUD simple)
Usado cuando la operación es directa sobre una sola tabla sin JOINs.
```java
customerRepository.findAll()       // SELECT * FROM customers
customerRepository.findById(id)    // SELECT * WHERE customer_id = ?
customerRepository.save(entity)    // INSERT / UPDATE
customerRepository.deleteById(id)  // DELETE WHERE customer_id = ?
```

#### JdbcTemplate (Consultas complejas con JOINs)
Usado en `InvoiceService`, `LogService`, `ProductDiscountService` donde se necesitan JOINs entre varias tablas.
```java
jdbc.queryForList("""
    SELECT f.cod_factura, f.fecha, f.total,
           c.full_name AS cliente,
           p.status AS estado_pago
    FROM facturas f
    LEFT JOIN customers c ON f.cod_cliente = c.customer_id
    LEFT JOIN pagos_factura p ON f.cod_pago = p.cod_pago_factura
    ORDER BY f.cod_factura DESC
""");
```

| Service | Usa JPA | Usa JdbcTemplate | Función principal |
|---|---|---|---|
| `InvoiceService` | No | Sí | CRUD facturas, JOINs, dashboard stats |
| `ProductoService` | Sí | Sí | CRUD + consulta con JOIN para impuesto |
| `CustomerService` | Sí | No | CRUD clientes |
| `EmpresaService` | Sí | No | CRUD empresas |
| `MetodoPagoService` | Sí | No | CRUD métodos de pago |
| `PagoFacturaService` | Sí | No | CRUD estados de pago |
| `ImpuestoService` | Sí | No | CRUD impuestos |
| `DescuentoService` | Sí | No | CRUD descuentos |
| `UsuarioService` | Sí | No | CRUD usuarios |
| `LogService` | No | Sí | Consulta logs con JOINs |
| `ProductDiscountService` | No | Sí | Relaciones producto-descuento |
| `PdfService` | No | No | Genera PDF con iText7 (no accede a BD) |

---

### 3. Capa de Repositorios — `repository/`

Interfaces que extienden `JpaRepository<Entidad, Integer>`. Spring Data genera automáticamente la implementación en tiempo de arranque.

```java
public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    // Spring genera automáticamente:
    // findAll(), findById(), save(), deleteById(), count(), existsById(), ...
}
```

No hay consultas `@Query` definidas manualmente. Para lógica compleja se usa JdbcTemplate directamente en el Service.

---

### 4. Capa de Entidades — `entity/`

Clases Java anotadas con JPA que mapean exactamente las tablas existentes en PostgreSQL. Hibernate usa `ddl-auto=validate` — **nunca crea ni altera tablas**, solo verifica que los campos coincidan al arrancar.

```java
@Entity
@Table(name = "customers")
@Data  // Lombok genera getters/setters/toString/equals/hashCode
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Integer customerId;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "document_type")
    private String documentType;
    // ...
}
```

**Entidades y sus tablas PostgreSQL:**

| Clase Java       | Tabla PostgreSQL   | Clave primaria         |
|------------------|--------------------|------------------------|
| `Customer`       | `customers`        | `customer_id`          |
| `Factura`        | `facturas`         | `cod_factura`          |
| `DetalleFactura` | `detalle_factura`  | `cod_destalle`         |
| `Producto`       | `productos`        | `cod_producto`         |
| `Impuesto`       | `impuestos`        | `cod_impuesto`         |
| `Descuento`      | `descuentos`       | `cod_descuento`        |
| `Usuario`        | `usuarios`         | `cod_usuario`          |
| `Empresa`        | `empresas`         | `cod_empresa`          |
| `MetodoPago`     | `metodos_pago`     | `cod_pago`             |
| `PagoFactura`    | `pagos_factura`    | `cod_pago_factura`     |
| `Log`            | `logs`             | `id_log`               |

Todas las claves primarias usan secuencias PostgreSQL (`GENERATED ALWAYS AS IDENTITY` o `SERIAL`), gestionadas automáticamente por Hibernate.

---

### 5. Capa de Vistas — `templates/`

Plantillas Thymeleaf que generan HTML dinámico en el servidor. Usa el **Thymeleaf Layout Dialect** para herencia de plantillas.

**Plantilla base — `layout.html`:**
```html
<!-- Define zonas reemplazables con layout:fragment -->
<div layout:fragment="content"><!-- contenido de cada página --></div>
<div layout:fragment="scripts"><!-- scripts específicos de cada página --></div>
```

**Página hija — cualquier template:**
```html
<html layout:decorate="~{layout}">  <!-- hereda layout.html -->
<section layout:fragment="content"> <!-- reemplaza el fragment "content" -->
    <!-- HTML específico de esta página -->
</section>
```

**Expresiones Thymeleaf más usadas en el proyecto:**

| Expresión | Propósito | Ejemplo |
|---|---|---|
| `th:text="${var}"` | Renderiza texto | `th:text="${customer.fullName}"` |
| `th:each="x : ${list}"` | Itera lista | `th:each="inv : ${all_invoices}"` |
| `th:if="${cond}"` | Condicional | `th:if="${product.stock > 0}"` |
| `th:href="@{/path}"` | URL segura | `th:href="@{/invoice/new}"` |
| `th:action="@{/path}"` | URL de formulario | `th:action="@{/customer/new}"` |
| `th:value="${var}"` | Valor de input | `th:value="${customer.email}"` |
| `th:data-name="${var}"` | Atributo data (para JS) | `th:data-name="${product.nombre}"` |
| `#numbers.formatDecimal(...)` | Formateo moneda | `#numbers.formatDecimal(total, 0, 'COMMA', 2, 'POINT')` |
| `#dates.format(...)` | Formateo fecha | `#dates.format(invoice.fecha, 'dd/MM/yyyy')` |

> **Nota Thymeleaf 3.1:** El motor prohíbe expresiones de cadena en atributos de eventos DOM (`th:onclick`). En este proyecto se usa el patrón `th:data-name="${entity.field}"` + `onclick="return confirm('...' + this.dataset.name + '?')"` como alternativa segura.

---

## Diagrama de Relaciones entre Tablas

```
                    ┌──────────────┐
                    │   usuarios   │
                    │  cod_usuario │◄──────────────────────┐
                    └──────────────┘                       │
                                                           │
┌──────────────┐   ┌────────────────────────────────────────────────────┐
│   customers  │   │                     facturas                       │
│  customer_id │◄──┤ cod_cliente                                        │
└──────────────┘   │ cod_usuario ──────────────────────────────────────►│
                   │ cod_empresa ──────────┐                            │
                   │ cod_metodo_pago ───┐  │                            │
                   │ cod_pago ──────┐   │  │                            │
                   │ cod_factura ◄──┼───┼──┼────────────────────────┐  │
                   └───────────────┼───┼──┼────────────────────────┼──┘
                                   │   │  │                        │
                    ┌──────────────┘   │  └──────────────────────┐ │
                    ▼                  ▼                          ▼ │
             ┌──────────────┐   ┌──────────────┐          ┌────────────┐
             │ pagos_factura│   │ metodos_pago │          │  empresas  │
             │cod_pago_fact.│   │   cod_pago   │          │cod_empresa │
             └──────────────┘   └──────────────┘          └────────────┘

                        │ cod_factura
                        ▼
               ┌────────────────────┐
               │   detalle_factura  │
               │ cod_factura        │
               │ cod_producto ──────┼──┐
               │ descuento_porc.    │  │
               │ impuesto_porc.     │  │
               └────────────────────┘  │
                                       ▼
                              ┌─────────────────┐   ┌──────────────────┐
                              │    productos    │   │    impuestos     │
                              │  cod_producto   │   │  cod_impuesto    │
                              │  cod_impuesto ──┼──►│  porcentaje      │
                              └────────┬────────┘   └──────────────────┘
                                       │
                                       │ (tabla puente)
                                       ▼
                              ┌────────────────────┐   ┌──────────────┐
                              │ producto_descuento │   │  descuentos  │
                              │  cod_producto      │   │ cod_descuento│
                              │  cod_descuento ────┼──►│ porcentaje   │
                              └────────────────────┘   └──────────────┘
```

---

## Mapa Completo de Archivos del Proyecto

```
Factugest Java/
│
├── pom.xml                          ← Dependencias Maven
│     Spring Boot 3.3.5, Thymeleaf, JPA, JdbcTemplate,
│     PostgreSQL driver, iText7 7.2.5, Lombok
│
├── mvnw / mvnw.cmd                  ← Maven Wrapper (no requiere Maven instalado)
│
├── postgres_migration.sql           ← Crea todas las tablas en PostgreSQL (ejecutar 1 vez)
├── reseed_data.sql                  ← Carga datos de prueba (TRUNCATE + INSERT, repetible)
│
└── src/main/
    │
    ├── java/com/factugest/
    │   │
    │   ├── FactugestApplication.java          ← @SpringBootApplication (main)
    │   │
    │   ├── controller/                        ← HTTP → Model → View name
    │   │   ├── HomeController.java            GET /  → index.html (dashboard)
    │   │   ├── InvoiceController.java         /invoice/**
    │   │   ├── CustomerController.java        /customer/**
    │   │   ├── ProductController.java         /products/product/**
    │   │   ├── UserController.java            /users/**
    │   │   ├── BranchController.java          /branches/**
    │   │   ├── PaymentMethodController.java   /payment_methods/**
    │   │   ├── DiscountController.java        /discount/**
    │   │   ├── TaxController.java             /invoice_taxes/**
    │   │   ├── InvoicePaymentController.java  /invoice_payments/**
    │   │   ├── LogController.java             /logs/**
    │   │   └── ProductDiscountController.java /product_discounts/**
    │   │
    │   ├── entity/                            ← @Entity (mapeo a tablas PostgreSQL)
    │   │   ├── Customer.java         → customers
    │   │   ├── Factura.java          → facturas
    │   │   ├── DetalleFactura.java   → detalle_factura
    │   │   ├── Producto.java         → productos
    │   │   ├── Impuesto.java         → impuestos
    │   │   ├── Descuento.java        → descuentos
    │   │   ├── Usuario.java          → usuarios
    │   │   ├── Empresa.java          → empresas
    │   │   ├── MetodoPago.java       → metodos_pago
    │   │   ├── PagoFactura.java      → pagos_factura
    │   │   └── Log.java              → logs
    │   │
    │   ├── repository/                        ← JpaRepository<Entity, Integer>
    │   │   ├── CustomerRepository.java
    │   │   ├── FacturaRepository.java
    │   │   ├── DetalleFacturaRepository.java
    │   │   ├── ProductoRepository.java
    │   │   ├── ImpuestoRepository.java
    │   │   ├── DescuentoRepository.java
    │   │   ├── UsuarioRepository.java
    │   │   ├── EmpresaRepository.java
    │   │   ├── MetodoPagoRepository.java
    │   │   ├── PagoFacturaRepository.java
    │   │   └── LogRepository.java
    │   │
    │   └── service/                           ← Lógica de negocio + DB access
    │       ├── InvoiceService.java            JdbcTemplate: JOINs complejos, dashboard
    │       ├── ProductoService.java           JPA + JdbcTemplate
    │       ├── CustomerService.java           JPA
    │       ├── EmpresaService.java            JPA
    │       ├── MetodoPagoService.java         JPA
    │       ├── PagoFacturaService.java        JPA
    │       ├── ImpuestoService.java           JPA
    │       ├── DescuentoService.java          JPA
    │       ├── UsuarioService.java            JPA
    │       ├── LogService.java                JdbcTemplate
    │       ├── ProductDiscountService.java    JdbcTemplate
    │       └── PdfService.java                iText7 (no accede a BD)
    │
    └── resources/
        │
        ├── application.properties             ← Configuración central
        │
        ├── static/
        │   ├── css/
        │   │   └── main.css                   ← Estilos: sidebar, navbar, cards
        │   └── img/
        │       ├── logodark.png               ← Logo (navbar con fondo oscuro)
        │       ├── logodark2.png              ← Logo alternativo
        │       ├── membrete.png               ← Membrete para PDFs
        │       └── prof_brandon.png           ← Avatar usuario
        │
        └── templates/
            ├── layout.html                    ← Plantilla base (navbar + sidebar)
            ├── index.html                     ← Dashboard / KPIs
            │
            ├── invoice/
            │   ├── index.html                 ← Lista de facturas
            │   ├── form.html                  ← Crear factura (con JS dinámico)
            │   └── view.html                  ← Ver factura + cambiar estado
            │
            ├── customer/        index + form + view
            ├── product/         index + form + view
            ├── users/           index + form
            ├── branches/        index + form
            ├── payment_methods/ index + form
            ├── discount/        index + form
            ├── invoice_taxes/   index + form
            ├── invoice_payments/index + form
            ├── logs/            index
            ├── product_discounts/index
            └── settings/        index
```

---

## Configuración Central — `application.properties`

Los secretos se inyectan mediante variables de entorno usando la sintaxis `${VARIABLE:default}` de Spring Boot. El archivo rastreado en Git **no contiene credenciales**.

```properties
# ── Base de datos ──────────────────────────────────────────────────
# Variables requeridas: DB_PASSWORD. El resto tiene valores por defecto.
spring.datasource.url=jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:factugest}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD:}
spring.datasource.driver-class-name=org.postgresql.Driver

# ── Hibernate / JPA ────────────────────────────────────────────────
spring.jpa.hibernate.ddl-auto=validate        # NUNCA crea ni modifica tablas
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.show-sql=false

# ── Thymeleaf ──────────────────────────────────────────────────────
spring.thymeleaf.cache=false                  # Cambiar a true en producción
spring.thymeleaf.encoding=UTF-8

# ── Servidor ───────────────────────────────────────────────────────
server.port=${SERVER_PORT:8080}
```

Ver `application.properties.example` para la lista completa de variables a definir en cada entorno.

---

## Inyección de Dependencias

Spring Boot gestiona automáticamente la inyección via `@Autowired`. El flujo de dependencias es:

```
Controller
    └── @Autowired Service(s)
              └── @Autowired Repository / JdbcTemplate
                        └── DataSource (PostgreSQL — pool HikariCP)
```

Ejemplo en `InvoiceController`:
```java
@Autowired private InvoiceService    invoiceService;
@Autowired private CustomerService   customerService;
@Autowired private EmpresaService    empresaService;
@Autowired private MetodoPagoService metodoPagoService;
@Autowired private PagoFacturaService pagoFacturaService;
@Autowired private ProductoService   productoService;
@Autowired private PdfService        pdfService;
```

---

## Dependencias Clave del `pom.xml`

| Dependencia | Versión | Rol |
|---|---|---|
| `spring-boot-starter-web` | 3.3.5 | MVC, DispatcherServlet, servidor Tomcat embebido |
| `spring-boot-starter-thymeleaf` | 3.3.5 | Motor de plantillas HTML |
| `thymeleaf-layout-dialect` | auto | Herencia de templates (`layout:decorate`) |
| `spring-boot-starter-data-jpa` | 3.3.5 | JPA / Hibernate 6 para CRUD automático |
| `spring-boot-starter-jdbc` | 3.3.5 | JdbcTemplate para SQL directo |
| `postgresql` | runtime | Conector JDBC para PostgreSQL |
| `lombok` | optional | `@Data` genera getters/setters en entidades |
| `itext7:kernel` + `itext7:layout` | 7.2.5 | Generación de PDF con iText7 |

---

## Rutas de la Aplicación (Resumen)

| Método | URL | Controlador | Acción |
|---|---|---|---|
| GET | `/` | `HomeController` | Dashboard con KPIs |
| GET | `/invoice` | `InvoiceController` | Lista todas las facturas |
| GET | `/invoice/new` | `InvoiceController` | Formulario nueva factura |
| POST | `/invoice/new` | `InvoiceController` | Crear factura + líneas de detalle |
| GET | `/invoice/{id}` | `InvoiceController` | Ver detalle factura |
| POST | `/invoice/{id}/status` | `InvoiceController` | Actualizar estado de pago |
| GET | `/invoice/{id}/pdf` | `InvoiceController` | Descargar PDF |
| GET | `/invoice/delete/{id}` | `InvoiceController` | Eliminar factura |
| GET/POST | `/customer/**` | `CustomerController` | CRUD clientes |
| GET/POST | `/products/product/**` | `ProductController` | CRUD productos |
| GET/POST | `/users/**` | `UserController` | CRUD usuarios |
| GET/POST | `/branches/**` | `BranchController` | CRUD empresas |
| GET/POST | `/payment_methods/**` | `PaymentMethodController` | CRUD métodos de pago |
| GET/POST | `/discount/**` | `DiscountController` | CRUD descuentos |
| GET/POST | `/invoice_taxes/**` | `TaxController` | CRUD impuestos |
| GET/POST | `/invoice_payments/**` | `InvoicePaymentController` | CRUD estados de pago |
| GET | `/logs` | `LogController` | Ver logs del sistema |
| GET | `/product_discounts` | `ProductDiscountController` | Ver relaciones producto-descuento |
| GET | `/settings` | `HomeController` | Configuración general |

---

## Consideraciones de Seguridad y Estabilidad

- **Sin secretos en el repositorio** — `application.properties` usa variables de entorno (`${VAR:default}`). Las credenciales reales se inyectan en runtime y nunca se rastrean en Git. Ver `application.properties.example` para referencia.
- **`ddl-auto=validate`** — Hibernate solo valida el schema al arrancar. Si hay diferencia entre la entidad Java y la tabla real, la aplicación falla al iniciar (comportamiento deseado: falla rápido, no silencioso).
- **No hay autenticación activa** — la aplicación asume un entorno de red local/intranet. Si se expone a internet, debe añadirse Spring Security.
- **Pool de conexiones** — Spring Boot usa HikariCP automáticamente (incluido con `spring-boot-starter-data-jpa`). No requiere configuración manual.
- **Transacciones** — las operaciones de creación de factura (INSERT facturas + múltiples INSERT detalle_factura) no están envueltas en `@Transactional` explícitamente. Para producción se recomienda añadir `@Transactional` en `InvoiceService.createInvoice()`.
- **Thymeleaf 3.1** — prohíbe expresiones de cadena en atributos de eventos DOM (`th:onclick`). El proyecto usa `th:data-*` + `onclick` nativo como alternativa compatible y segura.

---

*Factugest — Arquitectura Técnica | Java 21 + Spring Boot 3.3.5 + PostgreSQL*
