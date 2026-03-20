package com.factugest.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class CustomUserPrincipal extends User {

    private final String nombre;
    private final String rol;

    public CustomUserPrincipal(String username, String password,
                               Collection<? extends GrantedAuthority> authorities,
                               String nombre, String rol) {
        super(username, password, authorities);
        this.nombre = nombre;
        this.rol = rol;
    }

    public String getNombre() {
        return nombre;
    }

    public String getRol() {
        return rol;
    }
}
