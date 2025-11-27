package com.testnext.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.Jwt;
import java.time.Instant;
import java.util.Map;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthConverter jwtAuthConverter,
            TenantExtractionFilter tenantExtractionFilter,
            Environment env) throws Exception {
        
        final boolean isDev;
        if (env != null) {
            isDev = java.util.Arrays.stream(env.getActiveProfiles())
                    .anyMatch(p -> "dev".equals(p));
        } else {
            isDev = false;
        }

        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> {
                if (isDev) {
                    // allow tenant creation in dev profile for local POC (UI mock login)
                    auth.requestMatchers("/api/tenants").permitAll();
                } else {
                    auth.requestMatchers("/api/tenants").hasAuthority("SYSTEM_ADMIN");
                }
                auth.requestMatchers("/api/public/**").permitAll();
                // Actuator endpoints require ADMIN or ACTUATOR role in production
                auth.requestMatchers("/actuator/health/liveness").permitAll();
                auth.requestMatchers("/actuator/health/readiness").permitAll();
                auth.requestMatchers("/actuator/**").hasAuthority("ADMIN");
                auth.anyRequest().authenticated();
            })
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter))
            );

        // run tenant extraction after authentication is established
        http.addFilterAfter(tenantExtractionFilter, BearerTokenAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Simple JwtDecoder for local smoke runs. This does not perform signature verification
     * and should be replaced by a proper JwtDecoder (Nimbus, jwks) in production.
     * 
     * In Spring Boot 4.0, this pattern will be replaced with more declarative configuration.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        return token -> new Jwt(
            token,
            Instant.now(),
            Instant.now().plusSeconds(3600),
            Map.of("alg", "none"),
            Map.of()
        );
    }
}
