-- =====================================================
-- FACTUGEST - Migración MariaDB → PostgreSQL
-- Generado: 2026-03-18
-- Compatibilidad: Spring Boot 3.3.5 / Hibernate 6.5 / Java 21
-- =====================================================

BEGIN;

-- =====================================================
-- DDL - CREACIÓN DE TABLAS
-- =====================================================

-- tabla: usuarios
CREATE TABLE IF NOT EXISTS usuarios (
    cod_usuario   SERIAL        PRIMARY KEY,
    nombre        VARCHAR(255),
    correo        VARCHAR(255)  UNIQUE,
    contrasena    VARCHAR(255),
    rol           VARCHAR(255)
);

-- tabla: pagos_factura
CREATE TABLE IF NOT EXISTS pagos_factura (
    cod_pago_factura  SERIAL       PRIMARY KEY,
    status            VARCHAR(20)  NOT NULL
);

-- tabla: metodos_pago
CREATE TABLE IF NOT EXISTS metodos_pago (
    cod_pago     SERIAL        PRIMARY KEY,
    descripcion  VARCHAR(255),
    nombre       VARCHAR(255)
);

-- tabla: impuestos
CREATE TABLE IF NOT EXISTS impuestos (
    cod_impuesto  SERIAL        PRIMARY KEY,
    porcentaje    NUMERIC       NOT NULL,
    descripcion   VARCHAR(255)  NOT NULL,
    codigo_dian   VARCHAR(5)
);

-- tabla: customers
CREATE TABLE IF NOT EXISTS customers (
    customer_id         SERIAL        PRIMARY KEY,
    full_name           VARCHAR(100)  NOT NULL,
    document_type       VARCHAR(1)    NOT NULL,
    document_number     VARCHAR(20)   NOT NULL  UNIQUE,
    phone               VARCHAR(15),
    email               VARCHAR(100),
    address             VARCHAR(255),
    ciudad              VARCHAR(50)   NOT NULL  DEFAULT '',
    departamento        VARCHAR(50)   NOT NULL  DEFAULT '',
    pais                VARCHAR(50)   NOT NULL  DEFAULT '',
    tipo_persona        VARCHAR(20)             DEFAULT 'NATURAL',
    regimen_tributario  VARCHAR(60)             DEFAULT 'NO_RESPONSABLE_IVA'
);

-- tabla: empresas
CREATE TABLE IF NOT EXISTS empresas (
    cod_empresa          SERIAL        PRIMARY KEY,
    nombre               VARCHAR(255),
    nit                  VARCHAR(255)  UNIQUE,
    direccion            VARCHAR(255),
    ciudad               VARCHAR(50)   NOT NULL  DEFAULT '',
    telefono             VARCHAR(255),
    correo               VARCHAR(255),
    dv                   VARCHAR(5),
    regimen_tributario   VARCHAR(60)             DEFAULT 'RESPONSABLE_IVA',
    actividad_economica  VARCHAR(10),
    tipo_documento       VARCHAR(20)             DEFAULT 'NIT'
);

-- tabla: descuentos
-- NOTA: aplica_a_producto y aplica_a_factura son INTEGER (Java Integer) — en MariaDB eran SMALLINT (causa del error)
CREATE TABLE IF NOT EXISTS descuentos (
    cod_descuento      SERIAL        PRIMARY KEY,
    descripcion        VARCHAR(255),
    porcentaje         NUMERIC       NOT NULL,
    aplica_a_producto  INTEGER       NOT NULL,
    aplica_a_factura   INTEGER       NOT NULL
);

-- tabla: productos
CREATE TABLE IF NOT EXISTS productos (
    cod_producto    SERIAL          PRIMARY KEY,
    sku             VARCHAR(50)     NOT NULL  UNIQUE,
    nombre          VARCHAR(255)    NOT NULL,
    descripcion     TEXT,
    precio_unitario NUMERIC(12,2)   NOT NULL,
    stock           INTEGER                   DEFAULT 0,
    stock_minimo    INTEGER                   DEFAULT 5,
    cod_impuesto    INTEGER         NOT NULL  DEFAULT 1,
    unidad_medida   VARCHAR(20)               DEFAULT '94',
    codigo_barras   VARCHAR(50),
    activo          INTEGER                   DEFAULT 1,
    tipo_item       VARCHAR(5)                DEFAULT 'IP',
    CONSTRAINT fk_producto_impuesto FOREIGN KEY (cod_impuesto) REFERENCES impuestos (cod_impuesto)
);

-- tabla: facturas
-- NOTA: fecha → TIMESTAMP WITHOUT TIME ZONE para compatibilidad con java.time.LocalDateTime
--       fecha_vencimiento → DATE para java.time.LocalDate
--       total/subtotal etc → NUMERIC para java.math.BigDecimal
CREATE TABLE IF NOT EXISTS facturas (
    cod_factura       SERIAL                       PRIMARY KEY,
    fecha             TIMESTAMP WITHOUT TIME ZONE  NOT NULL,
    fecha_vencimiento DATE,
    cod_cliente       INTEGER,
    cod_usuario       INTEGER,
    cod_pago          INTEGER,
    total             NUMERIC                      NOT NULL,
    subtotal          NUMERIC                      NOT NULL  DEFAULT 0,
    total_descuentos  NUMERIC                      NOT NULL  DEFAULT 0,
    total_impuestos   NUMERIC                      NOT NULL  DEFAULT 0,
    tipo_factura      VARCHAR(5)                   NOT NULL  DEFAULT 'FV',
    observaciones     TEXT,
    cod_empresa       INTEGER,
    cod_metodo_pago   INTEGER,
    CONSTRAINT facturas_ibfk_1 FOREIGN KEY (cod_cliente)     REFERENCES customers    (customer_id)    ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT facturas_ibfk_2 FOREIGN KEY (cod_usuario)     REFERENCES usuarios     (cod_usuario)    ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT facturas_ibfk_3 FOREIGN KEY (cod_empresa)     REFERENCES empresas     (cod_empresa)    ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT facturas_ibfk_4 FOREIGN KEY (cod_pago)        REFERENCES pagos_factura(cod_pago_factura) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_facturas_metodo_pago FOREIGN KEY (cod_metodo_pago) REFERENCES metodos_pago (cod_pago)
);

-- tabla: detalle_factura
-- NOTA: precio_unitario, subtotal etc → NUMERIC para java.math.BigDecimal
CREATE TABLE IF NOT EXISTS detalle_factura (
    cod_destalle          SERIAL        PRIMARY KEY,
    cod_factura           INTEGER       NOT NULL,
    cod_producto          INTEGER       NOT NULL,
    cantidad              INTEGER       NOT NULL,
    precio_unitario       NUMERIC(12,2) NOT NULL,
    subtotal              NUMERIC(12,2) NOT NULL,
    descuento_porcentaje  NUMERIC       NOT NULL  DEFAULT 0,
    descuento_valor       NUMERIC       NOT NULL  DEFAULT 0,
    impuesto_porcentaje   NUMERIC       NOT NULL  DEFAULT 0,
    impuesto_valor        NUMERIC       NOT NULL  DEFAULT 0
);

-- tabla: factura_descuento
CREATE TABLE IF NOT EXISTS factura_descuento (
    cod_descuento   INTEGER  NOT NULL,
    cod_factura     INTEGER  NOT NULL,
    valor_descuento REAL     NOT NULL,
    PRIMARY KEY (cod_descuento, cod_factura),
    CONSTRAINT fk_factura_descuento_factura   FOREIGN KEY (cod_factura)   REFERENCES facturas   (cod_factura),
    CONSTRAINT fk_factura_descuento_descuento FOREIGN KEY (cod_descuento) REFERENCES descuentos (cod_descuento)
);

-- tabla: factura_impuesto
CREATE TABLE IF NOT EXISTS factura_impuesto (
    id_factura_impuesto  SERIAL   PRIMARY KEY,
    cod_factura          INTEGER,
    cod_impuesto         INTEGER,
    valor_impuesto       REAL     NOT NULL,
    CONSTRAINT fk_factura_impuesto_factura  FOREIGN KEY (cod_factura)  REFERENCES facturas  (cod_factura),
    CONSTRAINT fk_factura_impuesto_impuesto FOREIGN KEY (cod_impuesto) REFERENCES impuestos (cod_impuesto)
);

-- tabla: logs
-- NOTA: fecha → TIMESTAMP WITHOUT TIME ZONE para java.time.LocalDateTime
CREATE TABLE IF NOT EXISTS logs (
    id_log       SERIAL                       PRIMARY KEY,
    cod_usuario  INTEGER                      NOT NULL,
    fecha        TIMESTAMP WITHOUT TIME ZONE  NOT NULL,
    accion       VARCHAR(100)                 NOT NULL,
    descripcion  VARCHAR(255)                 NOT NULL,
    CONSTRAINT fk_logs_usuario FOREIGN KEY (cod_usuario) REFERENCES usuarios (cod_usuario)
);

-- tabla: producto_descuento
CREATE TABLE IF NOT EXISTS producto_descuento (
    cod_producto   INTEGER  NOT NULL,
    cod_descuento  INTEGER  NOT NULL,
    PRIMARY KEY (cod_producto, cod_descuento),
    CONSTRAINT fk_prod_desc_descuento FOREIGN KEY (cod_descuento) REFERENCES descuentos (cod_descuento) ON DELETE CASCADE ON UPDATE CASCADE
);

-- tabla: clientes (legada, sin entidad Java)
CREATE TABLE IF NOT EXISTS clientes (
    cod_cliente       SERIAL        PRIMARY KEY,
    nombre            VARCHAR(255),
    tipo_documento    VARCHAR(255),
    numero_documento  VARCHAR(255)  UNIQUE,
    telefono          VARCHAR(255),
    correo            VARCHAR(255),
    direccion         VARCHAR(255)
);

-- tabla: configuracion (sin entidad Java)
CREATE TABLE IF NOT EXISTS configuracion (
    clave  VARCHAR(255)  PRIMARY KEY,
    valor  VARCHAR(255)
);

-- tabla: productos_descuentos (legada/duplicada, sin entidad Java)
CREATE TABLE IF NOT EXISTS productos_descuentos (
    cod_descuento  INTEGER  NOT NULL,
    cod_producto   INTEGER  NOT NULL,
    PRIMARY KEY (cod_descuento)
);

-- =====================================================
-- DATOS
-- =====================================================

-- usuarios
INSERT INTO usuarios (cod_usuario, nombre, correo, contrasena, rol) VALUES
(1, 'Administrator', 'administrador@factugest.com', '123456789', 'ADMIN'),
(2, 'Brandon',       'brandon@factugest.com',       '123456789', 'ADMIN'),
(3, 'Johan',         'johan@factugest.com',          '123456789', 'ADMIN'),
(4, 'Wilmer',        'wilmer@factugest.com',         '123456789', 'ADMIN'),
(5, 'Yuliana',       'yuliana@factugest.com',        '123456789', 'CAJERO');

-- pagos_factura
INSERT INTO pagos_factura (cod_pago_factura, status) VALUES
(1, 'paid'),
(2, 'pending'),
(3, 'partially paid'),
(4, 'overdue'),
(5, 'cancelled'),
(6, 'disputed'),
(7, 'refunded');

-- metodos_pago
INSERT INTO metodos_pago (cod_pago, descripcion, nombre) VALUES
(1, 'CASH',          NULL),
(2, 'DEBIT CARD',    NULL),
(3, 'CREDIT CARD',   NULL),
(4, 'BANK TRANSFER', NULL),
(5, 'Sistecuerpo',   '');

-- impuestos
INSERT INTO impuestos (cod_impuesto, porcentaje, descripcion, codigo_dian) VALUES
(1, 19,  'IVA',      '01'),
(2, 2,   'RETEFUENTE','05'),
(3, 1,   'RETEICA',  '06'),
(6, 15,  'RETEIVA',  '07'),
(7, 0.8, 'RETECREE', '08'),
(8, 0,   'EXENTO',   'ZY');

-- customers
INSERT INTO customers (customer_id, full_name, document_type, document_number, phone, email, address, ciudad, departamento, pais, tipo_persona, regimen_tributario) VALUES
(1,  'Consumidor Final',                'C', '222222222222', '',             '',                          '',                                  '',       '',                    '',         'NATURAL', 'NO_RESPONSABLE_IVA'),
(2,  'Maria Fernanda Ruiz',             'C', '1015432109',   '3209876543',   'mafe.ruiz@outlook.com',     'Libertadores Ave # 11-45',          '',       '',                    '',         'NATURAL', 'NO_RESPONSABLE_IVA'),
(3,  'Juan Sebastian Gomez',            'E', 'E87654321',    '3156789012',   'juan.gomez@gmail.com',      'La Playa Neighborhood, House 4',    '',       '',                    '',         'NATURAL', 'NO_RESPONSABLE_IVA'),
(4,  'Diana Marcela Rojas',             'C', '1093456789',   '3004567890',   'diana.rojas@servicios.co',  '7N St # 3-12, Los Patios',          '',       '',                    '',         'NATURAL', 'NO_RESPONSABLE_IVA'),
(5,  'Ricardo Jose Torres',             'C', '1116789456',   '3112345678',   'ricardo.torres@empresa.com','0 Avenue # 15-30',                  '',       '',                    '',         'NATURAL', 'NO_RESPONSABLE_IVA'),
(6,  'Elena Patricia Meza',             'C', '1012345987',   '3189012345',   'elena.meza@misena.edu.co',  'Siglo XXI Estate',                  '',       '',                    '',         'NATURAL', 'NO_RESPONSABLE_IVA'),
(7,  'Oscar David Ortiz',               'E', 'E12345678',    '3145678234',   'oscar.ortiz@flete.net',     'Industrial Zone, Plot 5',           '',       '',                    '',         'NATURAL', 'NO_RESPONSABLE_IVA'),
(8,  'Sandra Milena Cano',              'C', '1090123456',   '3216540987',   'sandra.cano@yahoo.es',      '24th St # 12-05, Villa del Rosario','',       '',                    '',         'NATURAL', 'NO_RESPONSABLE_IVA'),
(9,  'Luis Alberto Quintero',           'C', '1115678234',   '3124567890',   'luis.quintero@tecnicos.com','5th Ave # 10-10',                   '',       '',                    '',         'NATURAL', 'NO_RESPONSABLE_IVA'),
(10, 'Angela Maria Velez',              'C', '1018907654',   '3012345678',   'angela.velez@estudio.edu',  '15th St # 4-50, Atalaya',           '',       '',                    '',         'NATURAL', 'NO_RESPONSABLE_IVA'),
(11, 'Brandon Arley Restrepo Gelvez',   'C', '1093789989',   '3044412657',   'brandonar1997@gmail.com',   'calle 37 # 3-41 Los Patios',        'Cucuta', 'Norte de Santander',  'Colombia', 'NATURAL', 'NO_RESPONSABLE_IVA'),
(12, 'wilmer contreras',                'C', '1149461932',   '3222597843',   'contreraswilmer083@gmail.com','Call 10 # san luis',              'Cucuta', 'Norte de Santander',  'Colombia', 'NATURAL', 'NO_RESPONSABLE_IVA');

-- empresas
INSERT INTO empresas (cod_empresa, nombre, nit, direccion, ciudad, telefono, correo, dv, regimen_tributario, actividad_economica, tipo_documento) VALUES
(1, 'Verdad y Reconciliacion', '980256314', 'Cll 28 # 9-47',      'Cali',   '3206841435', 'verdadyreconciliacion@factugest.com', '3', 'RESPONSABLE_IVA', NULL, 'NIT'),
(2, 'Pillar of Autumn',        '987654321', 'Av Caracas # 2-56',  'Cucuta', '3132156472', 'pillarofautumn@factugest.com',        '1', 'RESPONSABLE_IVA', NULL, 'NIT'),
(6, 'Gran Caridad',            '890345555', 'AV 10  # 3-21',      'Bogota', '312398888',  'grancaridad@factugest.com',           '2', 'RESPONSABLE_IVA', NULL, 'NIT');

-- descuentos
INSERT INTO descuentos (cod_descuento, descripcion, porcentaje, aplica_a_producto, aplica_a_factura) VALUES
(55501, 'Seasonal Discount',            10, 1, 0),
(55502, 'Frequent Customer Promotion',   5, 0, 1),
(55503, 'End of Month Offer',           15, 0, 1),
(55504, 'Volume Discount',               8, 1, 0),
(55505, 'Card Promotion',               12, 0, 1),
(55506, 'Inventory Clearance',          20, 1, 0),
(55507, 'Special VIP Discount',          7, 0, 1),
(55508, 'Anniversary Promotion',        18, 1, 0);

-- productos
INSERT INTO productos (cod_producto, sku, nombre, descripcion, precio_unitario, stock, stock_minimo, cod_impuesto, unidad_medida, codigo_barras, activo, tipo_item) VALUES
(1,  'TEC-001', 'Logitech Wireless Mouse',          'Ergonomic black mouse, AA battery included',          45000.00,  50,  10, 1, 'C62', NULL, 1, 'IP'),
(2,  'TEC-002', 'RGB Mechanical Keyboard',          'Gaming keyboard with blue switches, backlit',         180000.00, 20,   5, 1, 'C62', NULL, 1, 'IP'),
(3,  'TEC-003', 'Samsung 24" Monitor',              'LED IPS 75Hz display, HDMI/VGA',                      650000.00,  2,   3, 1, 'C62', NULL, 1, 'IP'),
(4,  'ACC-001', 'Laptop Backpack',                  'Waterproof backpack for laptops up to 15.6"',          95000.00, 30,   5, 1, 'C62', NULL, 1, 'IP'),
(5,  'SER-001', 'Preventive Maintenance',           'PC cleaning and optimization technical service',       80000.00,999,   0, 1, 'WSD', NULL, 1, 'IS'),
(6,  'TEC-004', '480GB SSD Solid State Drive',      'Kingston SATA III solid state drive',                120000.00, 40,   8, 1, 'C62', NULL, 1, 'IP'),
(7,  'TEC-005', '2 Meter HDMI Cable',               'Reinforced 4K high-speed cable',                      15000.00, 15,  20, 1, 'C62', NULL, 1, 'IP'),
(8,  'LIC-001', '1 Year Antivirus License',         'Digital activation code sent via email',               55000.00,999,   0, 1, 'WSD', NULL, 1, 'IS'),
(9,  'TEC-006', '8GB DDR4 RAM Memory',              'Laptop memory module 2666MHz',                        110000.00, 25,   5, 1, 'C62', NULL, 1, 'IP'),
(10, 'PAP-001', 'Letter Size Bond Paper Ream',      'White paper box 75g x 500 sheets',                    18500.00,  0,   0, 1, 'C62', NULL, 0, 'IP');

-- facturas
INSERT INTO facturas (cod_factura, fecha, fecha_vencimiento, cod_cliente, cod_usuario, cod_pago, total, subtotal, total_descuentos, total_impuestos, tipo_factura, observaciones, cod_empresa, cod_metodo_pago) VALUES
(2,  '2026-02-17 21:26:00', NULL,         7,  1, 1, 1200000, 0,      0, 0,      'FV', NULL, 6, 1),
(3,  '2026-02-19 09:33:40', NULL,         6,  2, 3, 2500000, 0,      0, 0,      'FV', NULL, 2, 3),
(4,  '2026-02-17 09:35:29', NULL,         10, 3, 2, 1800000, 0,      0, 0,      'FV', NULL, 1, 2),
(5,  '2026-02-06 09:36:21', NULL,         8,  4, 4, 1700000, 0,      0, 0,      'FV', NULL, 6, 4),
(6,  '2026-02-16 09:38:46', NULL,         5,  5, 5, 150000,  0,      0, 0,      'FV', NULL, 2, 5),
(7,  '2026-02-27 11:25:22', NULL,         9,  2, 6, 1300000, 0,      0, 0,      'FV', NULL, 2, 3),
(8,  '2026-02-25 11:26:02', NULL,         3,  3, 7, 2500000, 0,      0, 0,      'FV', NULL, 1, 1),
(10, '2026-03-09 01:00:00', NULL,         6,  1, 1, 90000,   0,      0, 0,      'FV', NULL, 1, 3),
(12, '2026-03-13 15:20:00', '2026-03-13', 10, 1, 1, 130900,  110000, 0, 20900,  'FV', NULL, 6, 3),
(13, '2026-03-17 09:38:00', '2026-03-17', 4,  1, 1, 904400,  760000, 0, 144400, 'FV', NULL, 1, 3),
(14, '2026-03-17 09:40:00', '2026-03-17', 1,  1, 1, 113050,  95000,  0, 18050,  'FV', NULL, 2, 1);

-- detalle_factura
INSERT INTO detalle_factura (cantidad, cod_destalle, cod_factura, cod_producto, precio_unitario, subtotal, descuento_porcentaje, descuento_valor, impuesto_porcentaje, impuesto_valor) VALUES
(2, 1, 10, 1, 45000,  90000,  0, 0, 0,  0),
(1, 2, 12, 9, 110000, 110000, 0, 0, 19, 20900),
(1, 3, 13, 9, 110000, 110000, 0, 0, 19, 20900),
(1, 4, 13, 3, 650000, 650000, 0, 0, 19, 123500),
(1, 5, 14, 4, 95000,  95000,  0, 0, 19, 18050);

-- logs
INSERT INTO logs (cod_usuario, id_log, fecha, accion, descripcion) VALUES
(1, 1, '2026-03-01 08:00:15', 'LOGIN',  'User logged in'),
(2, 2, '2026-03-01 08:10:32', 'VIEW',   'Accessed the main dashboard'),
(1, 3, '2026-03-01 08:15:10', 'CREATE', 'Created a new record'),
(3, 4, '2026-03-01 09:02:55', 'LOGIN',  'Successful login'),
(2, 5, '2026-03-01 09:15:45', 'UPDATE', 'Updated information');

-- producto_descuento
INSERT INTO producto_descuento (cod_producto, cod_descuento) VALUES
(1, 55504),
(2, 55506),
(3, 55501),
(6, 55508);

-- clientes (tabla legada)
INSERT INTO clientes (cod_cliente, nombre, tipo_documento, numero_documento, telefono, correo, direccion) VALUES
(2, 'juan perez',    'V', '12345678',   '0412-1234567', 'juan@email.com',  'caracas, venezuela'),
(3, 'María García',  'V', '87654321',   '0414-7654321', 'maria@email.com', 'Valencia, Venezuela'),
(6, 'Diana',         'J', '1005066451', '3107093720',   'diana@gmail.com', 'av10a');

-- productos_descuentos (tabla legada)
INSERT INTO productos_descuentos (cod_descuento, cod_producto) VALUES
(15, 3),
(20, 1);

-- =====================================================
-- AJUSTE DE SECUENCIAS (para que el SERIAL continúe
-- desde el último valor usado en MariaDB)
-- =====================================================

SELECT setval('usuarios_cod_usuario_seq',         6,       false);
SELECT setval('pagos_factura_cod_pago_factura_seq',8,       false);
SELECT setval('metodos_pago_cod_pago_seq',         7,       false);
SELECT setval('impuestos_cod_impuesto_seq',        9,       false);
SELECT setval('customers_customer_id_seq',         13,      false);
SELECT setval('empresas_cod_empresa_seq',          7,       false);
SELECT setval('descuentos_cod_descuento_seq',      665659,  false);
SELECT setval('productos_cod_producto_seq',        11,      false);
SELECT setval('facturas_cod_factura_seq',          15,      false);
SELECT setval('detalle_factura_cod_destalle_seq',  6,       false);
SELECT setval('factura_impuesto_id_factura_impuesto_seq', 1, false);
SELECT setval('logs_id_log_seq',                   6,       false);
SELECT setval('clientes_cod_cliente_seq',          7,       false);

COMMIT;
