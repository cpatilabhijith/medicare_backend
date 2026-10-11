package com.medicare.backend.config;

import com.medicare.backend.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 1. Public endpoints
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/doctors").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/doctors/*").permitAll()

                // 2. Admin specific collection endpoints
                .requestMatchers(HttpMethod.GET, "/api/patients").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/appointments").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/patients").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/doctors").hasRole("ADMIN")

                // 3. Profile endpoints — each role updates their own profile
                .requestMatchers("/api/profile/me").authenticated()
                .requestMatchers("/api/profile/patient").hasRole("PATIENT")
                .requestMatchers("/api/profile/doctor").hasRole("DOCTOR")
                .requestMatchers("/api/profile/admin").hasRole("ADMIN")

                // 4. Doctor-scoped endpoints
                .requestMatchers(HttpMethod.PUT, "/api/doctors/**").hasAnyRole("DOCTOR", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/doctors/**").hasAnyRole("DOCTOR", "ADMIN")
                .requestMatchers("/api/appointments/doctor/**").hasAnyRole("DOCTOR", "ADMIN")
                .requestMatchers("/api/appointments/*/status").hasAnyRole("DOCTOR", "ADMIN")

                // 5. Patient-scoped endpoints
                .requestMatchers(HttpMethod.GET, "/api/patients/**").hasAnyRole("PATIENT", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/patients/**").hasAnyRole("PATIENT", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/patients/**").hasAnyRole("PATIENT", "ADMIN")
                .requestMatchers("/api/appointments/patient/**").hasAnyRole("PATIENT", "ADMIN")
                .requestMatchers("/api/appointments/book").hasAnyRole("PATIENT", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/appointments/**").hasAnyRole("PATIENT", "DOCTOR", "ADMIN")

                // 6. Catch-all for any remaining request
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
