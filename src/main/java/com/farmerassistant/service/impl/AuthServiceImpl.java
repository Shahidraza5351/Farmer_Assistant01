package com.farmerassistant.service.impl;

import com.farmerassistant.dto.request.LoginRequest;
import com.farmerassistant.dto.request.RegisterRequest;
import com.farmerassistant.dto.response.AuthResponse;
import com.farmerassistant.entity.User;
import com.farmerassistant.exception.UserAlreadyExistsException;
import com.farmerassistant.repository.UserRepository;
import com.farmerassistant.service.AuthService;
import com.farmerassistant.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of AuthService.
 * Handles user registration and login with JWT token generation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    /**
     * Register a new farmer:
     * 1. Check email uniqueness
     * 2. Encrypt password with BCrypt
     * 3. Save user to DB
     * 4. Generate and return JWT
     */
    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new farmer with email: {}", request.getEmail());

        // Check for duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(
                "An account with email '" + request.getEmail() + "' already exists."
            );
        }

        // Build and save user entity
        User user = User.builder()
                .name(request.getName().trim())
                .email(request.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.USER)
                .isActive(true)
                .build();

        user = userRepository.save(user);
        log.info("New farmer registered successfully: id={}, email={}", user.getId(), user.getEmail());

        // Generate JWT for immediate login after registration
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        return buildAuthResponse(user, token, "Registration successful! Welcome to Farmer Assistant.");
    }

    /**
     * Login a farmer:
     * 1. Authenticate credentials via Spring Security
     * 2. Load user details
     * 3. Generate and return JWT
     */
    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        // Spring Security handles bad credentials → throws BadCredentialsException
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail().toLowerCase().trim(),
                request.getPassword()
            )
        );

        // Load user from DB for response data
        User user = userRepository.findByEmailAndIsActiveTrue(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new RuntimeException("User not found after authentication"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        log.info("Farmer logged in successfully: id={}, email={}", user.getId(), user.getEmail());
        return buildAuthResponse(user, token, "Login successful! Welcome back, " + user.getName() + ".");
    }

    /** Helper to build a consistent AuthResponse. */
    private AuthResponse buildAuthResponse(User user, String token, String message) {
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .expiresAt(jwtUtil.getExpirationAsLocalDateTime(token))
                .message(message)
                .build();
    }
}
