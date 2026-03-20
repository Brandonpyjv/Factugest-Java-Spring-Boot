package com.factugest.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "empresas")
public class Empresa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cod_empresa")
    private Integer codEmpresa;

    private String nombre;
    private String nit;
    private String dv;
    private String direccion;
    private String ciudad;
    private String telefono;
    private String correo;

    @Column(name = "regimen_tributario")
    private String regimenTributario;

    @Column(name = "actividad_economica")
    private String actividadEconomica;

    @Column(name = "tipo_documento")
    private String tipoDocumento;
}
