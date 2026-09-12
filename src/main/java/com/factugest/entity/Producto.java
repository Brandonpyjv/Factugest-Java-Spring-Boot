package com.factugest.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

/**
 * Entidad JPA que representa un producto o servicio del catálogo.
 *
 * El campo 'activo' funciona como un "borrado lógico": en vez de eliminar el
 * producto de la BD (lo que rompería el histórico de facturas que lo referencian),
 * se pone activo=0 para ocultarlo de los formularios nuevos mientras conserva
 * la trazabilidad histórica.
 */
@Entity
@Data
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cod_producto")
    private Integer codProducto;

    // SKU (Stock Keeping Unit): código único del producto dentro del negocio.
    // Tiene constraint UNIQUE en BD para evitar duplicados.
    private String sku;

    private String nombre;
    private String descripcion;

    // BigDecimal para precio: precisión exacta en operaciones monetarias.
    // La columna en BD es NUMERIC(12,2): hasta 10 dígitos enteros, 2 decimales.
    @Column(name = "precio_unitario")
    private BigDecimal precioUnitario;

    // stock: unidades disponibles actualmente en inventario
    private Integer stock;

    // stock_minimo: umbral de alerta. Si stock <= stock_minimo, aparece en dashboard
    @Column(name = "stock_minimo")
    private Integer stockMinimo;

    // FK a la tabla impuestos: qué IVA aplica a este producto (0%, 5%, 19%)
    @Column(name = "cod_impuesto")
    private Integer codImpuesto;

    // Código UNECE de unidad de medida para facturación electrónica DIAN.
    // Ejemplo: C62 = unidad, KGM = kilogramo, LTR = litro.
    @Column(name = "unidad_medida")
    private String unidadMedida;

    @Column(name = "codigo_barras")
    private String codigoBarras;

    // 1 = activo (visible en catálogo), 0 = inactivo (oculto, borrado lógico).
    // Se usa Integer en vez de boolean porque la BD originalmente era MariaDB
    // y usaba TINYINT(1) en lugar de BOOLEAN nativo.
    private Integer activo;
}
