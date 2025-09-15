package com.presta.infrastructure.external.security;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

        private static final String[] PERMITTED_ENDPOINTS = {
                "/actuator/info",
                "/actuator/info/**",
                "/actuator/health",
                "/actuator/health/**",
                "/actuator/metrics",
                "/actuator/metrics/**",
                "/v*/api-docs/**",
                "/v*/api-docs*",
                "/swagger-ui.html",
                "/swagger-ui/**",
                "/api/assignments/**"
        };



        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            return  http
                    .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(
                            req ->
                                    req.requestMatchers(PERMITTED_ENDPOINTS).permitAll()
                                            .anyRequest()
                                            .authenticated()
                    )
                    .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(new KeycloakJwtAuthenticationConverter())))
                    .build();
        }


        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
            CorsConfiguration corsConfiguration = new CorsConfiguration();
            corsConfiguration.setAllowedOrigins(List.of("http://localhost:4200"));
            corsConfiguration.setAllowedMethods(List.of("GET", "POST","PUT","PATCH"));
            corsConfiguration.setAllowCredentials(true);
            corsConfiguration.setAllowedHeaders(List.of("*"));
            corsConfiguration.setMaxAge(3600L);
            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("/**", corsConfiguration);
            return source;
        }


}


