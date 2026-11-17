
package com.photography.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // 1. Password encoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. Create one administrator account from environment variables
    @Bean
    public UserDetailsService userDetailsService(
            @Value("${app.admin.username}") String username,
            @Value("${app.admin.password}") String password,
            PasswordEncoder passwordEncoder
    ) {

        if (username == null || username.isBlank()
                || password == null || password.isBlank()) {

            throw new IllegalStateException(
                    "Admin username and password must be configured."
            );
        }

        UserDetails admin = User.withUsername(username.trim())
                .password(passwordEncoder.encode(password))
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(admin);
    }

    // 3. Application security rules
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .cors(Customizer.withDefaults())

                // REST API using explicit Authorization headers
                .csrf(csrf -> csrf.disable())

                // No server-side login session
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // HTTP Basic authentication
                .httpBasic(Customizer.withDefaults())

                .authorizeHttpRequests(auth -> auth

                        // CORS preflight
                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        ).permitAll()

                        // Health and error endpoints
                        .requestMatchers(
                                "/api/health",
                                "/error"
                        ).permitAll()

                        // Public website content
                        .requestMatchers(
                                HttpMethod.GET,
                                "/events",
                                "/events/**",
                                "/photos",
                                "/photos/**",
                                "/testimonials",
                                "/testimonials/**",
                                "/bookings/count",
                                "/api/health"
                        ).permitAll()

                        // Anyone can submit a booking
                        .requestMatchers(
                                HttpMethod.POST,
                                "/bookings"
                        ).permitAll()

                        // Only admin can view booking information
                        .requestMatchers(
                                HttpMethod.GET,
                                "/bookings",
                                "/bookings/**"
                        ).hasRole("ADMIN")

                        // Only admin can update bookings
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/bookings",
                                "/bookings/**"
                        ).hasRole("ADMIN")

                        // Only admin can delete bookings
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/bookings",
                                "/bookings/**"
                        ).hasRole("ADMIN")

                        // Only admin can add website content
                        .requestMatchers(
                                HttpMethod.POST,
                                "/events",
                                "/events/**",
                                "/photos",
                                "/photos/**",
                                "/testimonials",
                                "/testimonials/**"
                        ).hasRole("ADMIN")

                        // Only admin can edit website content
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/events",
                                "/events/**",
                                "/photos",
                                "/photos/**",
                                "/testimonials",
                                "/testimonials/**"
                        ).hasRole("ADMIN")

                        // Only admin can delete website content
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/events",
                                "/events/**",
                                "/photos",
                                "/photos/**",
                                "/testimonials",
                                "/testimonials/**"
                        ).hasRole("ADMIN")

                        // Any new endpoint is protected by default
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
