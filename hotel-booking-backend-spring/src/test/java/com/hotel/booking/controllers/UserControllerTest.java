package com.hotel.booking.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.booking.dtos.RegisterRequest;
import com.hotel.booking.dtos.UpdateRoleRequest;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {

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

    private User regularUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        regularUser = new User();
        regularUser.setEmail("user@example.com");
        regularUser.setPassword(passwordEncoder.encode("password123"));
        regularUser.setFirstName("Regular");
        regularUser.setLastName("User");
        regularUser.setRole(UserRole.USER);
        regularUser.setIsActive(true);
        regularUser = userRepository.save(regularUser);

        adminUser = new User();
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword(passwordEncoder.encode("adminPass123"));
        adminUser.setFirstName("Super");
        adminUser.setLastName("Admin");
        adminUser.setRole(UserRole.ADMIN);
        adminUser.setIsActive(true);
        adminUser = userRepository.save(adminUser);
    }

    @Test
    @DisplayName("POST /api/users/register creates user with BCrypt password and role USER")
    void testRegisterSuccess() throws Exception {
        RegisterRequest request = new RegisterRequest("New", "Person", "newperson@example.com", "strongPass123");

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered OK"))
                .andExpect(header().exists("Set-Cookie"));

        User saved = userRepository.findByEmail("newperson@example.com").orElse(null);
        assertNotNull(saved);
        assertEquals("New", saved.getFirstName());
        assertEquals(UserRole.USER, saved.getRole(), "Must always be assigned USER role");
        assertTrue(passwordEncoder.matches("strongPass123", saved.getPassword()), "Password must be BCrypt encoded");
        assertNotEquals("strongPass123", saved.getPassword(), "Plaintext password must never be stored");
    }

    @Test
    @DisplayName("POST /api/users/register with duplicate email returns 400 User already exists")
    void testRegisterDuplicateEmail() throws Exception {
        RegisterRequest request = new RegisterRequest("Existing", "User", "user@example.com", "password123");

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User already exists"));
    }

    @Test
    @DisplayName("GET /api/users/me with valid token returns user profile without password")
    void testGetCurrentUserWithValidToken() throws Exception {
        String token = tokenProvider.generateToken(regularUser.getId(), regularUser.getEmail(), regularUser.getRole());

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._id").value(regularUser.getId()))
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.firstName").value("Regular"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("GET /api/users/me unauthenticated returns 401 unauthorized")
    void testGetCurrentUserUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("unauthorized"));
    }

    @Test
    @DisplayName("GET /api/users as regular USER returns 403 Access denied")
    void testListUsersAsUserReturnsForbidden() throws Exception {
        String token = tokenProvider.generateToken(regularUser.getId(), regularUser.getEmail(), regularUser.getRole());

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    @DisplayName("GET /api/users as ADMIN returns 200 and list of users")
    void testListUsersAsAdminReturnsOk() throws Exception {
        String token = tokenProvider.generateToken(adminUser.getId(), adminUser.getEmail(), adminUser.getRole());

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].email").isNotEmpty())
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    @Test
    @DisplayName("PATCH /api/users/{id}/role as ADMIN updates role successfully")
    void testUpdateRoleAsAdmin() throws Exception {
        String token = tokenProvider.generateToken(adminUser.getId(), adminUser.getEmail(), adminUser.getRole());
        UpdateRoleRequest request = new UpdateRoleRequest(UserRole.HOTEL_OWNER);

        mockMvc.perform(patch("/api/users/" + regularUser.getId() + "/role")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._id").value(regularUser.getId()))
                .andExpect(jsonPath("$.role").value("hotel_owner"));

        User updated = userRepository.findById(regularUser.getId()).orElse(null);
        assertNotNull(updated);
        assertEquals(UserRole.HOTEL_OWNER, updated.getRole());
    }

    @Test
    @DisplayName("PATCH /api/users/{id}/role admin cannot demote themselves")
    void testAdminCannotDemoteSelf() throws Exception {
        String token = tokenProvider.generateToken(adminUser.getId(), adminUser.getEmail(), adminUser.getRole());
        UpdateRoleRequest request = new UpdateRoleRequest(UserRole.USER);

        mockMvc.perform(patch("/api/users/" + adminUser.getId() + "/role")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot demote your own admin role"));
    }
}

