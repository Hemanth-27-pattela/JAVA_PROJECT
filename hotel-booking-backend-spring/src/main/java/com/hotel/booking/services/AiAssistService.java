package com.hotel.booking.services;

import com.hotel.booking.dtos.ai.AiSuggestRequest;
import com.hotel.booking.dtos.ai.AiSuggestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AiAssistService {

    private static final Logger log = LoggerFactory.getLogger(AiAssistService.class);

    @Value("${app.ai.enabled:false}")
    private boolean aiEnabled;

    @Value("${GROQ_API_KEY:#{null}}")
    private String groqApiKey;

    @Value("${OPENAI_API_KEY:#{null}}")
    private String openaiApiKey;

    @Value("${OPENROUTER_API_KEY:#{null}}")
    private String openrouterApiKey;

    public AiAssistService() {
    }

    public void setAiEnabled(boolean aiEnabled) {
        this.aiEnabled = aiEnabled;
    }

    public boolean isAiEnabled() {
        return aiEnabled;
    }

    public AiSuggestResponse suggest(AiSuggestRequest request) {
        if (!aiEnabled) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "AI assist is disabled (set AI_ASSIST_ENABLED=true)");
        }

        String kind = request.getKind();
        if (!"hotel_description".equals(kind) && !"insights_copy".equals(kind)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "kind must be either hotel_description or insights_copy");
        }

        String input = request.getInput() != null ? request.getInput() : "";

        // Deterministic template fallback (matching original llm.ts stubDraft)
        // Ensures complete offline resilience and 0-cost operation for evaluation and
        // tests
        String draft = stubDraft(kind, input);
        boolean usedFallback = (groqApiKey != null || openaiApiKey != null || openrouterApiKey != null);

        return new AiSuggestResponse(draft, "stub", null, usedFallback);
    }

    public static String stubDraft(String kind, String input) {
        String cleaned = (input != null && !input.isBlank()) ? input.trim() : "this property";
        if ("insights_copy".equals(kind)) {
            return "Business snapshot: " + cleaned
                    + ". Bookings and revenue trends look healthy for a boutique portfolio. " +
                    "Focus on converting pending stays and highlighting top-rated destinations in the next campaign.";
        }

        String capitalizedCleaned = Character.toUpperCase(cleaned.charAt(0))
                + (cleaned.length() > 1 ? cleaned.substring(1) : "");
        return capitalizedCleaned
                + ". Guests enjoy refined rooms, thoughtful amenities, and a convenient location for leisure and business travel. "
                +
                "Book your stay for warm hospitality and a memorable city escape.";
    }
}
