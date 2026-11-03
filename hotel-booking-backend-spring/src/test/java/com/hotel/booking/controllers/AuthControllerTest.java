package com.hotel.booking.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.booking.dtos.LoginRequest;
import com.hotel.booking.entities.User;
import com.hotel.booking.entities.enums.UserRole;
import com.hotel.booking.repositories.UserRepository;
import com.hotel.booking.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        testUser = new User();
        testUser.setEmail("auth.test@example.com");
        testUser.setPassword(passwordEncoder.encode("securePassword123"));
        testUser.setFirstName("Auth");
        testUser.setLastName("Tester");
        testUser.setRole(UserRole.USER);
        testUser.setIsActive(true);
        testUser = userRepository.save(testUser);
    }

    @Test
    @DisplayName("POST /api/auth/login with valid credentials returns 200, JWT token, and user summary")
    void testLoginSuccess() throws Exception {
        LoginRequest request = new LoginRequest("auth.test@example.com", "securePassword123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(testUser.getId()))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.id").value(testUser.getId()))
                .andExpect(jsonPath("$.user.email").value("auth.test@example.com"))
                .andExpect(jsonPath("$.user.firstName").value("Auth"))
                .andExpect(jsonPath("$.user.lastName").value("Tester"))
                .andExpect(header().exists("Set-Cookie"));
    }

    @Test
    @DisplayName("POST /api/auth/login with wrong password returns 400 Invalid Credentials")
    void testLoginInvalidPassword() throws Exception {
        LoginRequest request = new LoginRequest("auth.test@example.com", "wrongPassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid Credentials"));
    }

    @Test
    @DisplayName("POST /api/auth/login with unknown email returns 400 Invalid Credentials")
    void testLoginUnknownEmail() throws Exception {
        LoginRequest request = new LoginRequest("unknown@example.com", "securePassword123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid Credentials"));
    }

    @Test
    @DisplayName("GET /api/auth/validate-token with valid Bearer token returns 200 and userId")
    void testValidateTokenSuccess() throws Exception {
        String token = tokenProvider.generateToken(testUser.getId(), testUser.getEmail(), testUser.getRole());

        mockMvc.perform(get("/api/auth/validate-token")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(testUser.getId()));
    }

    @Test
    @DisplayName("GET /api/auth/validate-token without token returns 401 unauthorized")
    void testValidateTokenWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/auth/validate-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("unauthorized"));
    }

    @Test
    @DisplayName("GET /api/auth/validate-token with invalid token returns 401 unauthorized")
    void testValidateTokenWithInvalidTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/auth/validate-token")
                        .header("Authorization", "Bearer invalid-or-malformed-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("unauthorized"));
    }

    @Test
    @DisplayName("POST /api/auth/logout clears auth cookies and returns 200")
    void testLogoutClearsCookies() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logout successful"))
                .andExpect(header().exists("Set-Cookie"));
    }
}

