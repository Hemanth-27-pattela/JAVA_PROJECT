package com.hotel.booking.services.recommendation;

import com.hotel.booking.dtos.recommendation.RecommendationExplanationDto;
import com.hotel.booking.entities.Hotel;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Generates fact-grounded, deterministic explanations for hotel recommendations.
 * Operates strictly on verified hotel attributes and guest booking history.
 * Does not make external network requests or call external LLM APIs.
 */
@Component
public class RecommendationExplanationGenerator {

    /**
     * Generates an explanation for a personalized recommendation.
     */
    public RecommendationExplanationDto generatePersonalizedExplanation(
            Hotel hotel,
            UserStayProfile profile,
            double destScore,
            double typeScore,
            double priceScore,
            double ratingScore
    ) {
        String primaryReason;
        List<String> supportingReasons = new ArrayList<>();
        List<String> relevantFeatures = new ArrayList<>();

        if (hotel == null) {
            return new RecommendationExplanationDto("Recommended for you.", List.of(), List.of());
        }

        // Determine primary driving feature
        if (profile != null && profile.hasHistory()) {
            if (destScore >= 0.99 && hotel.getCity() != null) {
                primaryReason = "Recommended because you previously booked stays in " + hotel.getCity() + ".";
                relevantFeatures.add("City: " + hotel.getCity());
            } else if (destScore >= 0.59 && hotel.getCountry() != null) {
                primaryReason = "Recommended because you previously visited " + hotel.getCountry() + ".";
                relevantFeatures.add("Country: " + hotel.getCountry());
            } else if (typeScore >= 0.30 && hotel.getType() != null && !hotel.getType().isEmpty()) {
                Set<String> shared = findSharedTypes(hotel.getType(), profile.getBookedHotelTypes());
                if (!shared.isEmpty()) {
                    primaryReason = "Matches your preferred hotel style (" + String.join(", ", shared) + ").";
                    relevantFeatures.add("Style: " + String.join(", ", shared));
                } else {
                    primaryReason = "Matches your preferred travel style.";
                }
            } else if (priceScore >= 0.70 && profile.getAverageNightlySpend() > 0) {
                primaryReason = "Fits your historical nightly spend range (~£" + Math.round(profile.getAverageNightlySpend()) + ").";
                relevantFeatures.add("Nightly Rate: £" + hotel.getPricePerNight());
            } else if (ratingScore >= 0.85) {
                primaryReason = "Highly rated by previous guests (" + formatRating(hotel.getAverageRating()) + "★).";
                relevantFeatures.add("Rating: " + formatRating(hotel.getAverageRating()) + "★");
            } else {
                primaryReason = "Popular choice among guests with strong traveler ratings.";
            }
        } else {
            // Cold start
            if (hotel.getAverageRating() != null && hotel.getAverageRating() >= 4.0) {
                primaryReason = "Top-rated hotel with " + formatRating(hotel.getAverageRating()) + "★ guest satisfaction.";
            } else if (hotel.getTotalBookings() != null && hotel.getTotalBookings() > 0) {
                primaryReason = "Popular choice with " + hotel.getTotalBookings() + " completed bookings.";
            } else {
                primaryReason = "Recommended quality stay in " + (hotel.getCity() != null ? hotel.getCity() : "our destination collection") + ".";
            }
        }

        // Supporting reasons
        if (hotel.getAverageRating() != null && hotel.getAverageRating() > 0.0) {
            supportingReasons.add("Rated " + formatRating(hotel.getAverageRating()) + "★ by guests (" + (hotel.getReviewCount() != null ? hotel.getReviewCount() : 0) + " reviews)");
        }
        if (hotel.getFacilities() != null && !hotel.getFacilities().isEmpty()) {
            supportingReasons.add("Offers " + hotel.getFacilities().size() + " amenities including " + hotel.getFacilities().get(0));
        }
        if (hotel.getPricePerNight() != null) {
            supportingReasons.add("Nightly rate from £" + hotel.getPricePerNight());
        }

        // Features
        if (hotel.getCity() != null && !relevantFeatures.stream().anyMatch(f -> f.startsWith("City:"))) {
            relevantFeatures.add("City: " + hotel.getCity());
        }
        if (hotel.getAverageRating() != null && !relevantFeatures.stream().anyMatch(f -> f.startsWith("Rating:"))) {
            relevantFeatures.add("Rating: " + formatRating(hotel.getAverageRating()) + "★");
        }
        if (hotel.getPricePerNight() != null && !relevantFeatures.stream().anyMatch(f -> f.startsWith("Nightly Rate:"))) {
            relevantFeatures.add("Price: £" + hotel.getPricePerNight());
        }

        return new RecommendationExplanationDto(primaryReason, supportingReasons, relevantFeatures);
    }

    /**
     * Generates an explanation for a popular hotel recommendation.
     */
    public RecommendationExplanationDto generatePopularExplanation(Hotel hotel) {
        if (hotel == null) {
            return new RecommendationExplanationDto("Popular among guests.", List.of(), List.of());
        }

        int bookings = hotel.getTotalBookings() != null ? hotel.getTotalBookings() : 0;
        String primaryReason = "Popular choice among guests with " + bookings + " total bookings.";

        List<String> supportingReasons = List.of(
                "Consistently chosen by travelers",
                "Quality rating of " + formatRating(hotel.getAverageRating()) + "★ across " + (hotel.getReviewCount() != null ? hotel.getReviewCount() : 0) + " reviews",
                "Located in " + (hotel.getCity() != null ? hotel.getCity() : "") + ", " + (hotel.getCountry() != null ? hotel.getCountry() : "")
        );

        List<String> relevantFeatures = List.of(
                "Total Bookings: " + bookings,
                "Average Rating: " + formatRating(hotel.getAverageRating()) + "★",
                "Location: " + (hotel.getCity() != null ? hotel.getCity() : "")
        );

        return new RecommendationExplanationDto(primaryReason, supportingReasons, relevantFeatures);
    }

    /**
     * Generates an explanation for a top-rated hotel recommendation.
     */
    public RecommendationExplanationDto generateTopRatedExplanation(Hotel hotel) {
        if (hotel == null) {
            return new RecommendationExplanationDto("Highly rated by previous guests.", List.of(), List.of());
        }

        String primaryReason = "Top rated by guests with an average score of " + formatRating(hotel.getAverageRating()) + "★.";

        List<String> supportingReasons = List.of(
                "Backed by " + (hotel.getReviewCount() != null ? hotel.getReviewCount() : 0) + " verified guest reviews",
                "Awarded " + (hotel.getStarRating() != null ? hotel.getStarRating() : 1) + "-star rating",
                "Exceptional overall guest satisfaction"
        );

        List<String> relevantFeatures = List.of(
                "Average Rating: " + formatRating(hotel.getAverageRating()) + "★",
                "Review Count: " + (hotel.getReviewCount() != null ? hotel.getReviewCount() : 0),
                "Star Rating: " + (hotel.getStarRating() != null ? hotel.getStarRating() : 1) + "★"
        );

        return new RecommendationExplanationDto(primaryReason, supportingReasons, relevantFeatures);
    }

    /**
     * Generates an explanation for an item-to-item similar hotel recommendation.
     */
    public RecommendationExplanationDto generateSimilarExplanation(Hotel source, Hotel candidate) {
        if (source == null || candidate == null) {
            return new RecommendationExplanationDto("Similar hotel option.", List.of(), List.of());
        }

        String primaryReason;
        boolean sameCity = candidate.getCity() != null && source.getCity() != null
                && candidate.getCity().trim().equalsIgnoreCase(source.getCity().trim());

        Set<String> sharedTypes = findSharedTypes(source.getType(), candidate.getType());
        Set<String> sharedFacilities = findSharedTypes(source.getFacilities(), candidate.getFacilities());

        if (sameCity) {
            primaryReason = "Similar to " + source.getName() + " in " + candidate.getCity() + ".";
        } else if (!sharedTypes.isEmpty()) {
            primaryReason = "Similar to " + source.getName() + " with matching " + String.join(", ", sharedTypes) + " style.";
        } else if (!sharedFacilities.isEmpty()) {
            primaryReason = "Similar to " + source.getName() + " because it offers matching facilities.";
        } else {
            primaryReason = "Similar to " + source.getName() + " with comparable price tier and star rating.";
        }

        List<String> supportingReasons = new ArrayList<>();
        if (!sharedFacilities.isEmpty()) {
            supportingReasons.add("Shared facilities: " + sharedFacilities.stream().limit(3).collect(Collectors.joining(", ")));
        }
        if (candidate.getPricePerNight() != null && source.getPricePerNight() != null) {
            supportingReasons.add("Comparable price: £" + candidate.getPricePerNight() + " vs £" + source.getPricePerNight());
        }
        if (candidate.getStarRating() != null) {
            supportingReasons.add(candidate.getStarRating() + "-star rated property");
        }

        List<String> relevantFeatures = new ArrayList<>();
        if (candidate.getCity() != null) {
            relevantFeatures.add("City: " + candidate.getCity());
        }
        if (candidate.getPricePerNight() != null) {
            relevantFeatures.add("Price: £" + candidate.getPricePerNight());
        }
        if (candidate.getStarRating() != null) {
            relevantFeatures.add("Star Rating: " + candidate.getStarRating() + "★");
        }

        return new RecommendationExplanationDto(primaryReason, supportingReasons, relevantFeatures);
    }

    private Set<String> findSharedTypes(List<String> a, Collection<String> b) {
        if (a == null || b == null || a.isEmpty() || b.isEmpty()) {
            return Set.of();
        }
        Set<String> normB = b.stream()
                .filter(Objects::nonNull)
                .map(s -> s.trim().toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());

        return a.stream()
                .filter(Objects::nonNull)
                .filter(s -> normB.contains(s.trim().toLowerCase(Locale.ROOT)))
                .map(String::trim)
                .collect(Collectors.toSet());
    }

    private String formatRating(Double rating) {
        if (rating == null) {
            return "0.0";
        }
        return String.format(Locale.ROOT, "%.1f", rating);
    }
}
