package com.factugest.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entidad JPA que representa la empresa emisora de las facturas.
 *
 * En Colombia, una empresa factura con su NIT (Número de Identificación Tributaria)
 * y su dígito de verificación (DV), que se calculan según el algoritmo de la DIAN.
 * El NIT aparece en la factura como: NIT-XXXXXXXXX-DV (ej: NIT-900123456-8).
 *
 * actividad_economica: código CIIU de la actividad (ej: 4711 = supermercados).
 * regimen_tributario: RESPONSABLE_IVA (empresas que cobran IVA) o NO_RESPONSABLE_IVA.
 */
@Entity
@Data
@Table(name = "empresas")
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cod_empresa")
    private Integer codEmpresa;

    private String nombre;

    // NIT tiene constraint UNIQUE: no pueden registrarse dos empresas con el mismo NIT.
    private String nit;

    // DV = dígito de verificación del NIT (un solo carácter: 0-9)
    private String dv;

    private String direccion;
    private String ciudad;
    private String telefono;
    private String correo;

    @Column(name = "regimen_tributario")
    private String regimenTributario;

    // Código CIIU de 4 dígitos de la actividad económica principal
    @Column(name = "actividad_economica")
    private String actividadEconomica;

    // Tipo de documento de la empresa: usualmente "NIT" en Colombia
    @Column(name = "tipo_documento")
    private String tipoDocumento;
}
