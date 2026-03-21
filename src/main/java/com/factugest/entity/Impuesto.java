package com.factugest.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

/**
 * Entidad JPA que representa un tipo de impuesto (IVA, excluido, etc.).
 *
 * Colombia maneja principalmente tres tarifas de IVA para bienes y servicios:
 *   - 19% (tarifa general): la mayoría de productos y servicios
 *   - 5%  (tarifa diferencial): algunos alimentos, medicamentos
 *   - 0%  (excluido/exento): productos de la canasta familiar, servicios de salud
 *
 * codigo_dian es el código estándar DIAN para reportes de facturación electrónica.
 * Ejemplo: "01" = IVA, "04" = INC (Impuesto Nacional al Consumo).
 */
@Entity
@Data
@Table(name = "impuestos")
public class Impuesto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cod_impuesto")
    private Integer codImpuesto;

    // Nombre descriptivo: "IVA 19%", "IVA 5%", "Exento"
    private String descripcion;

    // Valor numérico del porcentaje: 19.00, 5.00, 0.00
    private BigDecimal porcentaje;

    // Código estándar DIAN para el XML de factura electrónica
    @Column(name = "codigo_dian")
    private String codigoDian;
}
