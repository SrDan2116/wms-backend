package com.aliaga.fittrack.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Deshabilitar CSRF (estándar para APIs REST)
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Configuración de CORS
            .authorizeHttpRequests(auth -> auth
                // 1. Permite acceso LIBRE a Login y Registro
                .requestMatchers("/api/auth/**").permitAll()
                
                // 2. REGLA MAESTRA: Todo lo demás que empiece por /api/ requiere TOKEN.
                // Esto autoriza automáticamente: /api/historial, /api/rutinas, /api/pesos, /api/usuario
                .requestMatchers("/api/**").authenticated()
                
                // 3. Cualquier otra solicitud desconocida también se bloquea por seguridad
                .anyRequest().authenticated()
            )
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Sin estado (JWT)
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        
        // Define aquí las URLs de tu Frontend (Local y Producción)
        config.setAllowedOriginPatterns(List.of(
            "https://candid-cheesecake-ed013b.netlify.app", 
            "http://localhost:4200" 
        ));

        // Métodos HTTP permitidos (Importante incluir DELETE para borrar entrenos)
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}