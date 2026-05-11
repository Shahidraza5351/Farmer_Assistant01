// package com.farmerassistant.config;

// import com.farmerassistant.filter.JwtAuthenticationFilter;
// import lombok.RequiredArgsConstructor;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.security.authentication.AuthenticationManager;
// import org.springframework.security.authentication.AuthenticationProvider;
// import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
// import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
// import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
// import org.springframework.security.config.http.SessionCreationPolicy;
// import org.springframework.security.core.userdetails.UserDetailsService;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.security.web.SecurityFilterChain;
// import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
// import org.springframework.security.web.AuthenticationEntryPoint;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.MediaType;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.farmerassistant.dto.response.ApiResponse;

// import java.time.LocalDateTime;

// /**
//  * Spring Security configuration.
//  *
//  * - Stateless JWT-based session
//  * - Public: /api/auth/**
//  * - Protected: /api/chat/**, /api/disease/**
//  */
// @Configuration
// @EnableWebSecurity
// @EnableMethodSecurity
// @RequiredArgsConstructor
// public class SecurityConfig {

//     private final JwtAuthenticationFilter jwtAuthFilter;
//     private final UserDetailsService userDetailsService;

//     /**
//      * Main security filter chain configuration.
//      */
//     @Bean
//     public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//         http
//             // Disable CSRF (stateless REST API)
//             .csrf(AbstractHttpConfigurer::disable)

//             // Configure CORS
//             .cors(cors -> cors.configure(http))

//             // Set custom 401 handler
//             .exceptionHandling(ex -> ex
//                 .authenticationEntryPoint(customAuthenticationEntryPoint())
//             )

//             // Stateless session - no HTTP session
//             .sessionManagement(session ->
//                 session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//             )

//             // Authorization rules
//             .authorizeHttpRequests(auth -> auth
//                 // Public endpoints
//                 .requestMatchers("/api/auth/**").permitAll()
//                 .requestMatchers("/actuator/health").permitAll()
//                 .requestMatchers("/error").permitAll()
//                 // Protected endpoints
//                 .requestMatchers("/api/chat/**").authenticated()
//                 .requestMatchers("/api/disease/**").authenticated()
//                 // All other requests require authentication
//                 .anyRequest().authenticated()
//             )

//             // Set authentication provider
//             .authenticationProvider(authenticationProvider())

//             // Add JWT filter before the standard username/password filter
//             .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

//         return http.build();
//     }

//     /**
//      * Custom 401 Unauthorized response handler.
//      * Returns JSON instead of default Spring redirect.
//      */
//     @Bean
//     public AuthenticationEntryPoint customAuthenticationEntryPoint() {
//         return (request, response, authException) -> {
//             response.setStatus(HttpStatus.UNAUTHORIZED.value());
//             response.setContentType(MediaType.APPLICATION_JSON_VALUE);

//             ApiResponse<?> apiResponse = ApiResponse.error(
//                 "Authentication required. Please login to access this resource.",
//                 "UNAUTHORIZED"
//             );

//             ObjectMapper mapper = new ObjectMapper();
//             mapper.findAndRegisterModules();
//             response.getWriter().write(mapper.writeValueAsString(apiResponse));
//         };
//     }

//     /**
//      * DAO-based authentication provider using BCrypt and custom UserDetailsService.
//      */
//     @Bean
//     public AuthenticationProvider authenticationProvider() {
//         DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
//         authProvider.setUserDetailsService(userDetailsService);
//         authProvider.setPasswordEncoder(passwordEncoder());
//         return authProvider;
//     }

//     /**
//      * Expose the AuthenticationManager bean for use in auth service.
//      */
//     @Bean
//     public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
//             throws Exception {
//         return config.getAuthenticationManager();
//     }

//     /**
//      * BCrypt password encoder with default strength (10 rounds).
//      */
//     @Bean
//     public PasswordEncoder passwordEncoder() {
//         return new BCryptPasswordEncoder();
//     }
// }


package com.farmerassistant.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.farmerassistant.dto.response.ApiResponse;
import com.farmerassistant.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security Configuration
 *
 * Features:
 * - JWT Authentication
 * - Stateless Session
 * - CORS Support
 * - Custom 401 Response
 * - Public Auth Endpoints
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    /**
     * Main Security Filter Chain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http

            // Disable CSRF for REST API
            .csrf(AbstractHttpConfigurer::disable)

            // Enable CORS
            .cors(Customizer.withDefaults())

            // Stateless Session
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Custom Unauthorized Handler
            .exceptionHandling(ex ->
                    ex.authenticationEntryPoint(customAuthenticationEntryPoint())
            )

            // Authorization Rules
            .authorizeHttpRequests(auth -> auth

                    // Allow CORS preflight requests
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                    // Public Endpoints
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/actuator/health").permitAll()
                    .requestMatchers("/error").permitAll()

                    // Protected Endpoints
                    .requestMatchers("/api/chat/**").authenticated()
                    .requestMatchers("/api/disease/**").authenticated()

                    // Any Other Request
                    .anyRequest().authenticated()
            )

            // Authentication Provider
            .authenticationProvider(authenticationProvider())

            // JWT Filter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS Configuration
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        // Frontend URL
        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:5173"
        ));

        // Allowed HTTP Methods
        configuration.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "OPTIONS"
        ));

        // Allowed Headers
        configuration.setAllowedHeaders(List.of("*"));

        // Allow Credentials
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /**
     * Custom 401 Unauthorized Response
     */
    @Bean
    public AuthenticationEntryPoint customAuthenticationEntryPoint() {

        return (request, response, authException) -> {

            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            ApiResponse<?> apiResponse = ApiResponse.error(
                    "Authentication required. Please login to access this resource.",
                    "UNAUTHORIZED"
            );

            ObjectMapper mapper = new ObjectMapper();
            mapper.findAndRegisterModules();

            response.getWriter().write(
                    mapper.writeValueAsString(apiResponse)
            );
        };
    }

    /**
     * Authentication Provider
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider authProvider =
                new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    /**
     * Authentication Manager
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {

        return config.getAuthenticationManager();
    }

    /**
     * Password Encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }
}