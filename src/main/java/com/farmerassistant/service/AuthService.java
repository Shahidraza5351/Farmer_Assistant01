package com.farmerassistant.service;

import com.farmerassistant.dto.request.LoginRequest;
import com.farmerassistant.dto.request.RegisterRequest;
import com.farmerassistant.dto.response.AuthResponse;

/**
 * Service interface for user authentication operations.
 */
public interface AuthService {

    /**
     * Register a new farmer account.
     *
     * @param request registration details (name, email, password)
     * @return AuthResponse with JWT token and user info
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Authenticate a farmer and return a JWT token.
     *
     * @param request login credentials (email, password)
     * @return AuthResponse with JWT token and user info
     */
    AuthResponse login(LoginRequest request);
}
