package com.factugest.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "logs")
public class Log {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_log")
    private Integer idLog;

    @Column(name = "cod_usuario")
    private Integer codUsuario;

    private String accion;
    private String descripcion;
    private LocalDateTime fecha;
}
