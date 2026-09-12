package com.factugest.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entidad JPA que representa un cliente (persona natural o jurídica).
 *
 * Contexto colombiano:
 *   - document_type: tipo de documento según DIAN — "C" (CC), "E" (CE), "N" (NIT), "P" (Pasaporte)
 *   - tipo_persona: NATURAL (persona física) o JURIDICA (empresa)
 *   - regimen_tributario: NO_RESPONSABLE_IVA (la mayoría de personas) o RESPONSABLE_IVA
 *
 * El nombre de la tabla (customers, en inglés) y su PK (customer_id) vienen de la
 * migración original del sistema, mientras el resto del código está en español.
 */
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

    // VARCHAR(1): un solo carácter que identifica el tipo de documento.
    // Fue VARCHAR(1) para satisfacer Hibernate 6.5 con ddl-auto=validate
    // (antes era CHAR(1)/bpchar que Hibernate no reconocía como String).
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
