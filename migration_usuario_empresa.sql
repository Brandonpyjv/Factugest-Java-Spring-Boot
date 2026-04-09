-- ============================================================
-- MIGRACIÓN: Asignación de empresa/sucursal por usuario
-- Proyecto:  Factugest Java Spring Boot
-- Fecha:     2026-04-07
-- Autor:     wcrangel
-- ============================================================
-- Propósito: Agrega la columna cod_empresa a la tabla usuarios
-- para que cada usuario quede vinculado a su sucursal/empresa.
-- Esto permite preseleccionar la empresa emisora automáticamente
-- en el formulario de nueva factura.
-- ============================================================

-- 1. Agregar columna cod_empresa a usuarios
--    - NULLABLE para que los usuarios ADMIN puedan existir sin empresa asignada.
--    - FK referencia a empresas(cod_empresa) con restricción de integridad.
ALTER TABLE usuarios
    ADD COLUMN cod_empresa INTEGER REFERENCES empresas(cod_empresa);

-- 2. (Opcional) Si ya existen usuarios y quieres asignarlos a una empresa,
--    actualiza manualmente según corresponda. Ejemplo:
--
--    UPDATE usuarios SET cod_empresa = 1 WHERE correo = 'cajero@empresa.com';
--    UPDATE usuarios SET cod_empresa = 1 WHERE correo = 'vendedor@empresa.com';
--
--    Los usuarios ADMIN pueden quedar con cod_empresa = NULL.

-- 3. Verifica que la columna fue creada correctamente:
--    SELECT cod_usuario, nombre, correo, rol, cod_empresa FROM usuarios;
