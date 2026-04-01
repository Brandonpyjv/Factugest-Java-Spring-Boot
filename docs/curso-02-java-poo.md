# Curso 02 — Java con Programación Orientada a Objetos (POO)
### Para el equipo de Factugest | Clases, objetos y arquitectura real del proyecto

---

## Introducción

La Programación Orientada a Objetos (POO) es el paradigma sobre el que está construido todo Factugest. Cada entidad del sistema —`Cliente`, `Producto`, `Factura`, `Usuario`— es una clase. Entender POO es entender cómo están modelados los datos del proyecto.

En este curso verás los 4 pilares de POO con ejemplos directamente sacados (o inspirados) del código de Factugest.

> **Prerequisito:** Haber completado el Curso 01.

---

## Índice

1. Clases y Objetos
2. Atributos y Métodos de Instancia
3. Constructores
4. Encapsulamiento (getters, setters y Lombok)
5. Herencia
6. Interfaces
7. Polimorfismo
8. Clases Abstractas
9. Colecciones tipadas — `List<T>` y `Map<K,V>`
10. Optional
11. Enums
12. Ejercicios de Cierre

---

## Sección 1 — Clases y Objetos

### 1.1 ¿Qué es una clase?

Una **clase** es un molde o plantilla que define cómo es un "tipo de cosa". Define qué **datos** tiene (atributos) y qué **acciones** puede hacer (métodos).

### 1.2 ¿Qué es un objeto?

Un **objeto** es una instancia concreta de una clase. Puedes crear muchos objetos del mismo tipo.

```java
// La CLASE es el molde
class Cliente {
    String nombre;
    String correo;
    String tipoDocumento;
}

// El OBJETO es una instancia concreta
Cliente cliente1 = new Cliente();
cliente1.nombre = "Carlos López";
cliente1.correo = "carlos@ejemplo.com";
cliente1.tipoDocumento = "CC";

Cliente cliente2 = new Cliente();
cliente2.nombre = "Empresa XYZ S.A.S";
cliente2.tipoDocumento = "NIT";
```

### 1.3 Así se ve en Factugest — la entidad Customer

```java
// src/main/java/com/factugest/entity/Customer.java
@Entity
@Table(name = "customers")
@Data
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Integer customerId;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "document_type")
    private String documentType;

    @Column(name = "document_number")
    private String documentNumber;

    @Column(name = "tipo_persona")
    private String tipoPersona;

    @Column(name = "regimen_tributario")
    private String regimenTributario;
}
```

Cada vez que un cliente nuevo se guarda en la base de datos, se crea un **objeto** `Customer` con sus datos y Spring JPA lo convierte en una fila en la tabla `customers`.

---

## Sección 2 — Atributos y Métodos de Instancia

### 2.1 Atributos

Son las variables que pertenecen a cada objeto. Cada objeto tiene sus propios valores:

```java
class Producto {
    String nombre;
    String sku;
    BigDecimal precioUnitario;
    int stock;
    boolean activo;
}
```

### 2.2 Métodos de instancia

Son acciones que puede realizar un objeto. Tienen acceso directo a los atributos del mismo objeto:

```java
class Producto {
    String nombre;
    BigDecimal precioUnitario;
    int stock;
    boolean activo;

    // Método que usa los atributos del objeto
    public boolean estaDisponible() {
        return activo && stock > 0;
    }

    public BigDecimal calcularSubtotal(int cantidad) {
        return precioUnitario.multiply(new BigDecimal(cantidad));
    }

    public void mostrarInfo() {
        System.out.println("Producto: " + nombre);
        System.out.println("Precio: $" + precioUnitario);
        System.out.println("Stock: " + stock);
        System.out.println("Disponible: " + (estaDisponible() ? "Sí" : "No"));
    }
}
```

**Usando la clase:**

```java
Producto laptop = new Producto();
laptop.nombre = "Laptop Dell";
laptop.precioUnitario = new BigDecimal("2500000.00");
laptop.stock = 5;
laptop.activo = true;

laptop.mostrarInfo();

BigDecimal subtotal = laptop.calcularSubtotal(2);
System.out.println("Subtotal (2 unidades): $" + subtotal); // $5,000,000.00
```

### Ejercicio 2.1
Crea una clase `DetalleFactura` con los atributos:
- `nombreProducto` (String)
- `precioUnitario` (BigDecimal)
- `cantidad` (int)
- `porcentajeImpuesto` (BigDecimal)
- `porcentajeDescuento` (BigDecimal)

Agrega métodos:
- `calcularSubtotal()` → precio × cantidad
- `calcularValorDescuento()` → subtotal × (descuento / 100)
- `calcularValorImpuesto()` → (subtotal - descuento) × (impuesto / 100)
- `calcularTotal()` → subtotal - descuento + impuesto
- `mostrarDetalle()` → imprime toda la información

---

## Sección 3 — Constructores

Un **constructor** es un método especial que se llama automáticamente cuando creas un objeto con `new`. Se usa para inicializar los atributos desde el principio.

### 3.1 Constructor por defecto

Si no defines ninguno, Java crea uno vacío automáticamente:

```java
class Cliente {
    String nombre;
    // Constructor implícito: public Cliente() {}
}

Cliente c = new Cliente(); // Llama al constructor vacío
```

### 3.2 Constructor con parámetros

```java
class Cliente {
    String nombre;
    String correo;
    String tipoDocumento;

    // Constructor
    public Cliente(String nombre, String correo, String tipoDocumento) {
        this.nombre = nombre;          // this.nombre = atributo de la clase
        this.correo = correo;          // correo (sin this) = parámetro del constructor
        this.tipoDocumento = tipoDocumento;
    }
}

// Crear objetos con el constructor
Cliente c1 = new Cliente("Carlos López", "carlos@mail.com", "CC");
Cliente c2 = new Cliente("Empresa XYZ", "info@xyz.com", "NIT");
```

### 3.3 Múltiples constructores (sobrecarga)

```java
class Producto {
    String nombre;
    BigDecimal precio;
    int stock;

    // Constructor mínimo
    public Producto(String nombre, BigDecimal precio) {
        this.nombre = nombre;
        this.precio = precio;
        this.stock = 0; // valor por defecto
    }

    // Constructor completo
    public Producto(String nombre, BigDecimal precio, int stock) {
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
    }
}
```

### 3.4 Lombok — la forma en que Factugest elimina el código repetitivo

En Factugest, las entidades usan **Lombok** con `@Data`. Esta anotación genera automáticamente:
- Getters para todos los atributos
- Setters para todos los atributos
- Constructor vacío
- `toString()`, `equals()`, `hashCode()`

```java
import lombok.Data;

@Data
class Cliente {
    private Integer customerId;
    private String fullName;
    private String documentType;

    // Lombok genera automáticamente:
    // getCustomerId(), setCustomerId(Integer)
    // getFullName(), setFullName(String)
    // getDocumentType(), setDocumentType(String)
    // toString(), equals(), hashCode()
}

// Uso con los getters/setters de Lombok
Cliente c = new Cliente();
c.setFullName("Carlos López");
System.out.println(c.getFullName()); // "Carlos López"
```

---

## Sección 4 — Encapsulamiento

El encapsulamiento protege los datos de un objeto al marcarlos como `private` y exponerlos solo a través de métodos `get`/`set` controlados.

### 4.1 Sin encapsulamiento (peligroso)

```java
class Producto {
    public int stock; // ¡Cualquiera puede cambiar el stock!
}

Producto p = new Producto();
p.stock = -999; // Esto no debería ser posible
```

### 4.2 Con encapsulamiento (correcto)

```java
class Producto {
    private int stock; // Solo accesible desde dentro de la clase

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        if (stock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
        this.stock = stock;
    }
}

Producto p = new Producto();
p.setStock(10);  // OK
p.setStock(-5);  // Lanza excepción — protegido!
```

### 4.3 Modificadores de acceso

| Modificador | Visible desde |
|-------------|---------------|
| `private` | Solo dentro de la misma clase |
| `protected` | Dentro de la clase y sus subclases |
| `public` | Desde cualquier lugar |
| (sin modificador) | Solo dentro del mismo paquete |

En Factugest, todos los atributos de las entidades son `private` y se accede a ellos con getters/setters (generados por Lombok `@Data`).

### Ejercicio 4.1
Crea una clase `CuentaUsuario` con:
- `private String correo`
- `private String contrasena` (solo setter, sin getter por seguridad)
- `private int intentosFallidos`
- `private boolean bloqueada`

Con los métodos:
- `setContrasena(String nueva)` → valida que tenga mínimo 8 caracteres
- `registrarIntentoFallido()` → suma 1 y bloquea si llega a 3
- `desbloquear()` → reinicia intentos y desbloquea
- `getCorreo()`, `isBloqueada()`, `getIntentosFallidos()`

---

## Sección 5 — Herencia

La herencia permite que una clase **herede** atributos y métodos de otra. Se usa para evitar repetir código y modelar relaciones del tipo "es un".

### 5.1 extends

```java
// Clase padre (superclase)
class Persona {
    protected String nombre;
    protected String correo;

    public void saludar() {
        System.out.println("Hola, soy " + nombre);
    }
}

// Clase hija (subclase) hereda todo de Persona
class Usuario extends Persona {
    private String rol;
    private String contrasena;

    public void iniciarSesion() {
        System.out.println(nombre + " ha iniciado sesión con rol: " + rol);
    }
}

// Clase hija también puede heredar de Persona
class Cliente extends Persona {
    private String tipoDocumento;
    private String numeroDocumento;
}
```

**Uso:**

```java
Usuario u = new Usuario();
u.nombre = "Brandon";    // Heredado de Persona
u.correo = "b@mail.com"; // Heredado de Persona
u.saludar();             // Método heredado de Persona → "Hola, soy Brandon"
u.iniciarSesion();       // Método propio de Usuario
```

### 5.2 super — llamar al constructor del padre

```java
class Persona {
    protected String nombre;

    public Persona(String nombre) {
        this.nombre = nombre;
    }
}

class Usuario extends Persona {
    private String rol;

    public Usuario(String nombre, String rol) {
        super(nombre); // Llama al constructor de Persona
        this.rol = rol;
    }
}

Usuario u = new Usuario("Brandon", "ADMIN");
System.out.println(u.nombre); // "Brandon"
```

### 5.3 Sobreescritura de métodos (@Override)

Una subclase puede redefinir un método del padre:

```java
class Persona {
    public String getDescripcion() {
        return "Soy una persona llamada " + nombre;
    }
}

class Usuario extends Persona {
    @Override
    public String getDescripcion() {
        return "Soy el usuario " + nombre + " con rol " + rol;
    }
}
```

### 5.4 Herencia en Spring Security — CustomUserPrincipal

En Factugest, `CustomUserPrincipal` extiende la clase `User` de Spring Security para añadirle el nombre y rol del usuario autenticado:

```java
// security/CustomUserPrincipal.java
public class CustomUserPrincipal extends User {
    // Atributos adicionales propios de Factugest
    private final String nombre;
    private final String rol;

    public CustomUserPrincipal(String username, String password,
                                Collection<? extends GrantedAuthority> authorities,
                                String nombre, String rol) {
        super(username, password, authorities); // Llama al constructor de User
        this.nombre = nombre;
        this.rol = rol;
    }

    public String getNombre() { return nombre; }
    public String getRol() { return rol; }
}
```

### Ejercicio 5.1
Crea una jerarquía:
- Clase `EntidadBase` con atributos: `id` (Integer), `fechaCreacion` (String), `activo` (boolean)
- Clase `Producto` que extienda `EntidadBase` y añada: `nombre`, `sku`, `precioUnitario`, `stock`
- Clase `ProductoDigital` que extienda `Producto` y añada: `urlDescarga`, `licencias`

Agrega un método `getDescripcion()` en cada clase con `@Override`, mostrando los datos relevantes de cada nivel.

---

## Sección 6 — Interfaces

Una **interfaz** define un **contrato**: declara qué métodos debe tener una clase, pero no cómo los implementa. Una clase puede implementar múltiples interfaces.

### 6.1 Definición e implementación

```java
// Interfaz — define el contrato
interface Calculable {
    BigDecimal calcularTotal();
    BigDecimal calcularImpuesto();
}

// Clase que cumple el contrato
class Factura implements Calculable {

    private BigDecimal subtotal;
    private BigDecimal porcentajeIva;

    @Override
    public BigDecimal calcularTotal() {
        return subtotal.add(calcularImpuesto());
    }

    @Override
    public BigDecimal calcularImpuesto() {
        return subtotal.multiply(porcentajeIva)
                       .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }
}
```

### 6.2 Múltiples interfaces

```java
interface Guardable {
    void guardar();
    void eliminar();
}

interface Exportable {
    byte[] exportarPdf();
    String exportarCsv();
}

class Factura implements Calculable, Guardable, Exportable {
    // Debe implementar TODOS los métodos de AMBAS interfaces
}
```

### 6.3 Interfaces en Spring — Repository

La parte más importante de las interfaces en Factugest son los **repositories**. Cada uno es una interfaz que **extiende** `JpaRepository`, y Spring genera la implementación automáticamente:

```java
// repository/CustomerRepository.java
public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    // Spring JPA genera automáticamente:
    // findAll() → SELECT * FROM customers
    // findById(id) → SELECT * FROM customers WHERE customer_id = ?
    // save(customer) → INSERT o UPDATE
    // deleteById(id) → DELETE

    // Puedes declarar métodos personalizados sin escribir SQL:
    List<Customer> findByFullNameContainingIgnoreCase(String nombre);
    // Spring genera: SELECT * FROM customers WHERE LOWER(full_name) LIKE LOWER('%nombre%')
}
```

### 6.4 Interfaces como tipo de dato

Las interfaces pueden usarse como tipo, lo que permite intercambiar implementaciones fácilmente:

```java
// La variable puede apuntar a cualquier clase que implemente la interfaz
Calculable doc = new Factura();
// o
Calculable doc = new Cotizacion(); // Si Cotizacion también implementa Calculable
```

### Ejercicio 6.1
Crea una interfaz `Auditable` con los métodos:
- `String getAccion()` → devuelve la acción realizada ("CREAR", "EDITAR", "ELIMINAR")
- `String getDescripcionAuditoria()` → descripción legible

Implementa `Auditable` en una clase `RegistroLog` que tenga:
- `codUsuario` (Integer)
- `modulo` (String, por ejemplo "CLIENTES")
- `accion` (String)
- `descripcion` (String)
- `fecha` (String)

---

## Sección 7 — Polimorfismo

El polimorfismo permite tratar objetos de diferentes clases de manera uniforme cuando comparten una interfaz o clase padre.

### 7.1 Polimorfismo con herencia

```java
class Documento {
    String codigo;

    public String getTipo() {
        return "Documento genérico";
    }

    public void mostrar() {
        System.out.println("[" + getTipo() + "] Código: " + codigo);
    }
}

class Factura extends Documento {
    @Override
    public String getTipo() { return "FACTURA"; }
}

class Cotizacion extends Documento {
    @Override
    public String getTipo() { return "COTIZACIÓN"; }
}

// Polimorfismo: la lista puede tener tipos diferentes
List<Documento> documentos = new ArrayList<>();
documentos.add(new Factura());
documentos.add(new Cotizacion());
documentos.add(new Factura());

// Cada objeto llama a SU propia versión de getTipo()
for (Documento doc : documentos) {
    doc.mostrar();
    // Factura → "[FACTURA] Código: ..."
    // Cotizacion → "[COTIZACIÓN] Código: ..."
}
```

### 7.2 Polimorfismo con interfaces — el patrón Servicio de Factugest

En Factugest, los **servicios** son inyectados como interfaces en los controllers. Esto permite que si se cambia la implementación del servicio, el controller no cambia:

```java
// Interfaz (contrato)
interface IProductoService {
    List<Producto> getAll();
    Producto getById(Integer id);
    Producto save(Producto p);
    void delete(Integer id);
}

// Implementación concreta
@Service
class ProductoService implements IProductoService {
    // ...implementación con JPA...
}

// El controller solo conoce la interfaz, no la implementación
@Controller
class ProductController {
    private final IProductoService productoService; // tipo interfaz

    // Si mañana cambias la implementación, este código no cambia
    public ProductController(IProductoService productoService) {
        this.productoService = productoService;
    }
}
```

---

## Sección 8 — Clases Abstractas

Una clase abstracta es un "intermedio" entre una clase normal y una interfaz:
- Puede tener atributos con valores
- Puede tener métodos con implementación (completos)
- Puede tener métodos abstractos (sin implementación, obligatorios en las subclases)
- **No se puede instanciar directamente** — solo se puede usar a través de subclases

```java
abstract class DocumentoComercial {
    // Atributos comunes a todos los documentos
    protected String codigo;
    protected String fechaEmision;
    protected Integer codCliente;
    protected BigDecimal total;

    // Método concreto (implementado — se hereda tal cual)
    public String getEncabezado() {
        return "Documento: " + codigo + " | Fecha: " + fechaEmision;
    }

    // Método abstracto — CADA subclase DEBE implementarlo a su manera
    public abstract String calcularTotales();
    public abstract String getTipoDocumento();
}

class Factura extends DocumentoComercial {
    private BigDecimal iva;

    @Override
    public String calcularTotales() {
        return "Subtotal + IVA (" + iva + "%) = " + total;
    }

    @Override
    public String getTipoDocumento() { return "FACTURA DE VENTA"; }
}

class NotaCredito extends DocumentoComercial {
    private String motivoDevolucion;

    @Override
    public String calcularTotales() {
        return "Valor a devolver: " + total;
    }

    @Override
    public String getTipoDocumento() { return "NOTA CRÉDITO"; }
}
```

### Diferencia: Clase Abstracta vs Interfaz

| | Clase Abstracta | Interfaz |
|-|-----------------|----------|
| Instanciable | No | No |
| Atributos con estado | Sí | No (solo constantes) |
| Métodos concretos | Sí | Solo con `default` |
| Herencia | `extends` (solo una) | `implements` (múltiples) |
| Uso ideal | Jerarquía con lógica compartida | Contrato de comportamiento |

---

## Sección 9 — Colecciones tipadas

### 9.1 `List<T>`

Ya visto en el Curso 01, pero ahora con objetos propios:

```java
List<Customer> clientes = new ArrayList<>();
clientes.add(new Customer());
clientes.get(0);
clientes.size();
clientes.isEmpty();
clientes.remove(0);
```

**Iterar y filtrar con Stream (Java 8+):**

```java
List<Producto> productos = productoService.getAll();

// Filtrar solo los activos
List<Producto> activos = productos.stream()
    .filter(p -> p.isActivo() && p.getStock() > 0)
    .collect(Collectors.toList());

// Obtener solo los nombres
List<String> nombres = productos.stream()
    .map(Producto::getNombre)
    .collect(Collectors.toList());

// Calcular el total del inventario
BigDecimal totalInventario = productos.stream()
    .map(p -> p.getPrecioUnitario().multiply(new BigDecimal(p.getStock())))
    .reduce(BigDecimal.ZERO, BigDecimal::add);
```

### 9.2 `Map<K, V>`

Un mapa guarda pares clave-valor. Útil cuando quieres acceso rápido por un identificador:

```java
Map<Integer, String> productosMap = new HashMap<>();

// Agregar (put)
productosMap.put(1, "Laptop");
productosMap.put(2, "Mouse");
productosMap.put(3, "Teclado");

// Obtener (get)
String producto = productosMap.get(2); // "Mouse"

// Verificar si existe
boolean existe = productosMap.containsKey(5); // false

// Recorrer
for (Map.Entry<Integer, String> entry : productosMap.entrySet()) {
    System.out.println("ID " + entry.getKey() + ": " + entry.getValue());
}
```

**En Factugest:** Los resultados del dashboard usan un `Map<String, Object>` para enviar estadísticas a la vista:

```java
// En InvoiceService (simplificado)
Map<String, Object> stats = new HashMap<>();
stats.put("totalFacturas", 45);
stats.put("totalClientes", 23);
stats.put("ventasMes", new BigDecimal("15600000"));
return stats;
```

### Ejercicio 9.1
Tienes una lista de objetos `Producto` (usa la clase que creaste antes). Escribe métodos que:
1. Retornen solo los productos con `stock < 5`
2. Creen un `Map<String, BigDecimal>` donde la clave es el SKU y el valor es el precio
3. Calculen el valor total del inventario (precio × stock para cada producto)
4. Encuentren el producto con mayor stock

---

## Sección 10 — Optional

`Optional<T>` evita los `NullPointerException`. En lugar de retornar `null`, retornas un `Optional` que puede o no contener un valor.

### 10.1 Uso básico

```java
import java.util.Optional;

// Sin Optional — peligroso
Customer buscarCliente(Integer id) {
    return null; // Si no existe, quien llame puede fallar con NullPointerException
}

// Con Optional — seguro
Optional<Customer> buscarCliente(Integer id) {
    // retorna Optional vacío si no existe
    return Optional.empty();
    // o retorna Optional con valor si existe
    // return Optional.of(cliente);
}
```

### 10.2 Cómo usarlo

```java
Optional<Customer> resultado = customerRepository.findById(5);

// Opción 1: verificar y obtener
if (resultado.isPresent()) {
    Customer c = resultado.get();
    System.out.println(c.getFullName());
} else {
    System.out.println("Cliente no encontrado");
}

// Opción 2: con orElse (valor por defecto si está vacío)
Customer c = resultado.orElse(new Customer());

// Opción 3: con orElseThrow (lanza excepción si está vacío)
Customer c2 = resultado.orElseThrow(() ->
    new RuntimeException("Cliente no encontrado con ese ID"));
```

### 10.3 En Factugest — CustomerController

```java
// CustomerController.java (simplificado)
@GetMapping("/customer/{id}")
public String verCliente(@PathVariable Integer id, Model model) {
    Optional<Customer> cliente = customerService.getById(id);

    if (cliente.isPresent()) {
        model.addAttribute("customer", cliente.get());
        return "customer/detalle"; // Muestra la vista
    } else {
        return "redirect:/customer"; // Redirige si no existe
    }
}
```

---

## Sección 11 — Enums

Un **enum** (enumeración) es un tipo que solo puede tener un conjunto fijo de valores. Perfecto para estados, roles, tipos, etc.

### 11.1 Definición

```java
enum RolUsuario {
    ADMIN,
    CAJERO,
    SUPERVISOR
}

enum TipoDocumento {
    CC,
    NIT,
    CE,
    PASAPORTE
}

enum EstadoFactura {
    PENDIENTE,
    PAGADA,
    ANULADA
}
```

### 11.2 Uso

```java
RolUsuario rol = RolUsuario.ADMIN;

if (rol == RolUsuario.ADMIN) {
    System.out.println("Acceso completo");
}

// Con switch
switch (rol) {
    case ADMIN:
        System.out.println("Ver todo");
        break;
    case CAJERO:
        System.out.println("Solo facturas");
        break;
}
```

### 11.3 Enum con atributos y métodos

```java
enum TipoPersona {
    NATURAL("Persona Natural", "Simple"),
    JURIDICA("Persona Jurídica", "Ordinario");

    private final String descripcion;
    private final String regimenDefault;

    TipoPersona(String descripcion, String regimenDefault) {
        this.descripcion = descripcion;
        this.regimenDefault = regimenDefault;
    }

    public String getDescripcion() { return descripcion; }
    public String getRegimenDefault() { return regimenDefault; }
}

TipoPersona tipo = TipoPersona.JURIDICA;
System.out.println(tipo.getDescripcion());    // "Persona Jurídica"
System.out.println(tipo.getRegimenDefault()); // "Ordinario"
```

---

## Sección 12 — Ejercicios de Cierre

### Ejercicio Final 1 — Modelo completo de Factugest

Crea las siguientes clases **sin usar Lombok** (escribe los getters y setters manualmente para practicar):

**`Cliente`**
- `customerId` (Integer)
- `fullName` (String)
- `documentType` (String — puede ser un enum)
- `documentNumber` (String)
- `tipoPersona` (String)
- `regimenTributario` (String)
- Método: `getDescripcion()` → "NIT: 123456789 | Carlos López | Natural | Simple"

**`Producto`**
- `codProducto` (Integer)
- `sku` (String)
- `nombre` (String)
- `precioUnitario` (BigDecimal)
- `stock` (Integer)
- `activo` (boolean)
- Método: `estaDisponible()` → activo && stock > 0

**`DetalleFactura`**
- `codDetalle` (Integer)
- `producto` (Producto) ← objeto Producto, no solo el ID
- `cantidad` (int)
- `descuentoPorcentaje` (BigDecimal)
- `impuestoPorcentaje` (BigDecimal)
- Métodos: `calcularSubtotal()`, `calcularTotal()`

**`Factura`**
- `codFactura` (Integer)
- `fecha` (String)
- `cliente` (Cliente) ← objeto Cliente
- `detalles` (List\<DetalleFactura\>)
- Métodos:
  - `agregarDetalle(DetalleFactura d)`
  - `calcularTotal()` → suma los totales de todos los detalles
  - `mostrarResumen()` → imprime la factura completa

---

### Ejercicio Final 2 — Sistema de logs simple

Basado en el módulo de logs de Factugest (`LogService`, entidad `Log`), crea:

1. Una interfaz `IRegistrable` con el método `registrarAccion(String accion, String descripcion)`
2. Una clase abstracta `ModuloBase` que implemente `IRegistrable` y tenga:
   - `nombreModulo` (String)
   - Una lista interna de logs
   - Implementación de `registrarAccion` que agrega a la lista
   - Método `verLogs()` que imprime todos los registros
3. Una clase `ModuloClientes` que extienda `ModuloBase` y tenga operaciones como `crearCliente()`, `editarCliente()`, `eliminarCliente()`, cada una registrando su acción automáticamente.

---

### Ejercicio Final 3 — Repositorio en memoria

Simula un repositorio sin base de datos usando colecciones:

Crea una clase `ProductoRepositorySimulado` con:
- Una lista interna `List<Producto>` con productos de ejemplo
- Métodos:
  - `findAll()` → retorna todos
  - `findById(Integer id)` → retorna `Optional<Producto>`
  - `findByActivo(boolean activo)` → retorna lista filtrada
  - `save(Producto p)` → agrega si es nuevo, actualiza si ya existe (comparar por id)
  - `deleteById(Integer id)` → elimina por id
  - `countByActivoTrue()` → cuenta activos

---

## Resumen del Curso 02

| Pilar POO | Concepto | Ejemplo en Factugest |
|-----------|----------|----------------------|
| **Encapsulamiento** | `private` + getters/setters | Todas las entidades con `@Data` |
| **Herencia** | `extends` | `CustomUserPrincipal extends User` |
| **Interfaces** | `implements` | `CustomerRepository extends JpaRepository` |
| **Polimorfismo** | Mismo método, comportamiento diferente | Método `@Override` en subclases |
| **Clases abstractas** | Plantilla parcial | Base para documentos comerciales |
| **Optional** | Evitar null | `customerRepository.findById(id)` |
| **Enums** | Valores fijos | Roles de usuario (`ADMIN`, `CAJERO`) |

---

## Siguiente paso

Con este curso puedes leer y entender todas las entidades del proyecto (`Customer`, `Factura`, `Producto`, etc.) y cómo los repositorios y servicios trabajan con ellas. El siguiente paso es el **Curso 03 — Spring Boot y Spring Security**, donde aprenderás cómo todo esto se conecta para formar una aplicación web completa.

---

*Factugest — Equipo de Desarrollo*
