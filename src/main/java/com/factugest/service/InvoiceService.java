package com.factugest.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InvoiceService {

    @Autowired
    private JdbcTemplate jdbc;

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

    @Transactional
    public Integer createInvoice(Integer codCliente, Integer codUsuario, Integer codEmpresa,
                                  Integer codMetodoPago, Integer codPago, String fecha,
                                  BigDecimal total, BigDecimal subtotal,
                                  BigDecimal totalDescuentos, BigDecimal totalImpuestos,
                                  String tipoFactura, String observaciones, String fechaVencimiento) {
        jdbc.update("""
            INSERT INTO facturas (fecha, fecha_vencimiento, cod_cliente, cod_usuario, cod_empresa,
                                  cod_metodo_pago, cod_pago, total, subtotal, total_descuentos,
                                  total_impuestos, tipo_factura, observaciones)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
            fecha,
            (fechaVencimiento != null && !fechaVencimiento.isEmpty()) ? fechaVencimiento : null,
            codCliente, codUsuario, codEmpresa, codMetodoPago, codPago,
            total, subtotal, totalDescuentos, totalImpuestos,
            tipoFactura,
            (observaciones != null && !observaciones.isEmpty()) ? observaciones : null
        );
        return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
    }

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

    public void updateInvoiceStatus(Integer invoiceId, Integer codPago) {
        jdbc.update("UPDATE facturas SET cod_pago = ? WHERE cod_factura = ?", codPago, invoiceId);
    }

    @Transactional
    public void deleteInvoice(Integer invoiceId) {
        jdbc.update("DELETE FROM detalle_factura WHERE cod_factura = ?", invoiceId);
        jdbc.update("DELETE FROM facturas WHERE cod_factura = ?", invoiceId);
    }

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

    public Map<String, Object> getProductTaxInfo(Integer codProducto) {
        List<Map<String, Object>> rows = jdbc.queryForList("""
            SELECT p.precio_unitario, COALESCE(i.porcentaje, 0) AS tax_pct
            FROM productos p LEFT JOIN impuestos i ON p.cod_impuesto = i.cod_impuesto
            WHERE p.cod_producto = ?
            """, codProducto);
        return rows.isEmpty() ? null : rows.get(0);
    }
}
