package com.hotel.booking.dtos.recommendation;

import java.util.List;

/**
 * Immutable explanation payload detailing why a specific hotel was recommended.
 * Fact-grounded and generated deterministically without external LLM calls.
 */
public record RecommendationExplanationDto(
        String primaryReason,
        List<String> supportingReasons,
        List<String> relevantFeatures
) {
    public RecommendationExplanationDto {
        supportingReasons = supportingReasons != null ? List.copyOf(supportingReasons) : List.of();
        relevantFeatures = relevantFeatures != null ? List.copyOf(relevantFeatures) : List.of();
    }
}

