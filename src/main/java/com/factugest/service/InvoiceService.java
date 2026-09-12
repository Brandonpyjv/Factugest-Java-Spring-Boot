package com.factugest.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio de facturación: toda la lógica de negocio relacionada con facturas vive aquí.
 *
 * A diferencia del resto de servicios que usan JPA/Repository, este usa JdbcTemplate
 * directamente. La razón: las consultas de facturas son JOINs complejos entre 5-6 tablas
 * que devuelven mapas de datos mixtos (no entidades limpias). JdbcTemplate da control
 * total sobre el SQL y devuelve List<Map<String, Object>>, ideal para estos casos.
 *
 * @Transactional en los métodos de escritura garantiza que si falla algún INSERT,
 * todo el grupo de operaciones se revierte (atomicidad).
 */
@Service
public class InvoiceService {

    @Autowired
    private JdbcTemplate jdbc;

    /**
     * Lista todas las facturas con datos denormalizados para mostrar en la tabla principal.
     * Los LEFT JOINs aseguran que aparezcan incluso si alguna FK apunta a un registro eliminado.
     */
    public List<Map<String, Object>> getAllInvoicesDetailed() {
        return jdbc.queryForList("""
            SELECT f.cod_factura, f.fecha, f.fecha_vencimiento, f.total, f.subtotal,
                   f.total_descuentos, f.total_impuestos, f.tipo_factura,
                   f.cod_metodo_pago, f.cod_pago, f.observaciones,
                   c.full_name AS cliente, c.document_number AS cliente_doc,
                   u.nombre AS usuario, e.nombre AS empresa,
                   mp.descripcion AS metodo_pago, pf.status AS estado_pago
            FROM facturas f
                LEFT JOIN customers c ON f.cod_cliente = c.customer_id
                LEFT JOIN usuarios u ON f.cod_usuario = u.cod_usuario
                LEFT JOIN empresas e ON f.cod_empresa = e.cod_empresa
                LEFT JOIN metodos_pago mp ON f.cod_metodo_pago = mp.cod_pago
                LEFT JOIN pagos_factura pf ON f.cod_pago = pf.cod_pago_factura
            ORDER BY f.fecha DESC
            """);
    }

    /**
     * Carga una factura con todos sus datos relacionados para la vista de detalle y el PDF.
     * Incluye datos completos del cliente y la empresa emisora para el encabezado del documento.
     *
     * @return null si no existe la factura (el controlador maneja la redirección)
     */
    public Map<String, Object> getInvoiceById(Integer invoiceId) {
        List<Map<String, Object>> rows = jdbc.queryForList("""
            SELECT f.cod_factura, f.fecha, f.fecha_vencimiento, f.total, f.subtotal,
                   f.total_descuentos, f.total_impuestos, f.tipo_factura,
                   f.cod_metodo_pago, f.cod_pago, f.observaciones,
                   c.full_name AS cliente_nombre, c.document_type, c.document_number,
                   c.phone AS cliente_phone, c.email AS cliente_email,
                   c.address AS cliente_address, c.ciudad AS cliente_ciudad,
                   c.tipo_persona, c.regimen_tributario AS cliente_regimen,
                   u.nombre AS usuario_nombre,
                   e.nombre AS empresa_nombre, e.nit AS empresa_nit, e.dv AS empresa_dv,
                   e.direccion AS empresa_direccion, e.ciudad AS empresa_ciudad,
                   e.telefono AS empresa_telefono, e.correo AS empresa_correo,
                   e.regimen_tributario AS empresa_regimen, e.actividad_economica,
                   mp.descripcion AS metodo_pago_nombre, pf.status AS estado_pago
            FROM facturas f
                LEFT JOIN customers c ON f.cod_cliente = c.customer_id
                LEFT JOIN usuarios u ON f.cod_usuario = u.cod_usuario
                LEFT JOIN empresas e ON f.cod_empresa = e.cod_empresa
                LEFT JOIN metodos_pago mp ON f.cod_metodo_pago = mp.cod_pago
                LEFT JOIN pagos_factura pf ON f.cod_pago = pf.cod_pago_factura
            WHERE f.cod_factura = ?
            """, invoiceId);
        return rows.isEmpty() ? null : rows.get(0);
    }

    /**
     * Líneas de detalle de una factura: cada producto con precios, descuentos e impuestos.
     * El JOIN con impuestos trae el nombre y código DIAN del tributo para el PDF.
     */
    public List<Map<String, Object>> getInvoiceDetails(Integer invoiceId) {
        return jdbc.queryForList("""
            SELECT d.cod_destalle, d.cod_factura, d.cod_producto, d.cantidad,
                   d.precio_unitario, d.subtotal, d.descuento_porcentaje, d.descuento_valor,
                   d.impuesto_porcentaje, d.impuesto_valor,
                   p.nombre AS producto_nombre, p.sku, p.unidad_medida,
                   i.descripcion AS impuesto_nombre, i.codigo_dian AS impuesto_codigo_dian
            FROM detalle_factura d
                LEFT JOIN productos p ON d.cod_producto = p.cod_producto
                LEFT JOIN impuestos i ON p.cod_impuesto = i.cod_impuesto
            WHERE d.cod_factura = ?
            ORDER BY d.cod_destalle
            """, invoiceId);
    }

    /**
     * Inserta la cabecera de una factura y devuelve el ID generado por PostgreSQL.
     *
     * RETURNING cod_factura es sintaxis PostgreSQL: en un solo statement hace el INSERT
     * y devuelve el valor del SERIAL recién generado, evitando una segunda consulta.
     *
     * @Transactional: si falla el INSERT del descuento de factura, ambas operaciones
     * se revierten juntas.
     */
    @Transactional
    public Integer createInvoice(Integer codCliente, Integer codUsuario, Integer codEmpresa,
                                  Integer codMetodoPago, Integer codPago,
                                  BigDecimal total, BigDecimal subtotal,
                                  BigDecimal totalDescuentos, BigDecimal totalImpuestos,
                                  String tipoFactura, String observaciones,
                                  Integer codDescuentoFactura, BigDecimal valorDescuentoFactura) {

        LocalDateTime now   = LocalDateTime.now();
        LocalDate today     = LocalDate.now();

        Integer invoiceId = jdbc.queryForObject("""
            INSERT INTO facturas (fecha, fecha_vencimiento, cod_cliente, cod_usuario, cod_empresa,
                                  cod_metodo_pago, cod_pago, total, subtotal, total_descuentos,
                                  total_impuestos, tipo_factura, observaciones)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING cod_factura
            """,
            Integer.class,
            now, today,
            codCliente, codUsuario, codEmpresa, codMetodoPago, codPago,
            total, subtotal, totalDescuentos, totalImpuestos,
            tipoFactura,
            // Guardar null en lugar de cadena vacía para mantener consistencia en la BD
            (observaciones != null && !observaciones.isEmpty()) ? observaciones : null
        );

        // Si hay descuento a nivel de factura, registrarlo en la tabla puente
        if (codDescuentoFactura != null && valorDescuentoFactura != null
                && valorDescuentoFactura.compareTo(BigDecimal.ZERO) > 0) {
            jdbc.update(
                "INSERT INTO factura_descuento (cod_descuento, cod_factura, valor_descuento) VALUES (?,?,?)",
                codDescuentoFactura, invoiceId, valorDescuentoFactura
            );
        }

        return invoiceId;
    }

    /** Inserta una línea de detalle de factura con todos sus valores fiscales calculados. */
    @Transactional
    public void createInvoiceDetail(Integer codFactura, Integer codProducto, Integer cantidad,
                                     BigDecimal precioUnitario, BigDecimal subtotal,
                                     BigDecimal descuentoPorcentaje, BigDecimal descuentoValor,
                                     BigDecimal impuestoPorcentaje, BigDecimal impuestoValor) {
        jdbc.update("""
            INSERT INTO detalle_factura (cod_factura, cod_producto, cantidad, precio_unitario,
                                         subtotal, descuento_porcentaje, descuento_valor,
                                         impuesto_porcentaje, impuesto_valor)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
            codFactura, codProducto, cantidad, precioUnitario, subtotal,
            descuentoPorcentaje, descuentoValor, impuestoPorcentaje, impuestoValor
        );
    }

    /** Actualiza solo el estado de pago de una factura (sin recalcular totales). */
    public void updateInvoiceStatus(Integer invoiceId, Integer codPago) {
        jdbc.update("UPDATE facturas SET cod_pago = ? WHERE cod_factura = ?", codPago, invoiceId);
    }

    /**
     * Elimina una factura completa respetando el orden de las FK:
     * primero los registros hijos (descuentos y detalles), luego la cabecera.
     * Sin este orden, PostgreSQL lanzaría un error de violación de clave foránea.
     */
    @Transactional
    public void deleteInvoice(Integer invoiceId) {
        jdbc.update("DELETE FROM factura_descuento WHERE cod_factura = ?", invoiceId);
        jdbc.update("DELETE FROM detalle_factura WHERE cod_factura = ?", invoiceId);
        jdbc.update("DELETE FROM facturas WHERE cod_factura = ?", invoiceId);
    }

    /**
     * Estadísticas para el dashboard: totales, ingresos, clientes, stock bajo mínimo.
     * COALESCE(SUM(...), 0) evita NULL cuando no hay filas (en vez de mostrar "null" en la UI).
     */
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total_facturas", jdbc.queryForObject(
            "SELECT COUNT(*) FROM facturas WHERE tipo_factura = 'FV'", Long.class));
        stats.put("ingresos_cobrados", jdbc.queryForObject(
            "SELECT COALESCE(SUM(total), 0) FROM facturas WHERE cod_pago = 1 AND tipo_factura = 'FV'", BigDecimal.class));
        stats.put("total_clientes", jdbc.queryForObject(
            "SELECT COUNT(*) FROM customers", Long.class));
        stats.put("productos_activos", jdbc.queryForObject(
            "SELECT COUNT(*) FROM productos WHERE activo = 1", Long.class));
        stats.put("facturas_pendientes", jdbc.queryForObject(
            "SELECT COALESCE(SUM(total), 0) FROM facturas WHERE cod_pago = 2 AND tipo_factura = 'FV'", BigDecimal.class));
        stats.put("productos_bajo_stock", jdbc.queryForObject(
            "SELECT COUNT(*) FROM productos WHERE stock <= stock_minimo AND activo = 1", Long.class));
        stats.put("facturas_recientes", jdbc.queryForList("""
            SELECT f.cod_factura, f.fecha, f.total, f.tipo_factura,
                   c.full_name AS cliente, pf.status AS estado_pago
            FROM facturas f
                LEFT JOIN customers c ON f.cod_cliente = c.customer_id
                LEFT JOIN pagos_factura pf ON f.cod_pago = pf.cod_pago_factura
            ORDER BY f.fecha DESC LIMIT 8
            """));
        return stats;
    }

    /**
     * Devuelve el precio y el porcentaje de IVA de un producto para recalcularlos
     * en el servidor (no se fían los valores enviados desde el formulario).
     */
    public Map<String, Object> getProductTaxInfo(Integer codProducto) {
        List<Map<String, Object>> rows = jdbc.queryForList("""
            SELECT p.precio_unitario, COALESCE(i.porcentaje, 0) AS tax_pct
            FROM productos p LEFT JOIN impuestos i ON p.cod_impuesto = i.cod_impuesto
            WHERE p.cod_producto = ?
            """, codProducto);
        return rows.isEmpty() ? null : rows.get(0);
    }

    // ── Búsquedas para el autocompletado del formulario ───────────────────────

    /**
     * Búsqueda de clientes por nombre o número de documento.
     * LOWER() garantiza búsqueda insensible a mayúsculas sin depender de la collation de BD.
     * LIMIT 10 evita que una búsqueda muy general devuelva miles de registros.
     */
    public List<Map<String, Object>> searchCustomers(String q) {
        String like = "%" + q.toLowerCase() + "%";
        return jdbc.queryForList("""
            SELECT customer_id, full_name, document_type, document_number
            FROM customers
            WHERE LOWER(full_name) LIKE ? OR LOWER(document_number) LIKE ?
            ORDER BY full_name
            LIMIT 10
            """, like, like);
    }

    /**
     * Búsqueda de productos activos por SKU o nombre. Solo devuelve productos
     * con activo=1 para no ofrecer artículos descontinuados en nuevas facturas.
     */
    public List<Map<String, Object>> searchProductos(String q) {
        String like = "%" + q.toLowerCase() + "%";
        return jdbc.queryForList("""
            SELECT p.cod_producto, p.sku, p.nombre, p.precio_unitario,
                   COALESCE(i.porcentaje, 0) AS tax_porcentaje
            FROM productos p LEFT JOIN impuestos i ON p.cod_impuesto = i.cod_impuesto
            WHERE p.activo = 1 AND (LOWER(p.sku) LIKE ? OR LOWER(p.nombre) LIKE ?)
            ORDER BY p.nombre
            LIMIT 10
            """, like, like);
    }

    /** Descuentos habilitados para un producto específico (tabla puente producto_descuento). */
    public List<Map<String, Object>> getProductDiscounts(Integer codProducto) {
        return jdbc.queryForList("""
            SELECT d.cod_descuento, d.descripcion, d.porcentaje
            FROM producto_descuento pd
            JOIN descuentos d ON pd.cod_descuento = d.cod_descuento
            WHERE pd.cod_producto = ?
            ORDER BY d.descripcion
            """, codProducto);
    }

    /**
     * Descuentos aplicables a nivel de factura completa (no por producto).
     * Se distinguen por la columna aplica_a_factura = 1.
     */
    public List<Map<String, Object>> getInvoiceDiscounts() {
        return jdbc.queryForList("""
            SELECT cod_descuento, descripcion, porcentaje
            FROM descuentos
            WHERE aplica_a_factura = 1
            ORDER BY descripcion
            """);
    }
}
