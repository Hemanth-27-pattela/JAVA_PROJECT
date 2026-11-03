package com.hotel.booking.services;

import com.hotel.booking.dtos.ai.AiSuggestRequest;
import com.hotel.booking.dtos.ai.AiSuggestResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

public class AiAssistServiceTest {

    private AiAssistService aiAssistService;

    @BeforeEach
    void setUp() {
        aiAssistService = new AiAssistService();
    }

    @Test
    @DisplayName("Throws 503 Service Unavailable when AI assist is disabled")
    void suggest_whenDisabled_throws503() {
        aiAssistService.setAiEnabled(false);

        AiSuggestRequest request = new AiSuggestRequest("insights_copy", "overview data");
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> aiAssistService.suggest(request));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, ex.getStatusCode());
        assertEquals("AI assist is disabled (set AI_ASSIST_ENABLED=true)", ex.getReason());
    }

    @Test
    @DisplayName("Generates deterministic insights_copy draft when enabled")
    void suggest_insightsCopy_returnsStubDraft() {
        aiAssistService.setAiEnabled(true);

        AiSuggestRequest request = new AiSuggestRequest("insights_copy", "London boutique hotels");
        AiSuggestResponse response = aiAssistService.suggest(request);

        assertNotNull(response);
        assertEquals("stub", response.getProvider());
        assertTrue(response.getDraft().contains("Business snapshot: London boutique hotels"));
        assertTrue(response.getDraft().contains("Bookings and revenue trends look healthy"));
    }

    @Test
    @DisplayName("Generates deterministic hotel_description draft when enabled")
    void suggest_hotelDescription_returnsStubDraft() {
        aiAssistService.setAiEnabled(true);

        AiSuggestRequest request = new AiSuggestRequest("hotel_description", "charming boutique hotel in central Edinburgh");
        AiSuggestResponse response = aiAssistService.suggest(request);

        assertNotNull(response);
        assertEquals("stub", response.getProvider());
        assertTrue(response.getDraft().contains("Charming boutique hotel in central Edinburgh"));
        assertTrue(response.getDraft().contains("Guests enjoy refined rooms"));
    }

    @Test
    @DisplayName("Throws 400 Bad Request on invalid kind")
    void suggest_invalidKind_throws400() {
        aiAssistService.setAiEnabled(true);

        AiSuggestRequest request = new AiSuggestRequest("unsupported_kind", "some input");
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> aiAssistService.suggest(request));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }
}

