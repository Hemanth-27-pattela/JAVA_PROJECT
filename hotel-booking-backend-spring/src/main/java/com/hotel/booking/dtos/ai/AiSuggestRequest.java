package com.hotel.booking.dtos.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.Map;

public class AiSuggestRequest {

    @NotBlank(message = "kind is required")
    @Pattern(regexp = "^(hotel_description|insights_copy)$", message = "kind must be either hotel_description or insights_copy")
    private String kind;

    private String input;
    private Map<String, Object> context;

    public AiSuggestRequest() {
    }

    public AiSuggestRequest(String kind, String input) {
        this.kind = kind;
        this.input = input;
    }

    public AiSuggestRequest(String kind, String input, Map<String, Object> context) {
        this.kind = kind;
        this.input = input;
        this.context = context;
    }

    public String getKind() { return kind; }
    public void setKind(String kind) { this.kind = kind; }

    public String getInput() { return input; }
    public void setInput(String input) { this.input = input; }

    public Map<String, Object> getContext() { return context; }
    public void setContext(Map<String, Object> context) { this.context = context; }
}

