package com.hotel.booking.controllers;

import com.hotel.booking.dtos.recommendation.RecommendationDto;
import com.hotel.booking.security.UserPrincipal;
import com.hotel.booking.services.recommendation.RecommendationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * REST controller exposing recommendation endpoints.
 * Public endpoints: popular, top-rated, similar.
 * Authenticated endpoints: personalized (requires authenticated USER).
 */
@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    public static final int DEFAULT_LIMIT = 10;
    public static final int MIN_LIMIT = 1;
    public static final int MAX_LIMIT = 50;

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    /**
     * Personalized recommendations for the authenticated guest.
     * Derives identity strictly from UserPrincipal to prevent cross-user access.
     */
    @GetMapping("/personalized")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<RecommendationDto>> getPersonalizedRecommendations(
            @RequestParam(value = "limit", required = false, defaultValue = "10") Integer limit,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        int validatedLimit = validateLimit(limit);
        if (principal == null || principal.getId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        List<RecommendationDto> recommendations = recommendationService.getPersonalizedRecommendations(principal.getId(), validatedLimit);
        return ResponseEntity.ok(recommendations);
    }

    /**
     * Public endpoint for popular hotels ranked by booking volume.
     */
    @GetMapping("/popular")
    public ResponseEntity<List<RecommendationDto>> getPopularRecommendations(
            @RequestParam(value = "limit", required = false, defaultValue = "10") Integer limit
    ) {
        int validatedLimit = validateLimit(limit);
        List<RecommendationDto> recommendations = recommendationService.getPopularRecommendations(validatedLimit);
        return ResponseEntity.ok(recommendations);
    }

    /**
     * Public endpoint for top-rated hotels ranked by Bayesian rating score.
     */
    @GetMapping("/top-rated")
    public ResponseEntity<List<RecommendationDto>> getTopRatedRecommendations(
            @RequestParam(value = "limit", required = false, defaultValue = "10") Integer limit
    ) {
        int validatedLimit = validateLimit(limit);
        List<RecommendationDto> recommendations = recommendationService.getTopRatedRecommendations(validatedLimit);
        return ResponseEntity.ok(recommendations);
    }

    /**
     * Public endpoint for item-to-item similar hotels.
     */
    @GetMapping("/similar/{hotelId}")
    public ResponseEntity<List<RecommendationDto>> getSimilarRecommendations(
            @PathVariable String hotelId,
            @RequestParam(value = "limit", required = false, defaultValue = "10") Integer limit
    ) {
        if (hotelId == null || hotelId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hotel ID cannot be blank");
        }
        int validatedLimit = validateLimit(limit);
        List<RecommendationDto> recommendations = recommendationService.getSimilarRecommendations(hotelId.trim(), validatedLimit);
        return ResponseEntity.ok(recommendations);
    }

    private int validateLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_LIMIT;
        }
        if (limit < MIN_LIMIT || limit > MAX_LIMIT) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Limit must be between " + MIN_LIMIT + " and " + MAX_LIMIT
            );
        }
        return limit;
    }
}

