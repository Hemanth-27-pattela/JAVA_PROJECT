package com.hotel.booking.controllers;

import com.hotel.booking.dtos.ai.AiSuggestRequest;
import com.hotel.booking.dtos.ai.AiSuggestResponse;
import com.hotel.booking.services.AiAssistService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiAssistService aiAssistService;

    public AiController(AiAssistService aiAssistService) {
        this.aiAssistService = aiAssistService;
    }

    /**
     * Authenticated: generate an AI draft suggestion for hotel descriptions or insights copy.
     * POST /api/ai/suggest
     */
    @PostMapping("/suggest")
    public ResponseEntity<AiSuggestResponse> suggestAiAssist(@Valid @RequestBody AiSuggestRequest request) {
        AiSuggestResponse response = aiAssistService.suggest(request);
        return ResponseEntity.ok(response);
    }
}

