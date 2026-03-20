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
 * Migra las contraseñas existentes en texto plano a BCrypt al arrancar la aplicación.
 * Solo procesa contraseñas que aún no están hasheadas con BCrypt.
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
