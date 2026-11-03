package com.hotel.booking.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.booking.dtos.ai.AiSuggestRequest;
import com.hotel.booking.entities.User;
import com.hotel.booking.entities.enums.UserRole;
import com.hotel.booking.repositories.UserRepository;
import com.hotel.booking.security.JwtTokenProvider;
import com.hotel.booking.services.AiAssistService;
import com.hotel.booking.services.ImageUploadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private AiAssistService aiAssistService;

    @MockitoBean
    private ImageUploadService imageUploadService;

    private User testUser;
    private String userToken;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        testUser = new User();
        testUser.setEmail("ai_user@test.com");
        testUser.setPassword("password123");
        testUser.setFirstName("Bob");
        testUser.setLastName("Builder");
        testUser.setRole(UserRole.USER);
        testUser = userRepository.save(testUser);
        userToken = jwtTokenProvider.generateToken(testUser.getId(), testUser.getEmail(), UserRole.USER);
    }

    @Test
    @DisplayName("POST /api/ai/suggest - Unauthenticated returns 401")
    void suggest_unauthenticated_returns401() throws Exception {
        AiSuggestRequest request = new AiSuggestRequest("hotel_description", "Nice hotel");

        mockMvc.perform(post("/api/ai/suggest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/ai/suggest - When disabled returns 503 Service Unavailable")
    void suggest_whenDisabled_returns503() throws Exception {
        aiAssistService.setAiEnabled(false);

        AiSuggestRequest request = new AiSuggestRequest("insights_copy", "metrics data");

        mockMvc.perform(post("/api/ai/suggest")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message", is("AI assist is disabled (set AI_ASSIST_ENABLED=true)")));
    }

    @Test
    @DisplayName("POST /api/ai/suggest - When enabled returns 200 OK with draft")
    void suggest_whenEnabled_returns200() throws Exception {
        aiAssistService.setAiEnabled(true);

        AiSuggestRequest request = new AiSuggestRequest("hotel_description", "Luxury seaside resort in Cornwall");

        mockMvc.perform(post("/api/ai/suggest")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.draft", notNullValue()))
                .andExpect(jsonPath("$.provider", is("stub")));
    }

    @Test
    @DisplayName("POST /api/ai/suggest - Validation failure for invalid kind returns 400 Bad Request")
    void suggest_invalidKind_returns400() throws Exception {
        aiAssistService.setAiEnabled(true);

        AiSuggestRequest request = new AiSuggestRequest("invalid_kind", "some text");

        mockMvc.perform(post("/api/ai/suggest")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("kind must be either hotel_description or insights_copy")));
    }
}

