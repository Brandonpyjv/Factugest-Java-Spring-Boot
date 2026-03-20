package com.factugest.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "facturas")
public class Factura {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cod_factura")
    private Integer codFactura;

    private LocalDateTime fecha;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column(name = "cod_cliente")
    private Integer codCliente;

    @Column(name = "cod_usuario")
    private Integer codUsuario;

    @Column(name = "cod_empresa")
    private Integer codEmpresa;

    @Column(name = "cod_metodo_pago")
    private Integer codMetodoPago;

    @Column(name = "cod_pago")
    private Integer codPago;

    private BigDecimal total;
    private BigDecimal subtotal;

    @Column(name = "total_descuentos")
    private BigDecimal totalDescuentos;

    @Column(name = "total_impuestos")
    private BigDecimal totalImpuestos;

    @Column(name = "tipo_factura")
    private String tipoFactura;

    private String observaciones;
}
