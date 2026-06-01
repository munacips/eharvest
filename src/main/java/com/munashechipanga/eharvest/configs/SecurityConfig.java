package com.munashechipanga.eharvest.configs;

import com.munashechipanga.eharvest.security.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    // Add @Lazy annotation here to break the circular dependency
    public SecurityConfig(@Lazy JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configure(http)) // Enable CORS
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll() // login + register = public
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/buyers").permitAll() // public
                                                                                                                 // registration
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/farmers").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/logistics-providers")
                        .permitAll()
                        .requestMatchers("/api/v1/heatmap/**").permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/api/v1/payments/webhook", "/api/v1/payments/return").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/subscriptions/buyer/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/subscriptions/farmer/**").authenticated()
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/users/**").hasRole("ADMIN")
                        .requestMatchers("/farmer/**").hasRole("FARMER")
                        .requestMatchers("/buyer/**").hasRole("BUYER")
                        .requestMatchers("/logistics/**").hasRole("LOGISTICS")
                        .anyRequest().authenticated())
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // register JWT filter before username/password filter
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // CORS for Flutter apps and production
        // Flutter apps don't enforce same-origin policy, but we need proper headers for WebSocket
        // Production IP: 34.206.207.121 (AWS EC2)

        List<String> allowedOrigins = List.of(
                "http://localhost:*",          // Local development
                "http://127.0.0.1:*",          // Local development
                "http://192.168.*.*:*",        // Local network
                "http://34.206.207.121:*",     // Production AWS EC2
                "http://34.206.207.121",       // Production AWS EC2 (no port)
                "http://34.206.207.121:8080",  // Production Backend
                "http://34.206.207.121:80",    // Production Flutter Web
                "http://34.206.207.121:8000"   // Production AI Service
        );

        // Production: Use specific origins. Flutter handles security via JWT
        configuration.setAllowedOriginPatterns(Collections.singletonList("*"));

        configuration.setAllowCredentials(true); // Required for WebSocket with JWT auth
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Content-Range", "X-Content-Range", "Authorization", "X-Total-Count"));
        configuration.setMaxAge(86400L); // 24 hours for production

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
