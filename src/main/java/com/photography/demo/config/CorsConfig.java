
package com.photography.demo.config;

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

                registry.addMapping("/**")

                        .allowedOriginPatterns(
                                // Main production frontend
                                "https://lakshmi-digitals-frontend.vercel.app",

                                // Vercel preview deployments
                                "https://lakshmi-digitals-frontend-*-vivek-1050.vercel.app",

                                // Local development
                                "http://localhost:5173",
                                "http://localhost:3000"
                        )

                        .allowedMethods(
                                "GET",
                                "POST",
                                "PUT",
                                "DELETE",
                                "OPTIONS"
                        )

                        .allowedHeaders(
                                "Authorization",
                                "Content-Type",
                                "Accept"
                        )

                        .allowCredentials(false)

                        .maxAge(3600);
            }
        };
    }
}
