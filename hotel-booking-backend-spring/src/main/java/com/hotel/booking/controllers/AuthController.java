package com.hotel.booking.controllers;

import com.hotel.booking.dtos.LoginRequest;
import com.hotel.booking.dtos.LoginResponse;
import com.hotel.booking.dtos.MessageResponse;
import com.hotel.booking.dtos.ValidateTokenResponse;
import com.hotel.booking.entities.User;
import com.hotel.booking.repositories.UserRepository;
import com.hotel.booking.security.JwtTokenProvider;
import com.hotel.booking.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

/**
 * Controller for authentication and token validation.
 * Compatible with the existing React frontend authentication flow.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication and token lifecycle endpoints")
public class AuthController {

        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtTokenProvider tokenProvider;

        public AuthController(UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        JwtTokenProvider tokenProvider) {
                this.userRepository = userRepository;
                this.passwordEncoder = passwordEncoder;
                this.tokenProvider = tokenProvider;
        }

        /**
         * User login with email and password.
         * Returns JWT token in response body for localStorage storage, and sets
         * HTTP-only cookies.
         */
        @PostMapping("/login")
        @Operation(summary = "User login", description = "Authenticate user with email and password")
        public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                        HttpServletResponse response) {
                User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                                .orElseThrow(() -> new BadCredentialsException("Invalid Credentials"));

                if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                        throw new BadCredentialsException("Invalid Credentials");
                }

                if (Boolean.FALSE.equals(user.getIsActive())) {
                        throw new BadCredentialsException("Invalid Credentials");
                }

                // Update last login timestamp
                user.setLastLogin(Instant.now());
                userRepository.save(user);

                String token = tokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole());

                // Set session cookie for browser fallback compatibility
                ResponseCookie sessionCookie = ResponseCookie.from("session_id", token)
                                .httpOnly(true)
                                .path("/")
                                .maxAge(86400)
                                .sameSite("Lax")
                                .build();
                response.addHeader(HttpHeaders.SET_COOKIE, sessionCookie.toString());

                LoginResponse.UserSummary userSummary = new LoginResponse.UserSummary(
                                user.getId(),
                                user.getEmail(),
                                user.getFirstName(),
                                user.getLastName());

                LoginResponse loginResponse = new LoginResponse(
                                user.getId(),
                                "Login successful",
                                token,
                                userSummary);

                return ResponseEntity.ok(loginResponse);
        }

        /**
         * Validates the caller's JWT token from Authorization header or session cookie.
         */
        @GetMapping("/validate-token")
        @Operation(summary = "Validate authentication token", description = "Validate current user token")
        public ResponseEntity<ValidateTokenResponse> validateToken(
                        @AuthenticationPrincipal UserPrincipal userPrincipal) {
                if (userPrincipal == null) {
                        return ResponseEntity.status(401).build();
                }
                return ResponseEntity.ok(new ValidateTokenResponse(userPrincipal.getId(), userPrincipal.getRole()));
        }

        /**
         * Refreshes the caller's JWT token to reflect any role changes or permissions.
         */
        @PostMapping("/refresh-token")
        @Operation(summary = "Refresh user token", description = "Reissues JWT with latest database roles and permissions")
        public ResponseEntity<LoginResponse> refreshToken(@AuthenticationPrincipal UserPrincipal userPrincipal,
                        HttpServletResponse response) {
                if (userPrincipal == null) {
                        return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
                }

                User user = userRepository.findById(userPrincipal.getId())
                                .orElseThrow(() -> new BadCredentialsException("User not found"));

                if (Boolean.FALSE.equals(user.getIsActive())) {
                        return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
                }

                String newToken = tokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole());

                ResponseCookie sessionCookie = ResponseCookie.from("session_id", newToken)
                                .httpOnly(true)
                                .path("/")
                                .maxAge(86400)
                                .sameSite("Lax")
                                .build();
                response.addHeader(HttpHeaders.SET_COOKIE, sessionCookie.toString());

                LoginResponse.UserSummary userSummary = new LoginResponse.UserSummary(
                                user.getId(),
                                user.getEmail(),
                                user.getFirstName(),
                                user.getLastName());

                LoginResponse loginResponse = new LoginResponse(
                                user.getId(),
                                "Token refreshed",
                                newToken,
                                userSummary);

                return ResponseEntity.ok(loginResponse);
        }

        /**
         * User logout.
         * Clears authentication cookies.
         *
         * Architectural Note: JWTs are stateless bearer tokens. Clearing client-side
         * storage
         * (localStorage 'session_id' and cookies) invalidates the client session.
         * In accordance with standard stateless architecture matching the original
         * Express backend,
         * immediate server-side revocation is not performed without a stateful token
         * blacklist.
         */
        @PostMapping("/logout")
        @Operation(summary = "User logout", description = "Clear authentication cookies")
        public ResponseEntity<MessageResponse> logout(HttpServletResponse response) {
                ResponseCookie clearSessionCookie = ResponseCookie.from("session_id", "")
                                .httpOnly(true)
                                .path("/")
                                .maxAge(0)
                                .sameSite("Lax")
                                .build();
                ResponseCookie clearAuthCookie = ResponseCookie.from("auth_token", "")
                                .httpOnly(true)
                                .path("/")
                                .maxAge(0)
                                .sameSite("Lax")
                                .build();

                response.addHeader(HttpHeaders.SET_COOKIE, clearSessionCookie.toString());
                response.addHeader(HttpHeaders.SET_COOKIE, clearAuthCookie.toString());

                return ResponseEntity.ok(new MessageResponse("Logout successful"));
        }
}
