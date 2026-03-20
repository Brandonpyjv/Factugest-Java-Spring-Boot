package com.factugest.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Data
@Table(name = "impuestos")
public class Impuesto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cod_impuesto")
    private Integer codImpuesto;

    private String descripcion;
    private BigDecimal porcentaje;

    @Column(name = "codigo_dian")
    private String codigoDian;
}
