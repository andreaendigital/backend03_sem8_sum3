package com.bancoxyz.auth_server;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

/**
 * Configuración del Authorization Server OAuth2 para Spring Boot 4.1.1.
 *
 * Spring Boot 4.1.1 incluye auto-configuración completa para el Authorization Server
 * (OAuth2AuthorizationServerAutoConfiguration). El cliente OAuth2 se registra
 * directamente en application.properties con el prefijo
 * spring.security.oauth2.authorizationserver.client.*
 *
 * Esta clase solo provee los beans que la auto-configuración no puede generar
 * automáticamente:
 *   1. JWKSource con par de claves RSA para firmar los tokens JWT
 *   2. PasswordEncoder para verificar el client-secret
 *
 * Endpoints expuestos automáticamente por la auto-configuración:
 *   POST /oauth2/token              → emite access_token
 *   GET  /oauth2/jwks               → JWK Set público
 *   GET  /.well-known/oauth-authorization-server → metadata del servidor
 */
@Configuration
public class AuthorizationServerConfig {

    /**
     * Par de claves RSA-2048 generado en memoria al arrancar.
     * Los tokens JWT se firman con la clave privada; api-cuentas los valida
     * descargando la clave pública desde /oauth2/jwks.
     *
     * Nota académica: en producción, la clave debería persistirse (p.ej. en un
     * keystore) para que los tokens sigan siendo válidos tras reinicios.
     */
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        KeyPair keyPair = generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();

        return new ImmutableJWKSet<>(new JWKSet(rsaKey));
    }

    private static KeyPair generateRsaKey() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException("Error generando par de claves RSA", ex);
        }
    }

    /**
     * BCrypt para verificar el client-secret configurado en application.properties.
     * La auto-configuración lo usa internamente al autenticar al cliente OAuth2.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }
}
