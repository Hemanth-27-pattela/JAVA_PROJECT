package com.hotel.desktop.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.desktop.util.JsonUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecommendationModelTest {

    private final ObjectMapper mapper = JsonUtils.getMapper();

    @Test
    @DisplayName("RecommendationDto deserializes from JSON with _id and nested explanation")
    void testRecommendationDtoDeserialization() throws Exception {
        String json = """
                {
                    "_id": "rec-hotel-101",
                    "name": "The Grand Ritz",
                    "city": "Paris",
                    "country": "France",
                    "pricePerNight": 220.50,
                    "averageRating": 4.85,
                    "reviewCount": 142,
                    "starRating": 5,
                    "imageUrl": "https://example.com/ritz.jpg",
                    "imageUrls": ["https://example.com/ritz.jpg", "https://example.com/ritz2.jpg"],
                    "type": ["Luxury", "Boutique"],
                    "facilities": ["Spa", "Outdoor Pool", "Free WiFi"],
                    "score": 0.9425,
                    "category": "PERSONALIZED",
                    "explanation": {
                        "primaryReason": "High guest rating of 4.85/5 across 142 verified reviews",
                        "supportingReasons": [
                            "Matches your preferred destination Paris",
                            "Features requested amenities: Spa, Outdoor Pool"
                        ],
                        "relevantFeatures": ["Spa", "Outdoor Pool", "Luxury"]
                    }
                }
                """;

        RecommendationDto dto = mapper.readValue(json, RecommendationDto.class);

        assertNotNull(dto);
        assertEquals("rec-hotel-101", dto.getId());
        assertEquals("The Grand Ritz", dto.getName());
        assertEquals("Paris", dto.getCity());
        assertEquals("France", dto.getCountry());
        assertEquals(new BigDecimal("220.50"), dto.getPricePerNight());
        assertEquals(4.85, dto.getAverageRating(), 0.001);
        assertEquals(142, dto.getReviewCount());
        assertEquals(5, dto.getStarRating());
        assertEquals(0.9425, dto.getScore(), 0.0001);
        assertEquals(RecommendationCategory.PERSONALIZED, dto.getCategory());

        // Check explanation
        RecommendationExplanation exp = dto.getExplanation();
        assertNotNull(exp);
        assertEquals("High guest rating of 4.85/5 across 142 verified reviews", exp.getPrimaryReason());
        assertTrue(exp.hasSupportingReasons());
        assertEquals(2, exp.getSupportingReasons().size());
        assertTrue(exp.hasRelevantFeatures());
        assertEquals(3, exp.getRelevantFeatures().size());
        assertEquals("Spa", exp.getRelevantFeatures().get(0));

        // Check presentation helpers
        assertEquals("Paris, France", dto.getLocationDisplay());
        assertEquals("£220.5", dto.getFormattedPrice());
        assertEquals("★★★★★", dto.getStarRatingDisplay());
        assertEquals("★ 4.9 / 5.0", dto.getRatingDisplay());
        assertEquals("(142 reviews)", dto.getReviewCountDisplay());
        assertEquals("94% Match", dto.getScorePercentageDisplay());
        assertEquals("https://example.com/ritz.jpg", dto.getFirstImageUrl());
    }

    @Test
    @DisplayName("RecommendationDto converts cleanly to Hotel entity for details navigation")
    void testToHotelConversion() {
        RecommendationDto dto = new RecommendationDto();
        dto.setId("hotel-nav-99");
        dto.setName("Alpine Crest Resort");
        dto.setCity("Geneva");
        dto.setCountry("Switzerland");
        dto.setPricePerNight(BigDecimal.valueOf(310));
        dto.setStarRating(4);
        dto.setAverageRating(4.6);
        dto.setReviewCount(88);
        dto.setImageUrl("https://example.com/alpine.jpg");
        dto.setType(List.of("Resort", "Ski"));
        dto.setFacilities(List.of("Heated Pool", "Sauna"));

        Hotel hotel = dto.toHotel();

        assertNotNull(hotel);
        assertEquals("hotel-nav-99", hotel.getId());
        assertEquals("Alpine Crest Resort", hotel.getName());
        assertEquals("Geneva", hotel.getCity());
        assertEquals("Switzerland", hotel.getCountry());
        assertEquals(BigDecimal.valueOf(310), hotel.getPricePerNight());
        assertEquals(4, hotel.getStarRating());
        assertEquals(4.6, hotel.getAverageRating(), 0.001);
        assertEquals(88, hotel.getReviewCount());
        assertTrue(hotel.getImageUrls().contains("https://example.com/alpine.jpg"));
        assertTrue(hotel.getType().contains("Resort"));
        assertTrue(hotel.getFacilities().contains("Heated Pool"));
        assertTrue(hotel.getIsActive());
        assertNotNull(hotel.getPolicies());
        assertEquals("15:00", hotel.getPolicies().getCheckInTime());
    }

    @Test
    @DisplayName("RecommendationCategory enum exposes icons, titles, and CSS badges")
    void testRecommendationCategoryEnum() {
        assertEquals("Personalized For You", RecommendationCategory.PERSONALIZED.getDisplayName());
        assertEquals("✨", RecommendationCategory.PERSONALIZED.getIcon());
        assertEquals("rec-badge-personalized", RecommendationCategory.PERSONALIZED.getBadgeStyleClass());

        assertEquals("Trending & Popular", RecommendationCategory.POPULAR.getDisplayName());
        assertEquals("🔥", RecommendationCategory.POPULAR.getIcon());
        assertEquals("rec-badge-popular", RecommendationCategory.POPULAR.getBadgeStyleClass());

        assertEquals("Top Rated Stays", RecommendationCategory.TOP_RATED.getDisplayName());
        assertEquals("🏆", RecommendationCategory.TOP_RATED.getIcon());
        assertEquals("rec-badge-top-rated", RecommendationCategory.TOP_RATED.getBadgeStyleClass());

        assertEquals("Similar Hotel", RecommendationCategory.SIMILAR.getDisplayName());
        assertEquals("🏨", RecommendationCategory.SIMILAR.getIcon());
        assertEquals("rec-badge-similar", RecommendationCategory.SIMILAR.getBadgeStyleClass());
    }

    @Test
    @DisplayName("Empty or default RecommendationDto handles null safety gracefully")
    void testNullSafetyDefaults() {
        RecommendationDto dto = new RecommendationDto();
        assertEquals("", dto.getLocationDisplay());
        assertEquals("£0", dto.getFormattedPrice());
        assertEquals("★", dto.getStarRatingDisplay());
        assertEquals("Unrated", dto.getRatingDisplay());
        assertEquals("(0 reviews)", dto.getReviewCountDisplay());
        assertEquals("0% Match", dto.getScorePercentageDisplay());
        assertNull(dto.getFirstImageUrl());
        assertNotNull(dto.getExplanation());
        assertFalse(dto.getExplanation().hasSupportingReasons());
        assertFalse(dto.getExplanation().hasRelevantFeatures());
    }
}

