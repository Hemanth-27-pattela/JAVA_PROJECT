package com.hotel.booking.services.recommendation;

import com.hotel.booking.dtos.recommendation.RecommendationCategory;
import com.hotel.booking.dtos.recommendation.RecommendationDto;
import com.hotel.booking.dtos.recommendation.RecommendationExplanationDto;
import com.hotel.booking.entities.Hotel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Core orchestration engine for ranking and generating hotel recommendations.
 * Combines Content-Based Filtering, Popularity-Based Ranking, Bayesian Rating,
 * and Personalized Guest History.
 *
 * Adheres strictly to security and data rules:
 * - Inactive hotels are excluded.
 * - Source hotel is excluded from similar recommendations.
 * - All component calculations are deterministic and explainable.
 */
@Service
public class RecommendationEngine {

    private final RecommendationScorer scorer;
    private final RecommendationExplanationGenerator explanationGenerator;

    public RecommendationEngine(
            RecommendationScorer scorer,
            RecommendationExplanationGenerator explanationGenerator
    ) {
        this.scorer = scorer;
        this.explanationGenerator = explanationGenerator;
    }

    /**
     * Ranks hotels for an authenticated guest using their past stay profile.
     * If the profile has no history, falls back gracefully to cold-start hybrid ranking.
     * Strictly filters for active hotels only.
     */
    public List<RecommendationDto> rankPersonalized(
            List<Hotel> candidateHotels,
            UserStayProfile profile,
            int limit
    ) {
        if (candidateHotels == null || candidateHotels.isEmpty()) {
            return Collections.emptyList();
        }

        List<Hotel> activeHotels = filterActiveHotels(candidateHotels);
        if (activeHotels.isEmpty()) {
            return Collections.emptyList();
        }

        int maxBookings = calculateMaxBookings(activeHotels);
        int effectiveLimit = limit > 0 ? limit : activeHotels.size();

        List<RecommendationDto> recommendations = new ArrayList<>();
        for (Hotel hotel : activeHotels) {
            double hybridScore = scorer.calculateHybridScore(hotel, profile, maxBookings);

            double destScore = scorer.calculateDestinationAffinity(hotel.getCity(), hotel.getCountry(), profile);
            double typeScore = scorer.calculateTypeAffinity(hotel.getType(), profile != null ? profile.getBookedHotelTypes() : Collections.emptySet());
            double hotelPrice = hotel.getPricePerNight() != null ? hotel.getPricePerNight().doubleValue() : 0.0;
            double priceScore = scorer.calculatePriceCompatibility(hotelPrice, profile != null ? profile.getAverageNightlySpend() : 0.0);
            double ratingScore = scorer.calculateBayesianRatingScore(hotel.getReviewCount(), hotel.getAverageRating());

            RecommendationExplanationDto explanation = explanationGenerator.generatePersonalizedExplanation(
                    hotel, profile, destScore, typeScore, priceScore, ratingScore
            );

            recommendations.add(RecommendationDto.fromHotel(
                    hotel, hybridScore, RecommendationCategory.PERSONALIZED, explanation
            ));
        }

        recommendations.sort(Comparator.comparingDouble(RecommendationDto::getScore).reversed());
        return recommendations.stream().limit(effectiveLimit).collect(Collectors.toList());
    }

    /**
     * Ranks hotels based on popularity (total confirmed bookings normalized logarithmically).
     * Strictly filters for active hotels only.
     */
    public List<RecommendationDto> rankPopular(List<Hotel> candidateHotels, int limit) {
        if (candidateHotels == null || candidateHotels.isEmpty()) {
            return Collections.emptyList();
        }

        List<Hotel> activeHotels = filterActiveHotels(candidateHotels);
        if (activeHotels.isEmpty()) {
            return Collections.emptyList();
        }

        int maxBookings = calculateMaxBookings(activeHotels);
        int effectiveLimit = limit > 0 ? limit : activeHotels.size();

        List<RecommendationDto> recommendations = new ArrayList<>();
        for (Hotel hotel : activeHotels) {
            double popScore = scorer.calculatePopularityScore(hotel.getTotalBookings(), maxBookings);
            RecommendationExplanationDto explanation = explanationGenerator.generatePopularExplanation(hotel);

            recommendations.add(RecommendationDto.fromHotel(
                    hotel, popScore, RecommendationCategory.POPULAR, explanation
            ));
        }

        recommendations.sort(Comparator.comparingDouble(RecommendationDto::getScore).reversed());
        return recommendations.stream().limit(effectiveLimit).collect(Collectors.toList());
    }

    /**
     * Ranks hotels based on Bayesian rating score.
     * Strictly filters for active hotels only.
     */
    public List<RecommendationDto> rankTopRated(List<Hotel> candidateHotels, int limit) {
        if (candidateHotels == null || candidateHotels.isEmpty()) {
            return Collections.emptyList();
        }

        List<Hotel> activeHotels = filterActiveHotels(candidateHotels);
        if (activeHotels.isEmpty()) {
            return Collections.emptyList();
        }

        int effectiveLimit = limit > 0 ? limit : activeHotels.size();

        List<RecommendationDto> recommendations = new ArrayList<>();
        for (Hotel hotel : activeHotels) {
            double bayesianScore = scorer.calculateBayesianRatingScore(hotel.getReviewCount(), hotel.getAverageRating());
            RecommendationExplanationDto explanation = explanationGenerator.generateTopRatedExplanation(hotel);

            recommendations.add(RecommendationDto.fromHotel(
                    hotel, bayesianScore, RecommendationCategory.TOP_RATED, explanation
            ));
        }

        recommendations.sort(Comparator.comparingDouble(RecommendationDto::getScore).reversed());
        return recommendations.stream().limit(effectiveLimit).collect(Collectors.toList());
    }

    /**
     * Finds similar hotels to a given source hotel using multi-attribute similarity:
     * destination, hotel type, facilities, price tier, and star rating.
     * Excludes the source hotel itself and any inactive hotels.
     */
    public List<RecommendationDto> rankSimilar(
            Hotel sourceHotel,
            List<Hotel> candidateHotels,
            int limit
    ) {
        if (sourceHotel == null || candidateHotels == null || candidateHotels.isEmpty()) {
            return Collections.emptyList();
        }

        // Filter active hotels AND exclude source hotel
        List<Hotel> eligibleCandidates = candidateHotels.stream()
                .filter(Objects::nonNull)
                .filter(h -> Boolean.TRUE.equals(h.getIsActive()))
                .filter(h -> h.getId() == null || !h.getId().equals(sourceHotel.getId()))
                .collect(Collectors.toList());

        if (eligibleCandidates.isEmpty()) {
            return Collections.emptyList();
        }

        int effectiveLimit = limit > 0 ? limit : eligibleCandidates.size();

        List<RecommendationDto> recommendations = new ArrayList<>();
        for (Hotel candidate : eligibleCandidates) {
            double simScore = scorer.calculateSimilarityScore(sourceHotel, candidate);
            RecommendationExplanationDto explanation = explanationGenerator.generateSimilarExplanation(sourceHotel, candidate);

            recommendations.add(RecommendationDto.fromHotel(
                    candidate, simScore, RecommendationCategory.SIMILAR, explanation
            ));
        }

        recommendations.sort(Comparator.comparingDouble(RecommendationDto::getScore).reversed());
        return recommendations.stream().limit(effectiveLimit).collect(Collectors.toList());
    }

    /**
     * Filters a list of hotels to retain only active hotels.
     */
    public List<Hotel> filterActiveHotels(List<Hotel> hotels) {
        if (hotels == null) {
            return Collections.emptyList();
        }
        return hotels.stream()
                .filter(Objects::nonNull)
                .filter(h -> Boolean.TRUE.equals(h.getIsActive()))
                .collect(Collectors.toList());
    }

    private int calculateMaxBookings(List<Hotel> hotels) {
        return hotels.stream()
                .filter(Objects::nonNull)
                .map(Hotel::getTotalBookings)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0);
    }

    public RecommendationScorer getScorer() {
        return scorer;
    }

    public RecommendationExplanationGenerator getExplanationGenerator() {
        return explanationGenerator;
    }
}

