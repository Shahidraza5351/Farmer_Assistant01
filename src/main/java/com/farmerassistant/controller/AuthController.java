package com.farmerassistant.controller;

import com.farmerassistant.dto.request.LoginRequest;
import com.farmerassistant.dto.request.RegisterRequest;
import com.farmerassistant.dto.response.ApiResponse;
import com.farmerassistant.dto.response.AuthResponse;
import com.farmerassistant.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication endpoints.
 *
 * Public endpoints (no JWT required):
 *   POST /api/auth/register  → Register a new farmer
 *   POST /api/auth/login     → Login and receive JWT
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * Register a new farmer account.
     *
     * @param request name, email, password
     * @return 201 Created with JWT token
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        log.info("Registration request for email: {}", request.getEmail());
        AuthResponse authResponse = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Farmer registered successfully", authResponse));
    }

    /**
     * Login with existing credentials.
     *
     * @param request email and password
     * @return 200 OK with JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        log.info("Login request for email: {}", request.getEmail());
        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    }
}
