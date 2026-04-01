# Curso 01 — Java Básico
### Para el equipo de Factugest | Desde cero hasta entender el código del proyecto

---

## Bienvenida

Este curso está pensado para personas que nunca han programado en Java o que vienen de otro lenguaje. La idea es que al final de este curso puedas leer y entender el código del proyecto Factugest, y escribir lógica básica por tu cuenta.

Cada sección tiene **explicación**, **ejemplos reales** y **ejercicios para practicar**.

> **¿Qué es Java?**
> Java es un lenguaje de programación de propósito general, fuertemente tipado y orientado a objetos. Es el lenguaje base sobre el que está construido Factugest. Se ejecuta en la JVM (Java Virtual Machine), lo que significa que el mismo código funciona en Windows, Linux o Mac.

---

## Índice

1. Tu primer programa en Java
2. Variables y tipos de datos
3. Operadores
4. Condicionales (if / else / switch)
5. Ciclos (for, while, for-each)
6. Arrays y listas
7. Métodos (funciones)
8. Manejo de excepciones
9. Ejercicios de cierre

---

## Sección 1 — Tu primer programa en Java

Todo programa en Java empieza con una **clase** y un método **main**. El método main es el punto de entrada: cuando ejecutas el programa, Java busca ese método y empieza a correr desde ahí.

```java
public class HolaMundo {
    public static void main(String[] args) {
        System.out.println("Hola, Factugest!");
    }
}
```

**Desglose línea por línea:**

| Línea | Significado |
|-------|-------------|
| `public class HolaMundo` | Define una clase pública llamada HolaMundo. El nombre del archivo debe ser `HolaMundo.java` |
| `public static void main(String[] args)` | Método principal, punto de entrada. Java lo llama automáticamente |
| `System.out.println(...)` | Imprime texto en consola seguido de un salto de línea |

> En Factugest, la clase de entrada es `FactugestApplication.java`. Cuando ejecutas la aplicación, Spring Boot arranca desde ese `main`.

### Ejercicio 1.1
Crea un programa que imprima en consola:
- Tu nombre
- El módulo del proyecto en el que trabajarás (por ejemplo: "Módulo de Facturación")
- El año actual

---

## Sección 2 — Variables y tipos de datos

Una **variable** es un espacio en memoria con un nombre donde guardas información. En Java debes declarar el **tipo** antes del nombre.

### 2.1 Tipos primitivos

```java
int edad = 25;               // Número entero (sin decimales)
double precio = 19.99;       // Número con decimales
boolean activo = true;       // Verdadero o falso
char inicial = 'B';          // Un solo carácter
```

### 2.2 Tipos de referencia (objetos)

```java
String nombre = "Factugest";         // Texto (cadena de caracteres)
Integer cantidad = null;             // int que puede ser null (útil en bases de datos)
Double total = null;                 // double que puede ser null
```

> **¿Por qué `Integer` con mayúscula en Factugest?**
> En el proyecto verás cosas como `private Integer codCliente`. Los tipos con mayúscula (`Integer`, `Double`) pueden ser `null`, lo que es importante cuando una factura aún no tiene cliente asignado. El tipo primitivo `int` nunca puede ser `null` y causaría un error.

### 2.3 BigDecimal — para dinero

En Factugest, **todos los valores monetarios usan `BigDecimal`**, nunca `double`. Esto es porque `double` tiene errores de precisión en cálculos decimales.

```java
import java.math.BigDecimal;

// MAL: puede dar resultados incorrectos
double precio1 = 0.1 + 0.2;   // Resultado: 0.30000000000000004 !!

// BIEN: precisión exacta
BigDecimal precio2 = new BigDecimal("0.10");
BigDecimal precio3 = new BigDecimal("0.20");
BigDecimal total = precio2.add(precio3);   // Resultado: 0.30 exacto
```

**Operaciones con BigDecimal:**

```java
BigDecimal a = new BigDecimal("100.00");
BigDecimal b = new BigDecimal("15.50");

BigDecimal suma       = a.add(b);           // 115.50
BigDecimal resta      = a.subtract(b);      // 84.50
BigDecimal producto   = a.multiply(b);      // 1550.00
BigDecimal cociente   = a.divide(b, 2, RoundingMode.HALF_UP); // 6.45 (redondeado a 2 decimales)
```

Así se ve en una entidad de Factugest:

```java
// Entidad Factura.java (simplificado)
private BigDecimal subtotal;
private BigDecimal totalImpuestos;
private BigDecimal totalDescuentos;
private BigDecimal total;
```

### 2.4 Declaración de variables — reglas

```java
// Válido
int cantidad = 5;
String sku = "PROD-001";

// Inválido — no se puede cambiar el tipo después
int cantidad = 5;
cantidad = "cinco";  // ERROR: tipos incompatibles

// Constantes con final (no se pueden reasignar)
final int MAX_ITEMS = 100;
```

### Ejercicio 2.1
Declara variables para representar un producto de Factugest:
- Nombre del producto (texto)
- Precio unitario (usa BigDecimal con valor "25000.00")
- Stock disponible (número entero)
- Si está activo (booleano)
- Código del producto (entero que puede ser null)

Luego imprímelas todas con `System.out.println`.

---

## Sección 3 — Operadores

### 3.1 Aritméticos

```java
int a = 10, b = 3;

int suma      = a + b;   // 13
int resta     = a - b;   // 7
int producto  = a * b;   // 30
int division  = a / b;   // 3  (división entera, se pierde el .333)
int modulo    = a % b;   // 1  (residuo de la división)
```

### 3.2 Comparación (devuelven boolean)

```java
int stock = 5;

boolean igualA5    = (stock == 5);   // true
boolean diferente  = (stock != 10);  // true
boolean mayor      = (stock > 3);    // true
boolean menor      = (stock < 10);   // true
boolean mayorIgual = (stock >= 5);   // true
boolean menorIgual = (stock <= 5);   // true
```

### 3.3 Lógicos

```java
boolean activo = true;
boolean tieneStock = stock > 0;

boolean mostrarProducto = activo && tieneStock;  // AND: ambos deben ser true
boolean eliminar        = !activo || stock == 0; // OR: al menos uno true; NOT invierte
```

### 3.4 Asignación compuesta

```java
int total = 100;
total += 50;   // total = total + 50 → 150
total -= 20;   // total = total - 20 → 130
total *= 2;    // total = total * 2  → 260
total /= 4;    // total = total / 4  → 65
```

### Ejercicio 3.1
Tienes un producto con precio `25000` y cantidad `3`. Calcula:
1. El subtotal (precio × cantidad)
2. El IVA al 19% del subtotal
3. El total (subtotal + IVA)
4. Si el total supera `100000`, imprime "Compra grande"; si no, "Compra normal"

---

## Sección 4 — Condicionales

### 4.1 if / else if / else

```java
int stock = 3;

if (stock > 10) {
    System.out.println("Stock suficiente");
} else if (stock > 0) {
    System.out.println("Stock bajo — reabastecer pronto");
} else {
    System.out.println("Sin stock — no se puede vender");
}
```

### 4.2 Ejemplo de Factugest — tipo de persona de un cliente

En Factugest, los clientes pueden ser `"Natural"` o `"Juridica"`. Dependiendo del tipo, el régimen tributario cambia:

```java
String tipoPersona = "Natural";
String regimenTributario;

if (tipoPersona.equals("Natural")) {
    regimenTributario = "Simple";
} else if (tipoPersona.equals("Juridica")) {
    regimenTributario = "Ordinario";
} else {
    regimenTributario = "No clasificado";
}

System.out.println("Régimen: " + regimenTributario);
```

> **Importante:** Para comparar `String` en Java **nunca** uses `==`. Usa siempre `.equals()`.
> ```java
> // MAL
> if (tipoPersona == "Natural") { ... }
>
> // BIEN
> if (tipoPersona.equals("Natural")) { ... }
> ```

### 4.3 switch

Útil cuando tienes muchos valores posibles para una misma variable:

```java
String rol = "ADMIN";

switch (rol) {
    case "ADMIN":
        System.out.println("Acceso total");
        break;
    case "CAJERO":
        System.out.println("Solo puede crear facturas");
        break;
    default:
        System.out.println("Rol desconocido");
        break;
}
```

En Factugest, los usuarios tienen rol `ADMIN` o `CAJERO`. Spring Security usa esto para controlar qué páginas puede ver cada quien.

### 4.4 Operador ternario (if en una línea)

```java
int stock = 5;
String estado = (stock > 0) ? "Disponible" : "Agotado";
// Si stock > 0 → "Disponible", si no → "Agotado"
```

### Ejercicio 4.1
Escribe un programa que, dado el rol de un usuario (`"ADMIN"`, `"CAJERO"`, u otro), imprima qué módulos puede ver:
- **ADMIN**: Todos los módulos (Facturas, Clientes, Productos, Usuarios, Logs)
- **CAJERO**: Solo Facturas y Clientes
- **Otro**: Acceso denegado

### Ejercicio 4.2
Dado un porcentaje de descuento (por ejemplo `15`), determina si es:
- Mayor a `20`: "Descuento especial"
- Entre `10` y `20` (inclusive): "Descuento estándar"
- Menor a `10`: "Descuento mínimo"
- Exactamente `0`: "Sin descuento"

---

## Sección 5 — Ciclos

### 5.1 for clásico

```java
// Recorre números del 1 al 5
for (int i = 1; i <= 5; i++) {
    System.out.println("Item " + i);
}
```

**Estructura:**
```
for ( inicio ; condición ; incremento ) {
    // código que se repite
}
```

### 5.2 while

Cuando no sabes cuántas veces va a repetirse, sino que depende de una condición:

```java
int intentos = 0;
boolean loginExitoso = false;

while (!loginExitoso && intentos < 3) {
    // Simular intento de login
    System.out.println("Intento " + (intentos + 1));
    intentos++;
    if (intentos == 2) loginExitoso = true; // En el segundo intento funciona
}

if (loginExitoso) {
    System.out.println("Login correcto");
} else {
    System.out.println("Cuenta bloqueada por múltiples intentos");
}
```

### 5.3 do-while

Se ejecuta **al menos una vez** antes de verificar la condición:

```java
int opcion;
do {
    System.out.println("Menú: 1) Ver facturas  2) Salir");
    opcion = 2; // Simulamos que el usuario elige 2
} while (opcion != 2);

System.out.println("Saliendo...");
```

### 5.4 for-each (el más usado en Factugest)

Cuando tienes una lista de elementos y quieres recorrerlos todos:

```java
List<String> modulos = List.of("Facturas", "Clientes", "Productos", "Usuarios");

for (String modulo : modulos) {
    System.out.println("Módulo: " + modulo);
}
```

Esto se usa constantemente en Factugest, por ejemplo para mostrar todos los clientes en la vista:

```java
// En CustomerController.java (simplificado)
List<Customer> clientes = customerService.getAll();

for (Customer cliente : clientes) {
    System.out.println(cliente.getFullName());
}
```

### 5.5 break y continue

```java
// break — detiene el ciclo por completo
for (int i = 0; i < 10; i++) {
    if (i == 5) break;
    System.out.println(i); // Imprime 0, 1, 2, 3, 4
}

// continue — salta a la siguiente iteración
for (int i = 0; i < 10; i++) {
    if (i % 2 == 0) continue; // Salta los pares
    System.out.println(i); // Imprime 1, 3, 5, 7, 9
}
```

### Ejercicio 5.1
Dada una lista de precios de productos:
```java
double[] precios = {15000, 32000, 8500, 45000, 12000, 27000};
```
Calcula:
1. El precio total de todos los productos
2. Cuántos productos cuestan más de `20000`
3. El precio promedio
4. El producto más caro

### Ejercicio 5.2
Genera una tabla de multiplicar del número 7, del 1 al 10, con el formato:
```
7 x 1 = 7
7 x 2 = 14
...
7 x 10 = 70
```

---

## Sección 6 — Arrays y Listas

### 6.1 Arrays (tamaño fijo)

```java
// Declarar e inicializar
String[] roles = {"ADMIN", "CAJERO", "SUPERVISOR"};

// Acceder por índice (empieza en 0)
System.out.println(roles[0]); // "ADMIN"
System.out.println(roles[1]); // "CAJERO"

// Tamaño del array
System.out.println(roles.length); // 3

// Modificar
roles[2] = "AUDITOR";
```

### 6.2 ArrayList (tamaño dinámico — el más usado en Factugest)

```java
import java.util.ArrayList;
import java.util.List;

List<String> productos = new ArrayList<>();

// Agregar elementos
productos.add("Laptop");
productos.add("Mouse");
productos.add("Teclado");

// Tamaño
System.out.println(productos.size()); // 3

// Acceder
System.out.println(productos.get(0)); // "Laptop"

// Eliminar
productos.remove("Mouse");

// ¿Contiene un elemento?
boolean tiene = productos.contains("Laptop"); // true

// Recorrer con for-each
for (String p : productos) {
    System.out.println(p);
}
```

> En Factugest, los servicios devuelven `List<Customer>`, `List<Producto>`, etc. Estos son `ArrayList` por debajo. Cuando el controller recibe esa lista, la manda a la vista (HTML) para que Thymeleaf la dibuje como una tabla.

### 6.3 Listas con tipos de Factugest

```java
// Simulando lo que hace CustomerService.getAll()
List<String> nombresClientes = new ArrayList<>();
nombresClientes.add("Carlos Rodríguez");
nombresClientes.add("María López");
nombresClientes.add("Empresa XYZ S.A.S");

System.out.println("Total clientes: " + nombresClientes.size());

for (String nombre : nombresClientes) {
    System.out.println("- " + nombre);
}
```

### Ejercicio 6.1
Crea una lista de 5 SKUs de productos (por ejemplo: `"PROD-001"`, `"PROD-002"`, etc.).
1. Agrega un sexto SKU
2. Elimina el segundo
3. Imprime todos los SKUs que quedan
4. Imprime cuántos hay en total

---

## Sección 7 — Métodos (Funciones)

Un **método** es un bloque de código con nombre que realiza una tarea específica. En lugar de repetir el mismo código varias veces, lo encapsulas en un método y lo llamas cuando lo necesitas.

### 7.1 Estructura de un método

```java
modificador tipodeRetorno nombreDelMetodo(parametros) {
    // código
    return valor; // si el tipo de retorno no es void
}
```

### 7.2 Método sin retorno (void)

```java
public static void imprimirSeparador() {
    System.out.println("=========================");
}

// Llamada
imprimirSeparador();
```

### 7.3 Método con parámetros y retorno

```java
public static double calcularIva(double subtotal, double porcentajeIva) {
    return subtotal * (porcentajeIva / 100);
}

// Llamada
double iva = calcularIva(100000, 19);
System.out.println("IVA: " + iva); // 19000.0
```

### 7.4 Métodos con BigDecimal (como en Factugest)

```java
import java.math.BigDecimal;
import java.math.RoundingMode;

public static BigDecimal calcularIva(BigDecimal subtotal, BigDecimal porcentaje) {
    // porcentaje / 100 * subtotal
    return subtotal.multiply(porcentaje)
                   .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
}

// Llamada
BigDecimal subtotal = new BigDecimal("100000.00");
BigDecimal porcentajeIva = new BigDecimal("19");
BigDecimal iva = calcularIva(subtotal, porcentajeIva);
System.out.println("IVA: " + iva); // 19000.00
```

### 7.5 Sobrecarga de métodos

En Java, puedes tener varios métodos con el mismo nombre si tienen diferentes parámetros:

```java
public static String formatearPrecio(int precio) {
    return "$" + precio;
}

public static String formatearPrecio(BigDecimal precio) {
    return "$" + precio.toString();
}

public static String formatearPrecio(BigDecimal precio, String moneda) {
    return moneda + " " + precio.toString();
}
```

### 7.6 Métodos en Factugest — ejemplo real

En `InvoiceService.java`, hay un método que busca clientes por nombre (para el autocompletado de la factura):

```java
// Versión simplificada del método real en InvoiceService.java
public List<Customer> searchCustomers(String query) {
    // Retorna clientes cuyo nombre contiene el texto buscado
    return customerRepository.findByFullNameContainingIgnoreCase(query);
}
```

### Ejercicio 7.1
Crea los siguientes métodos:

1. `calcularSubtotal(int cantidad, BigDecimal precioUnitario)` → retorna el subtotal
2. `calcularDescuento(BigDecimal subtotal, BigDecimal porcentaje)` → retorna el valor del descuento
3. `calcularTotal(BigDecimal subtotal, BigDecimal iva, BigDecimal descuento)` → retorna el total final
4. Un método `imprimirResumenVenta` que reciba los tres valores anteriores y los imprima con formato

### Ejercicio 7.2
Crea un método `validarDocumento(String tipo, String numero)` que retorne `true` si:
- Tipo `"NIT"`: el número tiene entre 8 y 11 dígitos y es solo numérico
- Tipo `"CC"`: el número tiene entre 6 y 10 dígitos y es solo numérico
- Para cualquier otro tipo: retorna `false`

Pista: usa `numero.matches("[0-9]+")` para verificar que es solo numérico, y `.length()` para el largo.

---

## Sección 8 — Manejo de Excepciones

Las excepciones son errores que ocurren durante la ejecución del programa. Java permite capturarlos y manejarlos en lugar de que el programa se caiga.

### 8.1 try / catch / finally

```java
try {
    // Código que puede fallar
    int resultado = 10 / 0;
} catch (ArithmeticException e) {
    // Se ejecuta si ocurre el error especificado
    System.out.println("Error: no se puede dividir entre cero");
    System.out.println("Detalle: " + e.getMessage());
} finally {
    // Se ejecuta SIEMPRE, haya error o no (útil para cerrar conexiones)
    System.out.println("Fin del bloque try-catch");
}
```

### 8.2 Excepciones comunes en Java

| Excepción | Cuándo ocurre |
|-----------|---------------|
| `NullPointerException` | Intentas usar un objeto que es `null` |
| `ArithmeticException` | División entre cero |
| `NumberFormatException` | Convertir texto no numérico a número |
| `ArrayIndexOutOfBoundsException` | Acceder a un índice que no existe en el array |

### 8.3 NullPointerException — el más común

```java
String nombre = null;

// MAL — lanza NullPointerException
System.out.println(nombre.length());

// BIEN — verificar antes
if (nombre != null) {
    System.out.println(nombre.length());
} else {
    System.out.println("El nombre está vacío");
}
```

### 8.4 Ejemplo de Factugest — parsear un ID de la URL

Cuando Spring Boot recibe una URL como `/customer/5`, el `5` llega como texto. Al convertirlo a número puede fallar:

```java
public static Integer parsearId(String idTexto) {
    try {
        return Integer.parseInt(idTexto);
    } catch (NumberFormatException e) {
        System.out.println("ID inválido: " + idTexto);
        return null;
    }
}
```

### Ejercicio 8.1
Escribe un programa que:
1. Pida al usuario dividir `1000` entre un número (usa `Scanner`)
2. Maneje la excepción si se ingresa `0`
3. Maneje la excepción si se ingresa texto en lugar de número
4. En el `finally`, imprima "Operación finalizada"

---

## Sección 9 — Ejercicios de Cierre

Estos ejercicios integran todo lo aprendido en el curso.

### Ejercicio Final 1 — Calculadora de Factura Simple

Crea un programa que simule el cálculo de una factura:

```
Datos de entrada (puedes definirlos como variables):
- Nombre del cliente: "Carlos López"
- Productos:
  - "Laptop"   → precio $2,500,000  cantidad 1
  - "Mouse"    → precio $45,000     cantidad 2
  - "Teclado"  → precio $80,000     cantidad 1
- IVA: 19%
- Descuento sobre el total: 5%
```

El programa debe calcular e imprimir:
- Subtotal por cada producto
- Subtotal total
- Valor del descuento
- Subtotal con descuento
- Valor del IVA
- **TOTAL A PAGAR**

Usa métodos para cada cálculo y `BigDecimal` para los valores monetarios.

---

### Ejercicio Final 2 — Validador de Usuario

Crea un método `validarLogin(String correo, String contrasena)` que:
1. Verifique que el correo contiene `@` y `.`
2. Verifique que la contraseña tiene al menos 8 caracteres
3. Retorne `true` si ambas validaciones pasan, `false` si no
4. Pruébalo con al menos 3 casos distintos

---

### Ejercicio Final 3 — Reporte de Inventario

Dadas estas listas:

```java
String[] nombres    = {"Laptop", "Mouse", "Teclado", "Monitor", "Webcam"};
int[]    stocks     = {5, 12, 0, 3, 8};
double[] precios    = {2500000, 45000, 80000, 750000, 120000};
boolean[] activos   = {true, true, false, true, true};
```

Escribe un programa que:
1. Imprima solo los productos activos con stock mayor a 0
2. Calcule el valor total del inventario activo
3. Identifique cuántos productos necesitan reabastecimiento (stock < 3 y activo)
4. Imprima el producto más caro del inventario

---

## Resumen del Curso 01

| Concepto | Lo que aprendiste |
|----------|-------------------|
| Variables | Tipos primitivos, `String`, `Integer`, `BigDecimal` para dinero |
| Operadores | Aritméticos, comparación, lógicos |
| Condicionales | `if/else`, `switch`, ternario |
| Ciclos | `for`, `while`, `do-while`, `for-each` |
| Listas | `ArrayList`, `List<T>` |
| Métodos | Parámetros, retorno, sobrecarga |
| Excepciones | `try/catch/finally`, `NullPointerException` |

---

## Siguiente paso

Una vez completados los ejercicios de este curso, continúa con el **Curso 02 — Java con Programación Orientada a Objetos**, donde aprenderás a crear clases propias, como las entidades del proyecto Factugest (`Cliente`, `Producto`, `Factura`).

---

*Factugest — Equipo de Desarrollo*
