package com.factugest.repository;

import com.factugest.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio JPA de usuarios del sistema.
 *
 * El método más crítico es findByCorreo: es la consulta que Spring Security
 * ejecuta en cada intento de login. CustomUserDetailsService lo llama con
 * el correo que el usuario escribió en el formulario.
 *
 * Spring genera: SELECT * FROM usuarios WHERE correo = ? LIMIT 1
 * Devuelve Optional<Usuario> porque el correo podría no existir en BD.
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    // Busca un usuario por correo electrónico. Usado en el proceso de autenticación.
    Optional<Usuario> findByCorreo(String correo);
}
