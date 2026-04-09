package com.factugest.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración central de seguridad de la aplicación.
 *
 * Toda petición HTTP pasa primero por la cadena de filtros que se define aquí.
 * Spring Security actúa como un portero: decide quién entra, a qué rutas,
 * cómo se autentica y qué ocurre al cerrar sesión.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // CustomUserDetailsService es quien sabe cómo cargar un usuario desde la BD.
    // Se inyecta por constructor (más explícito que @Autowired en campo).
    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * Define las reglas de acceso y el comportamiento de login/logout.
     *
     * @Bean hace que Spring gestione el ciclo de vida de este objeto
     * (lo crea una sola vez y lo reutiliza en toda la app).
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Usa nuestro proveedor personalizado en vez del default de Spring
            .authenticationProvider(authenticationProvider())

            // Reglas de autorización: quién puede acceder a qué URL
            .authorizeHttpRequests(auth -> auth
                // Recursos estáticos y la página de login son públicos (no requieren sesión)
                .requestMatchers("/css/**", "/img/**", "/js/**", "/login", "/logout-success").permitAll()
                // Gestión de usuarios y logs solo para ADMIN
                .requestMatchers("/users/**", "/logs/**").hasRole("ADMIN")
                // Todo lo demás requiere estar autenticado
                .anyRequest().authenticated()
            )

            // Configuración del formulario de login propio (no el de Spring por defecto)
            .formLogin(form -> form
                .loginPage("/login")                        // GET /login → muestra el formulario
                .defaultSuccessUrl("/", true)               // al autenticarse → redirige al dashboard
                .failureUrl("/login?error=true")            // si falla → vuelve al login con param de error
                .usernameParameter("correo")                // nombre del campo en el HTML (no "username")
                .passwordParameter("contrasena")            // nombre del campo en el HTML (no "password")
                .permitAll()
            )

            // Configuración del logout
            .logout(logout -> logout
                .logoutUrl("/logout")                       // POST a /logout ejecuta el logout
                .logoutSuccessUrl("/logout-success")        // redirige a la página de sesión cerrada
                .invalidateHttpSession(true)                // destruye la sesión HTTP del servidor
                .clearAuthentication(true)                  // elimina el principal del contexto de seguridad
                .permitAll()
            );

        return http.build();
    }

    /**
     * BCrypt es el algoritmo estándar para contraseñas: aplica un salt aleatorio
     * y múltiples rondas de hash, haciendo que los ataques de fuerza bruta sean
     * computacionalmente inviables. Factor de costo por defecto: 10 rondas.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Conecta el proveedor de autenticación con nuestra fuente de usuarios (BD)
     * y el algoritmo de verificación de contraseñas (BCrypt).
     *
     * Cuando el usuario envía el formulario, Spring llama a este provider que:
     * 1. Busca al usuario por correo en la BD (via userDetailsService)
     * 2. Verifica la contraseña enviada contra el hash almacenado (via passwordEncoder)
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
}
