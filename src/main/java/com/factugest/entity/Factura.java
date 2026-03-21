package com.factugest.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * Entidad JPA que representa la cabecera de una factura.
 *
 * En facturación DIAN, una factura tiene dos partes:
 *   - Cabecera (esta clase): datos generales — fechas, cliente, empresa, totales, tipo.
 *   - Líneas de detalle: cada producto/servicio facturado (ver DetalleFactura).
 *
 * @Entity indica a Hibernate que esta clase mapea a una tabla real en BD.
 * @Table(name="facturas") especifica el nombre exacto de la tabla.
 * @Data (Lombok) genera automáticamente: getters, setters, toString, equals, hashCode.
 * Sin Lombok habría que escribir unos 80 métodos repetitivos a mano.
 */
@Entity
@Data
@Table(name = "facturas")
public class Factura {

    /**
     * @Id marca esta variable como clave primaria.
     * @GeneratedValue(IDENTITY) = PostgreSQL gestiona el auto-incremento (SERIAL).
     * @Column(name="cod_factura") mapea el campo Java al nombre real de la columna en BD.
     * Sin @Column, Hibernate buscaría una columna llamada "codFactura" (camelCase),
     * pero en BD usamos snake_case (cod_factura).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cod_factura")
    private Integer codFactura;

    // Timestamp completo de emisión: fecha + hora + minutos + segundos.
    // LocalDateTime = fecha+hora sin zona horaria (la BD guarda TIMESTAMP WITHOUT TIME ZONE).
    private LocalDateTime fecha;

    // Solo la parte de fecha (sin hora). LocalDate mapea a DATE en PostgreSQL.
    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    // Claves foráneas almacenadas como Integer simples.
    // Usamos Integer (objeto) y no int (primitivo) para que puedan ser null
    // cuando el registro aún no tiene cliente asignado.
    @Column(name = "cod_cliente")
    private Integer codCliente;

    @Column(name = "cod_usuario")
    private Integer codUsuario;

    @Column(name = "cod_empresa")
    private Integer codEmpresa;

    @Column(name = "cod_metodo_pago")
    private Integer codMetodoPago;

    @Column(name = "cod_pago")   // estado de pago: pagada, pendiente, vencida...
    private Integer codPago;

    // BigDecimal en vez de double/float para valores monetarios.
    // Los tipos float tienen errores de precisión (0.1 + 0.2 ≠ 0.3 en binario).
    // BigDecimal es exacto y es el estándar para dinero en Java.
    private BigDecimal total;
    private BigDecimal subtotal;

    @Column(name = "total_descuentos")
    private BigDecimal totalDescuentos;

    @Column(name = "total_impuestos")
    private BigDecimal totalImpuestos;

    // Tipo DIAN: FV (Factura de Venta), ND (Nota Débito), NC (Nota Crédito)
    @Column(name = "tipo_factura")
    private String tipoFactura;

    private String observaciones;
}
