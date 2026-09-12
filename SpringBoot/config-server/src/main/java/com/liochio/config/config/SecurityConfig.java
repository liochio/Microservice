package com.liochio.config.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * ==============================================================================
 * Cấu Hình Bảo Mật Spring Security Cho Config Server (Enterprise Security Config)
 * ==============================================================================
 * 
 * Mục đích:
 * 1. Bảo vệ các endpoint cấu hình nhạy cảm qua HTTP Basic Auth.
 * 2. Mở công khai endpoint Health Check và Info cho Eureka / K8s probes.
 * 3. Cho phép gọi API mã hóa / giải mã /encrypt và /decrypt có xác thực.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}