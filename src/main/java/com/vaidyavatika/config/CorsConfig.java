package com.vaidyavatika.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins(
                                "http://localhost:3000",
                                "http://localhost:5173",
                                "https://vaidyavatika.com",
                                "https://www.vaidyavatika.com"
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        // Only allow headers that the app actually uses
                        // instead of wildcard "*"
                        .allowedHeaders(
                                "Authorization",
                                "Content-Type",
                                "Accept",
                                "X-Requested-With",
                                "Origin"
                        )
                        .exposedHeaders("Authorization")
                        .allowCredentials(true)
                        .maxAge(3600); // cache preflight for 1 hour
            }
        };
    }
}