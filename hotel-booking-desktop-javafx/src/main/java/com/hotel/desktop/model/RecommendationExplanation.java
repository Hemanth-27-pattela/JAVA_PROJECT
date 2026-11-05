package com.hotel.desktop.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Client-side model representing a deterministic, fact-grounded recommendation explanation.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecommendationExplanation {

    private String primaryReason;
    private List<String> supportingReasons = new ArrayList<>();
    private List<String> relevantFeatures = new ArrayList<>();

    public RecommendationExplanation() {
    }

    public RecommendationExplanation(String primaryReason, List<String> supportingReasons, List<String> relevantFeatures) {
        this.primaryReason = primaryReason;
        this.supportingReasons = supportingReasons != null ? new ArrayList<>(supportingReasons) : new ArrayList<>();
        this.relevantFeatures = relevantFeatures != null ? new ArrayList<>(relevantFeatures) : new ArrayList<>();
    }

    public String getPrimaryReason() {
        return primaryReason != null ? primaryReason : "";
    }

    public void setPrimaryReason(String primaryReason) {
        this.primaryReason = primaryReason;
    }

    public List<String> getSupportingReasons() {
        return supportingReasons != null ? supportingReasons : Collections.emptyList();
    }

    public void setSupportingReasons(List<String> supportingReasons) {
        this.supportingReasons = supportingReasons != null ? new ArrayList<>(supportingReasons) : new ArrayList<>();
    }

    public List<String> getRelevantFeatures() {
        return relevantFeatures != null ? relevantFeatures : Collections.emptyList();
    }

    public void setRelevantFeatures(List<String> relevantFeatures) {
        this.relevantFeatures = relevantFeatures != null ? new ArrayList<>(relevantFeatures) : new ArrayList<>();
    }

    public boolean hasSupportingReasons() {
        return supportingReasons != null && !supportingReasons.isEmpty();
    }

    public boolean hasRelevantFeatures() {
        return relevantFeatures != null && !relevantFeatures.isEmpty();
    }
}

