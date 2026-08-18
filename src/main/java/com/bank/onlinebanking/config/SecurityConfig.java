package com.bank.onlinebanking.config;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.bank.onlinebanking.security.JwtAuthenticationFilter;
import com.bank.onlinebanking.service.CustomUserDetailsService;

import tools.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    public SecurityConfig(
            CustomUserDetailsService customUserDetailsService,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            ObjectMapper objectMapper) {

        this.customUserDetailsService = customUserDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.objectMapper = objectMapper;
    }

    // =====================================================
    // PASSWORD ENCODER
    // =====================================================

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

    // =====================================================
    // AUTHENTICATION PROVIDER
    // =====================================================

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(
                        customUserDetailsService
                );

        provider.setPasswordEncoder(
                passwordEncoder()
        );

        return provider;
    }

    // =====================================================
    // AUTHENTICATION MANAGER
    // =====================================================

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration)
            throws Exception {

        return configuration.getAuthenticationManager();
    }

    // =====================================================
    // JWT FILTER REGISTRATION
    // Keeps the JWT filter inside the security chain only,
    // so it never runs twice via servlet-container registration.
    // =====================================================

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(
            JwtAuthenticationFilter filter) {

        FilterRegistrationBean<JwtAuthenticationFilter> registration =
                new FilterRegistrationBean<>(filter);

        registration.setEnabled(false);

        return registration;
    }

    // =====================================================
    // SECURITY FILTER CHAIN
    // =====================================================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))

                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                .authenticationProvider(
                        authenticationProvider()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class)

                .exceptionHandling(ex -> ex

                        .authenticationEntryPoint((request, response, authException) -> {

                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                            objectMapper.writeValue(response.getOutputStream(),
                                    errorBody(401, "UNAUTHORIZED",
                                            "Authentication is required to access this resource",
                                            request.getRequestURI()));
                        })

                        .accessDeniedHandler((request, response, accessDeniedException) -> {

                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                            objectMapper.writeValue(response.getOutputStream(),
                                    errorBody(403, "FORBIDDEN",
                                            "You do not have permission to access this resource",
                                            request.getRequestURI()));
                        })
                )

                .authorizeHttpRequests(auth -> auth

                        // =================================
                        // PUBLIC: AUTHENTICATION APIS
                        // =================================

                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/error"
                        ).permitAll()

                        // =================================
                        // PUBLIC: FRONTEND PAGES
                        // =================================

                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/login.html",
                                "/register.html",
                                "/dashboard.html",
                                "/admin.html"
                        ).permitAll()

                        // =================================
                        // PUBLIC: FRONTEND CSS / JS
                        // =================================

                        .requestMatchers(
                                "/style.css",
                                "/login.css",
                                "/register.css",
                                "/app.js",
                                "/login.js",
                                "/register.js",
                                "/dashboard.js",
                                "/admin.js",
                                "/logout.js",
                                "/favicon.ico"
                        ).permitAll()

                        // =================================
                        // ADMIN ONLY
                        // =================================

                        .requestMatchers(
                                "/api/admin/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/customers"
                        ).hasRole("ADMIN")

                        // =================================
                        // EVERYTHING ELSE UNDER /api IS
                        // PROTECTED (VALID JWT REQUIRED)
                        // =================================

                        .requestMatchers(
                                "/api/**"
                        ).authenticated()

                        // =================================
                        // EVERYTHING ELSE
                        // =================================

                        .anyRequest().authenticated()
                );

        return http.build();
    }

    // =====================================================
    // HELPERS
    // =====================================================

    private Map<String, Object> errorBody(
            int status, String error, String message, String path) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status);
        body.put("error", error);
        body.put("message", message);
        body.put("path", path);

        return body;
    }
}