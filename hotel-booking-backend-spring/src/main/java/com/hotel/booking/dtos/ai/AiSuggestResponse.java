package com.hotel.booking.dtos.ai;

public class AiSuggestResponse {
    private String draft;
    private String provider;
    private String model;
    private Boolean usedFallback = false;

    public AiSuggestResponse() {
    }

    public AiSuggestResponse(String draft, String provider, String model, Boolean usedFallback) {
        this.draft = draft;
        this.provider = provider;
        this.model = model;
        this.usedFallback = usedFallback;
    }

    public String getDraft() { return draft; }
    public void setDraft(String draft) { this.draft = draft; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public Boolean getUsedFallback() { return usedFallback; }
    public void setUsedFallback(Boolean usedFallback) { this.usedFallback = usedFallback; }
}

