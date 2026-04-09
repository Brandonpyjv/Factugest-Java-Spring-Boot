package com.factugest.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * Representación del usuario autenticado dentro de la sesión de Spring Security.
 *
 * La clase User de Spring solo guarda username, password y roles. Para poder
 * mostrar el nombre real del usuario en la interfaz (ej: en el navbar) y tener
 * el rol disponible sin volver a consultar la BD, extendemos User con esos
 * dos campos adicionales.
 *
 * Este objeto vive en el SecurityContext mientras dure la sesión HTTP. Se puede
 * recuperar desde cualquier controlador con:
 *   SecurityContextHolder.getContext().getAuthentication().getPrincipal()
 */
public class CustomUserPrincipal extends User {

    // final porque el principal no debe mutar después de crearse
    private final String nombre;
    private final String rol;

    // ID del usuario en BD — evita volver a consultar la BD en cada request.
    private final Integer codUsuario;

    // ID de la empresa asignada al usuario — usado para preseleccionar la empresa
    // emisora en el formulario de nueva factura. Puede ser null para ADMIN sin empresa.
    private final Integer codEmpresa;

    public CustomUserPrincipal(String username, String password,
                               Collection<? extends GrantedAuthority> authorities,
                               String nombre, String rol,
                               Integer codUsuario, Integer codEmpresa) {
        super(username, password, authorities);
        this.nombre = nombre;
        this.rol = rol;
        this.codUsuario = codUsuario;
        this.codEmpresa = codEmpresa;
    }

    public String getNombre()      { return nombre; }
    public String getRol()         { return rol; }
    public Integer getCodUsuario() { return codUsuario; }
    public Integer getCodEmpresa() { return codEmpresa; }
}
