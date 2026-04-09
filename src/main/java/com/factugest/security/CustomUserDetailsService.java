package com.factugest.security;

import com.factugest.entity.Usuario;
import com.factugest.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Puente entre Spring Security y nuestra tabla de usuarios en base de datos.
 *
 * Spring Security no sabe nada de nuestra BD por sí solo. Esta clase implementa
 * la interfaz UserDetailsService, que le dice a Spring Security "cuando necesites
 * verificar un usuario, llama a mi método loadUserByUsername y yo te devuelvo
 * toda la información que necesitas para autenticar".
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Busca al usuario por su correo electrónico (que en nuestra app hace de "username").
     * Spring Security llama a este método automáticamente durante el proceso de login.
     *
     * @param correo el valor que el usuario escribió en el campo "correo" del formulario
     * @throws UsernameNotFoundException si no existe nadie con ese correo — Spring Security
     *         captura esta excepción y redirige al usuario a /login?error=true
     */
    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + correo));

        // Spring Security maneja roles con el prefijo "ROLE_". Si en BD guardamos "ADMIN",
        // aquí construimos "ROLE_ADMIN" para que .hasRole("ADMIN") en SecurityConfig funcione.
        String role = "ROLE_" + usuario.getRol().toUpperCase();

        // Devolvemos nuestro principal personalizado que extiende el User de Spring.
        // Incluye nombre, rol, codUsuario y codEmpresa para que los controladores
        // puedan usarlos sin volver a consultar la BD en cada request.
        return new CustomUserPrincipal(
                usuario.getCorreo(),
                usuario.getContrasena(),
                List.of(new SimpleGrantedAuthority(role)),
                usuario.getNombre(),
                usuario.getRol(),
                usuario.getCodUsuario(),
                usuario.getCodEmpresa()
        );
    }
}
