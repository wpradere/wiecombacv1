package com.wiimy.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:3001}")
    private List<String> allowedOrigins;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Credentials (cookies) require explicit origins — never use allowedOrigins("*") here
        config.setAllowCredentials(true);
        config.setAllowedOrigins(allowedOrigins);

        // Only the headers the frontend actually sends
        config.setAllowedHeaders(List.of(
                HttpHeaders.CONTENT_TYPE,
                HttpHeaders.ACCEPT,
                HttpHeaders.AUTHORIZATION,
                "X-Requested-With"
        ));

        // Only methods used by the API — PUT is not needed
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Set-Cookie is managed by the browser automatically for httpOnly cookies;
        // exposing it here is unnecessary and slightly widens the attack surface
        config.setExposedHeaders(List.of());

        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return new CorsFilter(source);
    }
}
