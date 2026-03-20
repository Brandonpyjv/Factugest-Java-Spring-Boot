package com.factugest.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Data
@Table(name = "descuentos")
public class Descuento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cod_descuento")
    private Integer codDescuento;

    private String descripcion;
    private BigDecimal porcentaje;

    @Column(name = "aplica_a_producto")
    private Integer aplicaAProducto;

    @Column(name = "aplica_a_factura")
    private Integer aplicaAFactura;
}
