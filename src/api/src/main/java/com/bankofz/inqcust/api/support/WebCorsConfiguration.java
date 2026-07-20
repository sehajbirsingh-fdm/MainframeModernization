package com.bankofz.inqcust.api.support;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

@Configuration
@SuppressWarnings("null")
public class WebCorsConfiguration implements WebMvcConfigurer {

    private final String[] allowedOrigins;

    public WebCorsConfiguration(@Value("${app.cors.allowed-origins:http://localhost:5173}") String allowedOrigins) {
        List<String> parsedOrigins = new ArrayList<>();
        for (String value : allowedOrigins.split(",")) {
            String origin = value.trim();
            if (!origin.isEmpty()) {
                parsedOrigins.add(origin);
            }
        }

        this.allowedOrigins = parsedOrigins.isEmpty()
                ? new String[]{"http://localhost:5173"}
                : parsedOrigins.toArray(String[]::new);
    }

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);

        registry.addMapping("/v1/**")
            .allowedOrigins(allowedOrigins)
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .maxAge(3600);
    }
}
