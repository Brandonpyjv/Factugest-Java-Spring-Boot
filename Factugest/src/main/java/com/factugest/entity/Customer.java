package com.factugest.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "customers")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Integer customerId;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "document_type")
    private String documentType;

    @Column(name = "document_number")
    private String documentNumber;

    private String phone;
    private String email;
    private String address;
    private String ciudad;
    private String departamento;
    private String pais;

    @Column(name = "tipo_persona")
    private String tipoPersona;

    @Column(name = "regimen_tributario")
    private String regimenTributario;
}
