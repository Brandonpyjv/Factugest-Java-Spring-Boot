package com.factugest.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "metodos_pago")
public class MetodoPago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cod_pago")
    private Integer codPago;

    private String descripcion;
    private String nombre;
}
