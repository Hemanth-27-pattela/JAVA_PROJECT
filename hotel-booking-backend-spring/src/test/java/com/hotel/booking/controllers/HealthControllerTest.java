package com.hotel.booking.controllers;

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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String adminToken;
    private String ownerToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        adminToken = createOrGetToken("admin_health@test.com", UserRole.ADMIN);
        ownerToken = createOrGetToken("owner_health@test.com", UserRole.HOTEL_OWNER);
        userToken = createOrGetToken("user_health@test.com", UserRole.USER);
    }

    private String createOrGetToken(String email, UserRole role) {
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User u = new User();
            u.setEmail(email);
            u.setPassword("password123");
            u.setFirstName("Test");
            u.setLastName(role.name());
            u.setRole(role);
            u.setIsActive(true);
            u.setEmailVerified(true);
            return userRepository.save(u);
        });
        return jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole());
    }

    @Test
    @DisplayName("Root endpoint is public and returns welcome banner")
    void testRootEndpoint() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Hotel Booking Backend Spring Boot API is running")));
    }

    @Test
    @DisplayName("Anonymous basic health request returns 200 with minimal status")
    void testBasicHealthEndpoint_Anonymous_Returns200() throws Exception {
        mockMvc.perform(get("/api/health").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("healthy"))
                .andExpect(jsonPath("$.database.status").value("connected"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.performance").doesNotExist());
    }

    @Test
    @DisplayName("USER basic health request returns 200 with minimal status")
    void testBasicHealthEndpoint_User_Returns200() throws Exception {
        mockMvc.perform(get("/api/health")
                        .header("Authorization", "Bearer " + userToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("healthy"))
                .andExpect(jsonPath("$.database.status").value("connected"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.performance").doesNotExist());
    }

    @Test
    @DisplayName("Anonymous detailed health request returns 401 Unauthorized")
    void testDetailedHealthEndpoint_Anonymous_Returns401() throws Exception {
        mockMvc.perform(get("/api/health/detailed").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("USER detailed health request returns 403 Forbidden")
    void testDetailedHealthEndpoint_User_Returns403() throws Exception {
        mockMvc.perform(get("/api/health/detailed")
                        .header("Authorization", "Bearer " + userToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("HOTEL_OWNER detailed health request returns 403 Forbidden")
    void testDetailedHealthEndpoint_HotelOwner_Returns403() throws Exception {
        mockMvc.perform(get("/api/health/detailed")
                        .header("Authorization", "Bearer " + ownerToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ADMIN detailed health request returns 200 OK with sanitized metrics")
    void testDetailedHealthEndpoint_Admin_Returns200() throws Exception {
        mockMvc.perform(get("/api/health/detailed")
                        .header("Authorization", "Bearer " + adminToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("healthy"))
                .andExpect(jsonPath("$.database.status").value("connected"))
                .andExpect(jsonPath("$.performance.memory.percentage").isNumber())
                .andExpect(jsonPath("$.performance.uptime").isNumber());
    }

    @Test
    @DisplayName("CORS preflight on /api/health allows frontend origin")
    void testCorsHeadersOnOptions() throws Exception {
        mockMvc.perform(options("/api/health")
                        .header("Origin", "http://localhost:5174")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5174"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }
}
