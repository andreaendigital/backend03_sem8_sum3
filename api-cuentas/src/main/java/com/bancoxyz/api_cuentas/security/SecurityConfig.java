package com.bancoxyz.api_cuentas.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración de Spring Security — OAuth2 Resource Server.
 *
 * api-cuentas actúa como Resource Server: acepta tokens JWT emitidos
 * por auth-server y los valida usando el JWK Set publicado en
 * ${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}.
 *
 * Cambios respecto a Semana 6/7:
 *   - Se eliminaron JwtUtil, JwtAuthFilter y AuthController (JWT manual).
 *   - Se eliminó el endpoint POST /auth/token (ahora está en auth-server).
 *   - La autenticación la gestiona Spring Security automáticamente via
 *     .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults())).
 *   - Rutas públicas: /actuator/** (health check Docker).
 *   - Todo lo demás requiere token OAuth2 Bearer válido.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm ->
                sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Health check accesible desde Docker sin token
                .requestMatchers("/actuator/**").permitAll()
                // Todos los demás endpoints requieren token OAuth2 válido
                .anyRequest().authenticated()
            )
            // Resource Server: valida JWT con JWK Set del auth-server
            .oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwt -> {
                    // La jwk-set-uri se inyecta desde application.properties
                    // No se necesita configuración adicional aquí
                })
            );

        return http.build();
    }
}
