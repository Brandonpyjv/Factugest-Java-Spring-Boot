package com.factugest.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entidad JPA que representa un usuario del sistema.
 *
 * Los usuarios tienen dos roles posibles: "ADMIN" y "USER".
 * El rol determina qué rutas puede visitar (ver SecurityConfig.java).
 *
 * La contraseña se almacena hasheada con BCrypt (nunca en texto plano).
 * PasswordMigrationRunner se encarga de migrar contraseñas antiguas al arrancar.
 */
@Entity
@Data
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cod_usuario")
    private Integer codUsuario;

    // Nombre para mostrar en la interfaz (ej: "Brandon")
    private String nombre;

    // Correo electrónico: sirve como username para el login.
    // Tiene constraint UNIQUE en BD porque no pueden existir dos cuentas con el mismo correo.
    private String correo;

    // Hash BCrypt de la contraseña. Nunca se almacena la contraseña real.
    // Formato: $2a$10$<22 chars de salt><31 chars de hash>
    private String contrasena;

    // Rol en mayúsculas: "ADMIN" o "USER".
    // Spring Security lo convierte a "ROLE_ADMIN" o "ROLE_USER" internamente.
    private String rol;
}
