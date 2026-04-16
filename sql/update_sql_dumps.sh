#!/usr/bin/env bash
# ============================================================
# update_sql_dumps.sh
# Actualiza postgres_migration.sql y reseed_data.sql
# extrayendo el esquema y datos reales del contenedor Docker.
#
# Uso:
#   ./sql/update_sql_dumps.sh
#
# Requiere: docker con el contenedor "postgresy" corriendo.
# ============================================================

set -euo pipefail

CONTAINER="postgresy"
DB_USER="postgres"
DB_NAME="factugest"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TIMESTAMP=$(date +"%Y-%m-%d %H:%M:%S")

echo "[$TIMESTAMP] Iniciando actualización de dumps SQL..."

# ---- Verificar que el contenedor esté corriendo ----
if ! docker ps --format '{{.Names}}' | grep -q "^${CONTAINER}$"; then
    echo "ERROR: El contenedor '${CONTAINER}' no está corriendo." >&2
    exit 1
fi

# ============================================================
# 1. postgres_migration.sql  →  DDL + datos (desde cero)
# ============================================================
echo "  Generando postgres_migration.sql ..."

{
cat <<HEADER
-- =====================================================
-- FACTUGEST - Esquema + datos PostgreSQL
-- Generado automáticamente: ${TIMESTAMP}
-- Compatibilidad: Spring Boot 3.3.5 / Hibernate 6.5 / Java 21
-- =====================================================

HEADER

# DDL limpio: sin comentarios de versión, sin SET de permisos
docker exec "${CONTAINER}" pg_dump \
    --username="${DB_USER}" \
    --dbname="${DB_NAME}" \
    --schema-only \
    --no-owner \
    --no-acl \
    --no-comments \
    --schema=public

echo ""
echo "-- ====================================================="
echo "-- DATOS"
echo "-- ====================================================="
echo ""

# Datos en orden seguro respecto a FKs
for TABLE in \
    usuarios pagos_factura metodos_pago impuestos \
    customers empresas descuentos productos \
    facturas detalle_factura factura_descuento factura_impuesto \
    logs producto_descuento clientes configuracion productos_descuentos
do
    docker exec "${CONTAINER}" pg_dump \
        --username="${DB_USER}" \
        --dbname="${DB_NAME}" \
        --data-only \
        --no-owner \
        --no-acl \
        --no-comments \
        --table="public.${TABLE}" \
        2>/dev/null || true
done

} > "${SCRIPT_DIR}/postgres_migration.sql"

echo "  postgres_migration.sql actualizado."

# ============================================================
# 2. reseed_data.sql  →  TRUNCATE + INSERT (sólo datos)
# ============================================================
echo "  Generando reseed_data.sql ..."

{
cat <<HEADER
-- ============================================================
-- FACTUGEST - Re-seed completo de datos PostgreSQL
-- Generado automáticamente: ${TIMESTAMP}
-- Ejecutar contra: jdbc:postgresql://localhost:5432/factugest
-- ============================================================

BEGIN;

-- Vaciar todas las tablas (CASCADE resuelve FKs)
TRUNCATE TABLE
    detalle_factura, factura_descuento, factura_impuesto,
    logs, producto_descuento, facturas, customers, empresas,
    usuarios, metodos_pago, pagos_factura, productos, descuentos,
    impuestos, clientes, configuracion, productos_descuentos
RESTART IDENTITY CASCADE;

HEADER

# Insertar datos en orden seguro
for TABLE in \
    usuarios pagos_factura metodos_pago impuestos \
    customers empresas descuentos productos \
    facturas detalle_factura factura_descuento factura_impuesto \
    logs producto_descuento clientes configuracion productos_descuentos
do
    docker exec "${CONTAINER}" pg_dump \
        --username="${DB_USER}" \
        --dbname="${DB_NAME}" \
        --data-only \
        --no-owner \
        --no-acl \
        --no-comments \
        --disable-triggers \
        --table="public.${TABLE}" \
        2>/dev/null || true
    echo ""
done

echo "COMMIT;"

} > "${SCRIPT_DIR}/reseed_data.sql"

echo "  reseed_data.sql actualizado."
echo "[$TIMESTAMP] Listo. Archivos en: ${SCRIPT_DIR}/"
