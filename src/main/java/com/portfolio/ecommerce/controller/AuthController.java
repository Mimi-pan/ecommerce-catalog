package com.portfolio.ecommerce.controller;

import com.portfolio.ecommerce.dto.auth.AuthResponseDTO;
import com.portfolio.ecommerce.dto.auth.LoginRequestDTO;
import com.portfolio.ecommerce.dto.auth.RegisterRequestDTO;
import com.portfolio.ecommerce.exception.BusinessException;
import com.portfolio.ecommerce.model.User;
import com.portfolio.ecommerce.repository.UserRepository;
import com.portfolio.ecommerce.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register a new account and log in to receive a JWT token")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    /**
     * POST /auth/register
     * Creates a new user account and returns a JWT token immediately.
     */
    @Operation(summary = "Register a new user",
               description = "Creates a new USER account. Returns a JWT token that can be used immediately.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Account created successfully"),
        @ApiResponse(responseCode = "400", description = "Username already taken"),
        @ApiResponse(responseCode = "422", description = "Validation failed (e.g. password too short)")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Username '" + request.getUsername() + "' is already taken");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.USER)
                .build();

        userRepository.save(user);

        String token = jwtTokenProvider.generateToken(user.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED).body(
                AuthResponseDTO.builder()
                        .token(token)
                        .username(user.getUsername())
                        .role(user.getRole().name())
                        .build()
        );
    }

    /**
     * POST /auth/login
     * Authenticates an existing user and returns a fresh JWT token.
     */
    @Operation(summary = "Log in",
               description = "Authenticates with username and password. Returns a JWT token — include it as 'Bearer <token>' in the Authorization header for protected endpoints.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login successful"),
        @ApiResponse(responseCode = "401", description = "Invalid username or password"),
        @ApiResponse(responseCode = "422", description = "Validation failed")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        // Throws BadCredentialsException (caught by GlobalExceptionHandler) if invalid
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException("User not found"));

        String token = jwtTokenProvider.generateToken(user.getUsername());

        return ResponseEntity.ok(
                AuthResponseDTO.builder()
                        .token(token)
                        .username(user.getUsername())
                        .role(user.getRole().name())
                        .build()
        );
    }
}
