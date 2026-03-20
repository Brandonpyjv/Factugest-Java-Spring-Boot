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

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + correo));

        String role = "ROLE_" + usuario.getRol().toUpperCase();

        return new CustomUserPrincipal(
                usuario.getCorreo(),
                usuario.getContrasena(),
                List.of(new SimpleGrantedAuthority(role)),
                usuario.getNombre(),
                usuario.getRol()
        );
    }
}
