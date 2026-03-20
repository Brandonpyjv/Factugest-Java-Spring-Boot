package com.factugest.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Data
@Table(name = "detalle_factura")
public class DetalleFactura {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cod_destalle")
    private Integer codDestalle;

    @Column(name = "cod_factura")
    private Integer codFactura;

    @Column(name = "cod_producto")
    private Integer codProducto;

    private Integer cantidad;

    @Column(name = "precio_unitario")
    private BigDecimal precioUnitario;

    private BigDecimal subtotal;

    @Column(name = "descuento_porcentaje")
    private BigDecimal descuentoPorcentaje;

    @Column(name = "descuento_valor")
    private BigDecimal descuentoValor;

    @Column(name = "impuesto_porcentaje")
    private BigDecimal impuestoPorcentaje;

    @Column(name = "impuesto_valor")
    private BigDecimal impuestoValor;
}
