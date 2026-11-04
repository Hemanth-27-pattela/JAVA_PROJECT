package com.hotel.booking.services.recommendation;

import com.hotel.booking.dtos.recommendation.RecommendationCategory;
import com.hotel.booking.dtos.recommendation.RecommendationDto;
import com.hotel.booking.dtos.recommendation.RecommendationExplanationDto;
import com.hotel.booking.entities.Hotel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Recommendation DTO Tests")
class RecommendationDtoTest {

    @Test
    @DisplayName("Maps Hotel entity accurately into RecommendationDto")
    void recommendationDto_fromHotel_mapsFields() {
        Hotel h = new Hotel();
        h.setId("hotel-101");
        h.setName("The Grand Hotel");
        h.setCity("London");
        h.setCountry("United Kingdom");
        h.setPricePerNight(BigDecimal.valueOf(175.50));
        h.setAverageRating(4.7);
        h.setReviewCount(42);
        h.setStarRating(4);
        h.setImageUrls(List.of("https://example.com/h1.jpg", "https://example.com/h2.jpg"));
        h.setType(List.of("Boutique", "Historic"));
        h.setFacilities(List.of("WiFi", "Gym"));

        RecommendationExplanationDto explanation = new RecommendationExplanationDto(
                "Great match for you",
                List.of("Matches preferred city"),
                List.of("City: London")
        );

        RecommendationDto dto = RecommendationDto.fromHotel(
                h, 0.85678, RecommendationCategory.PERSONALIZED, explanation
        );

        assertEquals("hotel-101", dto.getId());
        assertEquals("The Grand Hotel", dto.getName());
        assertEquals("London", dto.getCity());
        assertEquals("United Kingdom", dto.getCountry());
        assertEquals(BigDecimal.valueOf(175.50), dto.getPricePerNight());
        assertEquals(4.7, dto.getAverageRating());
        assertEquals(42, dto.getReviewCount());
        assertEquals(4, dto.getStarRating());
        assertEquals("https://example.com/h1.jpg", dto.getImageUrl());
        assertEquals(2, dto.getImageUrls().size());
        assertEquals(List.of("Boutique", "Historic"), dto.getType());
        assertEquals(List.of("WiFi", "Gym"), dto.getFacilities());
        assertEquals(0.8568, dto.getScore(), 0.0001); // 4-decimal scale
        assertEquals(RecommendationCategory.PERSONALIZED, dto.getCategory());
        assertEquals(explanation, dto.getExplanation());
    }

    @Test
    @DisplayName("RecommendationDto clamps raw score to [0.0, 1.0]")
    void recommendationDto_clampsScore() {
        RecommendationDto low = RecommendationDto.fromHotel(null, -0.5, RecommendationCategory.POPULAR, null);
        assertEquals(0.0, low.getScore(), 0.0001);

        RecommendationDto high = RecommendationDto.fromHotel(null, 1.8, RecommendationCategory.POPULAR, null);
        assertEquals(1.0, high.getScore(), 0.0001);
    }

    @Test
    @DisplayName("RecommendationExplanationDto protects list immutability")
    void explanationDto_immutableLists() {
        RecommendationExplanationDto dto = new RecommendationExplanationDto(
                "Primary",
                List.of("Support 1"),
                List.of("Feature 1")
        );

        assertThrows(UnsupportedOperationException.class, () -> dto.supportingReasons().add("New Reason"));
        assertThrows(UnsupportedOperationException.class, () -> dto.relevantFeatures().add("New Feature"));

        // Null list parameters default to empty immutable lists
        RecommendationExplanationDto nullDto = new RecommendationExplanationDto("Primary", null, null);
        assertNotNull(nullDto.supportingReasons());
        assertTrue(nullDto.supportingReasons().isEmpty());
        assertNotNull(nullDto.relevantFeatures());
        assertTrue(nullDto.relevantFeatures().isEmpty());
    }
}

