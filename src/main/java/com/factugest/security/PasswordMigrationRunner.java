package com.factugest.security;

import com.factugest.entity.Usuario;
import com.factugest.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Migración automática de contraseñas al arrancar la aplicación.
 *
 * Problema que resuelve: la BD originalmente tenía contraseñas en texto plano.
 * Al activar Spring Security con BCrypt, esas contraseñas dejaron de funcionar
 * porque BCrypt espera hashes, no texto plano.
 *
 * ApplicationRunner es una interfaz de Spring Boot que ejecuta el método run()
 * UNA SOLA VEZ justo después de que el contexto de la aplicación esté listo
 * (servidor arriba, BD conectada, todos los beans inicializados).
 *
 * Es idempotente: si la contraseña ya fue migrada (empieza con $2a$ o $2b$,
 * que son los prefijos del formato BCrypt), la saltamos. Así puede ejecutarse
 * en cada arranque sin riesgo de romper contraseñas ya hasheadas.
 */
@Component
public class PasswordMigrationRunner implements ApplicationRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        List<Usuario> usuarios = usuarioRepository.findAll();
        boolean migrated = false;

        for (Usuario u : usuarios) {
            String pwd = u.getContrasena();

            // Los hashes BCrypt siempre empiezan con "$2a$" o "$2b$".
            // Si la contraseña no cumple ese patrón, todavía está en texto plano.
            if (pwd != null && !pwd.startsWith("$2a$") && !pwd.startsWith("$2b$")) {
                u.setContrasena(passwordEncoder.encode(pwd));
                usuarioRepository.save(u);
                migrated = true;
            }
        }

        if (migrated) {
            System.out.println("[Factugest] Migración de contraseñas completada: las contraseñas han sido hasheadas con BCrypt.");
        }
    }
}
