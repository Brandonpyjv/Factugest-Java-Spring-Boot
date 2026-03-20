package com.factugest.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Data
@Table(name = "productos")
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cod_producto")
    private Integer codProducto;

    private String sku;
    private String nombre;
    private String descripcion;

    @Column(name = "precio_unitario")
    private BigDecimal precioUnitario;

    private Integer stock;

    @Column(name = "stock_minimo")
    private Integer stockMinimo;

    @Column(name = "cod_impuesto")
    private Integer codImpuesto;

    @Column(name = "unidad_medida")
    private String unidadMedida;

    @Column(name = "codigo_barras")
    private String codigoBarras;

    private Integer activo;
}
