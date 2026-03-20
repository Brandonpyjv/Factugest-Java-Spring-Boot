# Factugest — Sistema de Facturación Electrónica

Sistema de facturación electrónica para Colombia, construido con **Java 21 + Spring Boot 3.3.5** y plantillas **Thymeleaf**. Corre sobre una base de datos **PostgreSQL**.

---

## Tabla de Contenidos

- [¿Qué es Factugest?](#qué-es-factugest)
- [Tecnologías](#tecnologías)
- [Requisitos previos](#requisitos-previos)
- [Configuración de la base de datos](#configuración-de-la-base-de-datos)
- [Variables de entorno](#variables-de-entorno)
- [Cómo arrancar el proyecto](#cómo-arrancar-el-proyecto)
- [Acceder a la aplicación](#acceder-a-la-aplicación)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Módulos disponibles](#módulos-disponibles)
- [Notas importantes](#notas-importantes)

---

## ¿Qué es Factugest?

Factugest es una aplicación web para gestionar facturas electrónicas. Permite:

- Crear, visualizar y eliminar **facturas electrónicas** (FV, NC, ND)
- Gestionar **clientes** con datos tributarios completos
- Administrar **productos/servicios** con control de stock
- Registrar **empresas** emisoras con NIT y régimen tributario
- Configurar **métodos de pago** y **estados de factura**
- Ver un **dashboard** con KPIs en tiempo real
- Generar **PDF** de cada factura

---

## Tecnologías

| Componente       | Tecnología                           |
|------------------|--------------------------------------|
| Lenguaje         | Java 21                              |
| Framework        | Spring Boot 3.3.5                    |
| Vistas           | Thymeleaf + Thymeleaf Layout Dialect |
| Acceso a datos   | Spring Data JPA + JdbcTemplate       |
| ORM              | Hibernate 6 (modo `validate`)        |
| Base de datos    | PostgreSQL 15+                       |
| PDF              | iText 7.2.5                          |
| Frontend         | Bootstrap 5.3 + Bootstrap Icons      |
| Gestor de build  | Maven (wrapper incluido)             |

---

## Requisitos previos

Antes de arrancar, asegúrate de tener instalado:

### 1. Java 21 o superior

```bash
java -version
# Debe mostrar: openjdk 21 o superior
```

Descarga: https://adoptium.net/ (Temurin JDK 21, recomendado)

### 2. PostgreSQL 15 o superior corriendo en `localhost:5432`

Descarga: https://www.postgresql.org/download/

Verifica que el servicio esté activo:
```bash
# Windows — en servicios busca "postgresql-x64-15" o similar
# O desde psql:
psql -U postgres -c "SELECT version();"
```

### 3. Maven no es necesario instalar

El proyecto incluye el **Maven Wrapper** (`mvnw` / `mvnw.cmd`). Solo necesitas tener Java 21.

---

## Configuración de la base de datos

### Paso 1 — Crear la base de datos en PostgreSQL

Abre pgAdmin o psql y ejecuta:

```sql
CREATE DATABASE factugest;
```

### Paso 2 — Crear el schema (tablas, índices, secuencias)

Ejecuta el archivo `postgres_migration.sql` que está en la raíz del proyecto:

```bash
# Desde psql:
psql -U postgres -d factugest -f postgres_migration.sql

# O desde pgAdmin:
# Abre Query Tool sobre la BD "factugest" → carga y ejecuta postgres_migration.sql
```

Este script crea todas las tablas necesarias con sus tipos, restricciones y secuencias.

### Paso 3 — Cargar los datos de ejemplo

Ejecuta el archivo `reseed_data.sql` que también está en la raíz del proyecto:

```bash
psql -U postgres -d factugest -f reseed_data.sql
```

Este script carga clientes, productos, empresas, impuestos, usuarios, métodos de pago y facturas de prueba.

> Si en algún momento necesitas resetear todos los datos al estado inicial, vuelve a ejecutar `reseed_data.sql` — hace TRUNCATE con CASCADE antes de insertar, así que es seguro correrlo varias veces.

### Paso 4 — Configurar las credenciales

La conexión se configura mediante **variables de entorno**. El archivo `application.properties` usa la sintaxis `${VARIABLE:default}` de Spring Boot — nunca contiene contraseñas reales.

Consulta `src/main/resources/application.properties.example` para ver qué variables necesitas definir:

```properties
DB_HOST=localhost
DB_PORT=5432
DB_NAME=factugest
DB_USERNAME=postgres
DB_PASSWORD=tu_contraseña_aqui
SERVER_PORT=8080   # opcional
```

**Cómo inyectar las variables** (elige una opción):

- **IntelliJ IDEA** → ver sección [Variables de entorno](#variables-de-entorno) más abajo
- **Terminal / Maven Wrapper:**
  ```bash
  DB_PASSWORD=mi_clave ./mvnw spring-boot:run
  ```
- **JAR:**
  ```bash
  DB_PASSWORD=mi_clave java -jar target/factugest-0.0.1-SNAPSHOT.jar
  ```

> La aplicación usa `ddl-auto=validate` — **nunca crea ni modifica tablas**. Si la BD no existe o el schema no coincide, falla al iniciar (intencional: falla rápido, no silencioso).

**Tablas que debe tener la base de datos:**

| Tabla                | Descripción                        |
|----------------------|------------------------------------|
| `customers`          | Clientes                           |
| `facturas`           | Cabecera de facturas               |
| `detalle_factura`    | Líneas de productos por factura    |
| `productos`          | Catálogo de productos/servicios    |
| `impuestos`          | Tipos de impuesto (IVA, etc.)      |
| `descuentos`         | Descuentos configurables           |
| `usuarios`           | Usuarios del sistema               |
| `empresas`           | Empresas emisoras                  |
| `metodos_pago`       | Métodos de pago disponibles        |
| `pagos_factura`      | Estados de pago (paid, pending...) |
| `logs`               | Registro de actividad              |
| `producto_descuento` | Relación producto ↔ descuento      |

---

---

## Variables de entorno

El proyecto **no almacena secretos en el código**. Las credenciales se inyectan en tiempo de ejecución a través de variables de entorno.

### Configuración en IntelliJ IDEA

1. Menú superior → **Run** → **Edit Configurations...**
2. Selecciona la configuración de `FactugestApplication` (o créala si no existe)
3. En el campo **Environment variables**, haz clic en el ícono de carpeta a la derecha
4. Agrega cada variable con su valor real:

| Variable | Valor de ejemplo | Descripción |
|---|---|---|
| `DB_HOST` | `localhost` | Host de PostgreSQL |
| `DB_PORT` | `5432` | Puerto de PostgreSQL |
| `DB_NAME` | `factugest` | Nombre de la base de datos |
| `DB_USERNAME` | `postgres` | Usuario de PostgreSQL |
| `DB_PASSWORD` | `tu_contraseña` | **Contraseña** — nunca la subas a Git |
| `SERVER_PORT` | `8080` | Puerto HTTP (opcional) |

5. Haz clic en **OK** y luego en **Apply**.

> **Nota:** IntelliJ guarda estas variables en `.idea/workspace.xml`, que está en `.gitignore`. Están a salvo y no se subirán al repositorio.

### Valores por defecto

Si no defines una variable, Spring Boot usa el valor por defecto declarado en `application.properties` (ej: `DB_HOST` → `localhost`, `SERVER_PORT` → `8080`). La única variable **sin valor por defecto** es `DB_PASSWORD` — si no se define, la aplicación fallará al intentar conectarse a PostgreSQL.

---

## Cómo arrancar el proyecto

### Opción 1 — Con Maven Wrapper (recomendado)

```bash
# 1. Abre una terminal en la carpeta raíz del proyecto
cd "ruta/a/Factugest Java"

# 2. En Windows CMD:
mvnw spring-boot:run

# 2. En Windows PowerShell:
.\mvnw spring-boot:run

# 2. En Linux/Mac:
./mvnw spring-boot:run
```

La primera vez Maven descargará todas las dependencias (~200 MB). Las siguientes ejecuciones serán inmediatas.

### Opción 2 — Desde un IDE (IntelliJ IDEA / Eclipse / VS Code)

1. Abre la carpeta del proyecto como **proyecto Maven** (IntelliJ la detecta automáticamente)
2. Espera a que el IDE descargue las dependencias (barra de progreso en IntelliJ)
3. Ejecuta la clase `com.factugest.FactugestApplication` — tiene el método `main`
4. En IntelliJ: clic derecho → `Run 'FactugestApplication'`

### Opción 3 — Con el JAR compilado

```bash
# 1. Compilar primero (genera target/factugest-0.0.1-SNAPSHOT.jar)
mvnw package -DskipTests

# 2. Ejecutar el JAR
java -jar target/factugest-0.0.1-SNAPSHOT.jar
```

---

## Acceder a la aplicación

Una vez iniciado (verás en consola `Started FactugestApplication`), abre el navegador en:

```
http://localhost:8080
```

Verás el **Dashboard** de Factugest con los KPIs del sistema.

**Usuarios de prueba** (cargados por `reseed_data.sql`):

| Usuario       | Correo                          | Contraseña  | Rol    |
|---------------|---------------------------------|-------------|--------|
| Administrator | administrador@factugest.com     | 123456789   | ADMIN  |
| Brandon       | brandon@factugest.com           | 123456789   | ADMIN  |
| Johan         | johan@factugest.com             | 123456789   | ADMIN  |
| Wilmer        | wilmer@factugest.com            | 123456789   | ADMIN  |
| Yuliana       | yuliana@factugest.com           | 123456789   | CAJERO |

> Nota: actualmente la aplicación no tiene pantalla de login — los usuarios son solo para asociar facturas a un cajero.

---

## Estructura del proyecto

```
Factugest Java/
├── mvnw / mvnw.cmd              ← Maven Wrapper (no requiere Maven instalado)
├── pom.xml                      ← Dependencias y configuración del build
├── postgres_migration.sql       ← Script de creación del schema PostgreSQL
├── reseed_data.sql              ← Script para cargar/resetear datos de prueba
│
└── src/
    └── main/
        ├── java/com/factugest/
        │   ├── FactugestApplication.java      ← Punto de entrada (main)
        │   │
        │   ├── controller/                    ← Reciben peticiones HTTP
        │   │   ├── HomeController.java        (/ y /settings)
        │   │   ├── InvoiceController.java     (/invoice/*)
        │   │   ├── CustomerController.java    (/customer/*)
        │   │   ├── ProductController.java     (/products/product/*)
        │   │   ├── UserController.java        (/users/*)
        │   │   ├── BranchController.java      (/branches/*)
        │   │   ├── PaymentMethodController.java (/payment_methods/*)
        │   │   ├── DiscountController.java    (/discount/*)
        │   │   ├── TaxController.java         (/invoice_taxes/*)
        │   │   ├── InvoicePaymentController.java (/invoice_payments/*)
        │   │   ├── LogController.java         (/logs)
        │   │   └── ProductDiscountController.java (/product_discounts)
        │   │
        │   ├── entity/                        ← Mapeo a tablas de la BD
        │   │   ├── Customer.java
        │   │   ├── Factura.java
        │   │   ├── DetalleFactura.java
        │   │   ├── Producto.java
        │   │   ├── Impuesto.java
        │   │   ├── Descuento.java
        │   │   ├── Usuario.java
        │   │   ├── Empresa.java
        │   │   ├── MetodoPago.java
        │   │   ├── PagoFactura.java
        │   │   └── Log.java
        │   │
        │   ├── repository/                    ← Acceso a BD (CRUD automático JPA)
        │   │   └── [Una interfaz por entidad]
        │   │
        │   └── service/                       ← Lógica de negocio
        │       ├── InvoiceService.java        (cálculos, JOINs complejos)
        │       ├── ProductoService.java
        │       ├── LogService.java
        │       ├── ProductDiscountService.java
        │       ├── PdfService.java            (generación de PDF con iText7)
        │       └── [Un service por módulo]
        │
        └── resources/
            ├── application.properties         ← Configuración (BD, puerto...)
            ├── static/
            │   ├── css/main.css              ← Estilos personalizados
            │   └── img/                      ← Logos e imágenes
            └── templates/                    ← Vistas HTML (Thymeleaf)
                ├── layout.html               ← Plantilla base (navbar + sidebar)
                ├── index.html                ← Dashboard
                ├── invoice/                  ← Facturas
                ├── customer/                 ← Clientes
                ├── product/                  ← Productos
                ├── users/                    ← Usuarios
                ├── branches/                 ← Empresas
                ├── payment_methods/          ← Métodos de pago
                ├── discount/                 ← Descuentos
                ├── invoice_taxes/            ← Impuestos
                ├── invoice_payments/         ← Estados de pago
                ├── logs/                     ← Logs del sistema
                └── settings/                 ← Configuración
```

---

## Módulos disponibles

| Módulo              | URL base              | Descripción                              |
|---------------------|-----------------------|------------------------------------------|
| Dashboard           | `/`                   | KPIs y facturas recientes                |
| Facturas            | `/invoice`            | Lista, creación, vista, PDF, eliminación |
| Clientes            | `/customer`           | CRUD completo de clientes                |
| Productos           | `/products/product`   | Inventario con stock y precios           |
| Empresas            | `/branches`           | Empresas emisoras de facturas            |
| Usuarios            | `/users`              | Gestión de usuarios del sistema          |
| Métodos de pago     | `/payment_methods`    | Efectivo, tarjeta, transferencia, etc.   |
| Estados de pago     | `/invoice_payments`   | paid, pending, overdue, cancelled...     |
| Descuentos          | `/discount`           | Descuentos por producto o factura        |
| Impuestos           | `/invoice_taxes`      | IVA y otros impuestos DIAN               |
| Logs                | `/logs`               | Historial de actividad del sistema       |
| Configuración       | `/settings`           | Ajustes generales                        |

---

## Notas importantes

- **No modifica la base de datos** — Hibernate corre en modo `validate`. Si la BD no está disponible al iniciar o el schema no coincide, la aplicación falla con error de conexión o validación.
- **Puerto por defecto:** `8080`. Si está ocupado, cámbialo en `application.properties` con `server.port=XXXX`.
- **Cache de Thymeleaf desactivado** (`spring.thymeleaf.cache=false`) — ideal para desarrollo. En producción cámbialo a `true`.
- **Generación de PDFs** — se sirven directamente al navegador con opción de descarga (`Content-Disposition: attachment`).
- **Primer arranque lento** — Maven descarga dependencias (~200 MB la primera vez). Arranques siguientes son inmediatos.
- **Thymeleaf 3.1** — usa `th:data-*` en lugar de `th:onclick` por restricciones de seguridad del motor de plantillas.

---

*Factugest — Sistema de Facturación Electrónica Colombia*
