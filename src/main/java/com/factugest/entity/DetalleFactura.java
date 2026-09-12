package com.factugest.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

/**
 * Entidad JPA que representa una línea de detalle dentro de una factura.
 *
 * Relación: cada Factura tiene 1..N DetalleFactura (uno por producto facturado).
 * cod_factura es la clave foránea que une el detalle con su cabecera.
 *
 * Todos los valores (descuentos, impuestos) se almacenan calculados al momento
 * de crear la factura, no en tiempo real. Esto garantiza que una factura histórica
 * siempre refleje los precios del momento en que se emitió, aunque los precios
 * o IVAs hayan cambiado después.
 *
 * Nota: el nombre de columna tiene un typo en la BD (cod_destalle en vez de cod_detalle).
 * Se conserva para mantener compatibilidad con datos existentes.
 */
@Entity
@Data
@Table(name = "detalle_factura")
public class DetalleFactura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cod_destalle")  // typo histórico en la BD, se mantiene por compatibilidad
    private Integer codDestalle;

    @Column(name = "cod_factura")
    private Integer codFactura;

    @Column(name = "cod_producto")
    private Integer codProducto;

    private Integer cantidad;

    @Column(name = "precio_unitario")
    private BigDecimal precioUnitario;

    // subtotal aquí = base gravable (precio × cant − descuento)
    private BigDecimal subtotal;

    @Column(name = "descuento_porcentaje")
    private BigDecimal descuentoPorcentaje;

    @Column(name = "descuento_valor")
    private BigDecimal descuentoValor;    // valor absoluto del descuento en pesos

    @Column(name = "impuesto_porcentaje")
    private BigDecimal impuestoPorcentaje;

    @Column(name = "impuesto_valor")
    private BigDecimal impuestoValor;    // valor absoluto del IVA en pesos
}
