package com.hotel.booking.services.recommendation;

import com.hotel.booking.entities.Hotel;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implements deterministic scoring algorithms for the hybrid recommendation engine.
 * All component scores are normalized to the [0.0, 1.0] interval.
 */
@Component
public class RecommendationScorer {

    // Bayesian Rating Constants
    public static final double BAYESIAN_PRIOR_WEIGHT_M = 3.0;
    public static final double BAYESIAN_PRIOR_RATING_C = 4.0;
    public static final double MAX_STAR_RATING = 5.0;
    public static final double SATURATION_FACILITY_COUNT = 6.0;

    // Hybrid Weights - History Present (sum = 1.00)
    public static final double WEIGHT_PERSONAL = 0.45;
    public static final double WEIGHT_CONTENT = 0.20;
    public static final double WEIGHT_RATING = 0.20;
    public static final double WEIGHT_POPULARITY = 0.15;

    // Hybrid Weights - Cold Start (sum = 1.00)
    public static final double COLD_WEIGHT_CONTENT = 0.30;
    public static final double COLD_WEIGHT_RATING = 0.45;
    public static final double COLD_WEIGHT_POPULARITY = 0.25;

    // Personalization Sub-Weights (sum = 1.00)
    public static final double SUB_WEIGHT_DESTINATION = 0.45;
    public static final double SUB_WEIGHT_TYPE = 0.35;
    public static final double SUB_WEIGHT_PRICE = 0.20;

    // Similarity Weights (sum = 1.00)
    public static final double SIM_WEIGHT_DESTINATION = 0.30;
    public static final double SIM_WEIGHT_TYPE = 0.25;
    public static final double SIM_WEIGHT_FACILITY = 0.20;
    public static final double SIM_WEIGHT_PRICE = 0.15;
    public static final double SIM_WEIGHT_STAR = 0.10;

    /**
     * Destination affinity based on user's past confirmed/completed stays:
     * - Same city: 1.0
     * - Same country: 0.6
     * - Otherwise: 0.0
     */
    public double calculateDestinationAffinity(String hotelCity, String hotelCountry, UserStayProfile profile) {
        if (profile == null || !profile.hasHistory()) {
            return 0.0;
        }
        if (hotelCity != null && profile.hasBookedCity(hotelCity)) {
            return 1.0;
        }
        if (hotelCountry != null && profile.hasBookedCountry(hotelCountry)) {
            return 0.6;
        }
        return 0.0;
    }

    /**
     * Jaccard similarity between two sets of string tokens (e.g. hotel types, facilities).
     * Compares strings case-insensitively.
     * Returns 0.0 when union is empty or either collection is null/empty.
     */
    public double calculateJaccardSimilarity(Collection<String> setA, Collection<String> setB) {
        if (setA == null || setB == null || setA.isEmpty() || setB.isEmpty()) {
            return 0.0;
        }

        Set<String> normA = setA.stream()
                .filter(Objects::nonNull)
                .map(s -> s.trim().toLowerCase(Locale.ROOT))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());

        Set<String> normB = setB.stream()
                .filter(Objects::nonNull)
                .map(s -> s.trim().toLowerCase(Locale.ROOT))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());

        if (normA.isEmpty() || normB.isEmpty()) {
            return 0.0;
        }

        Set<String> intersection = new HashSet<>(normA);
        intersection.retainAll(normB);

        Set<String> union = new HashSet<>(normA);
        union.addAll(normB);

        if (union.isEmpty()) {
            return 0.0;
        }

        double score = (double) intersection.size() / union.size();
        return clamp(score);
    }

    public double calculateTypeAffinity(Collection<String> hotelTypes, Collection<String> userBookedTypes) {
        return calculateJaccardSimilarity(hotelTypes, userBookedTypes);
    }

    /**
     * Price compatibility comparing candidate hotel price against user's historical average spend:
     * S_price = max(0, 1 - (|hotelPrice - avgSpend| / max(hotelPrice, avgSpend)))
     */
    public double calculatePriceCompatibility(double hotelPrice, double averageSpend) {
        if (hotelPrice <= 0.0 || averageSpend <= 0.0) {
            return 0.0;
        }
        if (Double.compare(hotelPrice, averageSpend) == 0) {
            return 1.0;
        }
        double maxPrice = Math.max(hotelPrice, averageSpend);
        double diff = Math.abs(hotelPrice - averageSpend);
        double score = 1.0 - (diff / maxPrice);
        return clamp(score);
    }

    /**
     * Composite personalized score:
     * S_personal = 0.45 * S_destination + 0.35 * S_type + 0.20 * S_price
     */
    public double calculatePersonalScore(Hotel hotel, UserStayProfile profile) {
        if (hotel == null || profile == null || !profile.hasHistory()) {
            return 0.0;
        }

        double sDest = calculateDestinationAffinity(hotel.getCity(), hotel.getCountry(), profile);
        double sType = calculateTypeAffinity(hotel.getType(), profile.getBookedHotelTypes());

        double hotelPrice = hotel.getPricePerNight() != null ? hotel.getPricePerNight().doubleValue() : 0.0;
        double sPrice = calculatePriceCompatibility(hotelPrice, profile.getAverageNightlySpend());

        double sPersonal = (SUB_WEIGHT_DESTINATION * sDest)
                + (SUB_WEIGHT_TYPE * sType)
                + (SUB_WEIGHT_PRICE * sPrice);

        return clamp(sPersonal);
    }

    /**
     * Bayesian rating score:
     * S_rating = (v * R + m * C) / ((v + m) * 5.0)
     * For v = 0 reviews: (3.0 * 4.0) / (3.0 * 5.0) = 12.0 / 15.0 = 0.80
     */
    public double calculateBayesianRatingScore(Integer reviewCount, Double averageRating) {
        double v = Math.max(0, reviewCount != null ? reviewCount : 0);
        double r = averageRating != null ? clamp(averageRating, 0.0, MAX_STAR_RATING) : 0.0;

        double numerator = (v * r) + (BAYESIAN_PRIOR_WEIGHT_M * BAYESIAN_PRIOR_RATING_C);
        double denominator = (v + BAYESIAN_PRIOR_WEIGHT_M) * MAX_STAR_RATING;

        return clamp(numerator / denominator);
    }

    /**
     * Logarithmic popularity score:
     * S_popularity = ln(1 + hotelBookings) / ln(1 + maxHotelBookings + 1)
     * When maxHotelBookings <= 0 or hotelBookings <= 0, returns 0.0.
     * Prevents division by zero or NaN.
     */
    public double calculatePopularityScore(Integer hotelBookings, int maxHotelBookings) {
        int b = Math.max(0, hotelBookings != null ? hotelBookings : 0);
        int maxB = Math.max(0, maxHotelBookings);

        if (maxB <= 0 || b <= 0) {
            return 0.0;
        }

        double num = Math.log(1.0 + b);
        double den = Math.log(1.0 + maxB + 1.0);

        if (den <= 0.0) {
            return 0.0;
        }

        return clamp(num / den);
    }

    /**
     * Content match score:
     * S_content = 0.5 * (starRating / 5.0) + 0.5 * (min(facilityCount, 6) / 6.0)
     */
    public double calculateContentMatchScore(Integer starRating, Integer facilityCount) {
        double stars = starRating != null ? clamp(starRating, 0.0, MAX_STAR_RATING) : 0.0;
        double facilities = facilityCount != null ? clamp(facilityCount, 0.0, SATURATION_FACILITY_COUNT) : 0.0;

        double starComponent = 0.5 * (stars / MAX_STAR_RATING);
        double facilityComponent = 0.5 * (facilities / SATURATION_FACILITY_COUNT);

        return clamp(starComponent + facilityComponent);
    }

    /**
     * Hybrid score combining Personalization, Content, Bayesian Rating, and Popularity.
     * Selects dynamic weights depending on whether the guest has booking history.
     */
    public double calculateHybridScore(Hotel hotel, UserStayProfile profile, int maxBookings) {
        if (hotel == null) {
            return 0.0;
        }

        int facCount = hotel.getFacilities() != null ? hotel.getFacilities().size() : 0;
        double sContent = calculateContentMatchScore(hotel.getStarRating(), facCount);
        double sRating = calculateBayesianRatingScore(hotel.getReviewCount(), hotel.getAverageRating());
        double sPop = calculatePopularityScore(hotel.getTotalBookings(), maxBookings);

        if (profile != null && profile.hasHistory()) {
            double sPers = calculatePersonalScore(hotel, profile);
            double score = (WEIGHT_PERSONAL * sPers)
                    + (WEIGHT_CONTENT * sContent)
                    + (WEIGHT_RATING * sRating)
                    + (WEIGHT_POPULARITY * sPop);
            return clamp(score);
        } else {
            // Cold start hybrid
            double score = (COLD_WEIGHT_CONTENT * sContent)
                    + (COLD_WEIGHT_RATING * sRating)
                    + (COLD_WEIGHT_POPULARITY * sPop);
            return clamp(score);
        }
    }

    /**
     * Cold-start hybrid scoring convenience method.
     */
    public double calculateColdStartScore(Hotel hotel, int maxBookings) {
        return calculateHybridScore(hotel, null, maxBookings);
    }

    /**
     * Item-to-item similarity score between a source hotel and a candidate hotel:
     * - City/Country similarity (0.30)
     * - Hotel type overlap (0.25)
     * - Facility overlap (0.20)
     * - Price similarity (0.15)
     * - Star rating similarity (0.10)
     * Excludes self-similarity (returns 0.0 if same ID).
     */
    public double calculateSimilarityScore(Hotel source, Hotel candidate) {
        if (source == null || candidate == null) {
            return 0.0;
        }
        if (source.getId() != null && source.getId().equals(candidate.getId())) {
            return 0.0;
        }

        // 1. Destination
        double sDest = 0.0;
        if (candidate.getCity() != null && source.getCity() != null
                && candidate.getCity().trim().equalsIgnoreCase(source.getCity().trim())) {
            sDest = 1.0;
        } else if (candidate.getCountry() != null && source.getCountry() != null
                && candidate.getCountry().trim().equalsIgnoreCase(source.getCountry().trim())) {
            sDest = 0.6;
        }

        // 2. Type overlap
        double sType = calculateJaccardSimilarity(source.getType(), candidate.getType());

        // 3. Facility overlap
        double sFac = calculateJaccardSimilarity(source.getFacilities(), candidate.getFacilities());

        // 4. Price similarity
        double priceSrc = source.getPricePerNight() != null ? source.getPricePerNight().doubleValue() : 0.0;
        double priceCand = candidate.getPricePerNight() != null ? candidate.getPricePerNight().doubleValue() : 0.0;
        double sPrice;
        if (Double.compare(priceSrc, priceCand) == 0) {
            sPrice = 1.0;
        } else if (Math.max(priceSrc, priceCand) > 0.0) {
            sPrice = clamp(1.0 - (Math.abs(priceSrc - priceCand) / Math.max(priceSrc, priceCand)));
        } else {
            sPrice = 1.0;
        }

        // 5. Star rating similarity
        int starSrc = source.getStarRating() != null ? source.getStarRating() : 1;
        int starCand = candidate.getStarRating() != null ? candidate.getStarRating() : 1;
        double sStar = clamp(1.0 - (Math.abs(starSrc - starCand) / 4.0));

        double sim = (SIM_WEIGHT_DESTINATION * sDest)
                + (SIM_WEIGHT_TYPE * sType)
                + (SIM_WEIGHT_FACILITY * sFac)
                + (SIM_WEIGHT_PRICE * sPrice)
                + (SIM_WEIGHT_STAR * sStar);

        return clamp(sim);
    }

    private double clamp(double value) {
        return clamp(value, 0.0, 1.0);
    }

    private double clamp(double value, double min, double max) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return min;
        }
        return Math.max(min, Math.min(max, value));
    }
}

