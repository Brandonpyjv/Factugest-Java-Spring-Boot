package com.factugest.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "pagos_factura")
public class PagoFactura {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cod_pago_factura")
    private Integer codPagoFactura;

    private String status;
}
