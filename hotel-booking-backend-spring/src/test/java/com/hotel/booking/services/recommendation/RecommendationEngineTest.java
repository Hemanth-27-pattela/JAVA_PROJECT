package com.hotel.booking.services.recommendation;

import com.hotel.booking.dtos.recommendation.RecommendationCategory;
import com.hotel.booking.dtos.recommendation.RecommendationDto;
import com.hotel.booking.entities.Hotel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RecommendationEngine Unit Tests")
class RecommendationEngineTest {

    private RecommendationScorer scorer;
    private RecommendationExplanationGenerator explanationGenerator;
    private RecommendationEngine engine;

    private Hotel activeHotelLondon;
    private Hotel activeHotelParis;
    private Hotel activeHotelTokyo;
    private Hotel inactiveHotel;

    @BeforeEach
    void setUp() {
        scorer = new RecommendationScorer();
        explanationGenerator = new RecommendationExplanationGenerator();
        engine = new RecommendationEngine(scorer, explanationGenerator);

        activeHotelLondon = createHotel("h-1", "London Thames Boutique", "London", "United Kingdom",
                List.of("Boutique", "Luxury"), List.of("WiFi", "Gym", "Spa", "Pool"),
                150.0, 5, 4.8, 25, 50, true);

        activeHotelParis = createHotel("h-2", "Parisian Seine Retreat", "Paris", "France",
                List.of("Romantic", "Boutique"), List.of("WiFi", "Restaurant", "Bar"),
                200.0, 4, 4.5, 15, 30, true);

        activeHotelTokyo = createHotel("h-3", "Tokyo Shinjuku Tower", "Tokyo", "Japan",
                List.of("Modern", "Business"), List.of("WiFi", "Business Center", "Gym"),
                250.0, 4, 4.2, 10, 15, true);

        inactiveHotel = createHotel("h-inactive", "Closed Grand Resort", "London", "United Kingdom",
                List.of("Luxury"), List.of("WiFi", "Pool"),
                100.0, 5, 5.0, 100, 200, false); // isActive = false
    }

    private Hotel createHotel(String id, String name, String city, String country,
                              List<String> types, List<String> facilities,
                              double price, int starRating, double avgRating,
                              int reviewCount, int bookings, boolean isActive) {
        Hotel h = new Hotel();
        h.setId(id);
        h.setName(name);
        h.setCity(city);
        h.setCountry(country);
        h.setType(types);
        h.setFacilities(facilities);
        h.setPricePerNight(BigDecimal.valueOf(price));
        h.setStarRating(starRating);
        h.setAverageRating(avgRating);
        h.setReviewCount(reviewCount);
        h.setTotalBookings(bookings);
        h.setIsActive(isActive);
        h.setImageUrls(List.of("https://example.com/images/" + id + ".jpg"));
        return h;
    }

    @Nested
    @DisplayName("13: Inactive Hotel Exclusion Tests")
    class InactiveHotelExclusionTests {

        @Test
        @DisplayName("Scenario 13a: Inactive hotels are strictly excluded from personalized recommendations")
        void rankPersonalized_excludesInactiveHotels() {
            List<Hotel> pool = List.of(activeHotelLondon, activeHotelParis, inactiveHotel);
            UserStayProfile profile = new UserStayProfile(Set.of("London"), Set.of("United Kingdom"), Set.of("Boutique"), 150.0, 1);

            List<RecommendationDto> results = engine.rankPersonalized(pool, profile, 10);

            assertFalse(results.isEmpty());
            assertTrue(results.stream().noneMatch(r -> "h-inactive".equals(r.getId())));
            assertTrue(results.stream().anyMatch(r -> "h-1".equals(r.getId())));
        }

        @Test
        @DisplayName("Scenario 13b: Inactive hotels are strictly excluded from popular recommendations")
        void rankPopular_excludesInactiveHotels() {
            List<Hotel> pool = List.of(activeHotelLondon, activeHotelParis, inactiveHotel);

            List<RecommendationDto> results = engine.rankPopular(pool, 10);

            assertTrue(results.stream().noneMatch(r -> "h-inactive".equals(r.getId())));
        }

        @Test
        @DisplayName("Scenario 13c: Inactive hotels are strictly excluded from top-rated recommendations")
        void rankTopRated_excludesInactiveHotels() {
            List<Hotel> pool = List.of(activeHotelLondon, activeHotelParis, inactiveHotel);

            List<RecommendationDto> results = engine.rankTopRated(pool, 10);

            assertTrue(results.stream().noneMatch(r -> "h-inactive".equals(r.getId())));
        }

        @Test
        @DisplayName("Scenario 13d: Inactive hotels and source hotel are strictly excluded from similar recommendations")
        void rankSimilar_excludesInactiveAndSourceHotels() {
            List<Hotel> pool = List.of(activeHotelLondon, activeHotelParis, activeHotelTokyo, inactiveHotel);

            List<RecommendationDto> results = engine.rankSimilar(activeHotelLondon, pool, 10);

            // Neither the source hotel (h-1) nor the inactive hotel (h-inactive) may be in results
            assertTrue(results.stream().noneMatch(r -> "h-1".equals(r.getId())));
            assertTrue(results.stream().noneMatch(r -> "h-inactive".equals(r.getId())));
            assertTrue(results.stream().allMatch(r -> RecommendationCategory.SIMILAR.equals(r.getCategory())));
        }
    }

    @Nested
    @DisplayName("14: Explanation Generation Tests")
    class ExplanationGenerationTests {

        @Test
        @DisplayName("Scenario 14a: Personalized explanation reflects destination match and verified reasons")
        void explanation_personalized_destinationMatch() {
            UserStayProfile profile = new UserStayProfile(Set.of("London"), Set.of("United Kingdom"), Set.of("Boutique"), 150.0, 1);
            List<RecommendationDto> recs = engine.rankPersonalized(List.of(activeHotelLondon), profile, 5);

            assertEquals(1, recs.size());
            RecommendationDto dto = recs.get(0);
            assertNotNull(dto.getExplanation());
            assertTrue(dto.getExplanation().primaryReason().contains("London"));
            assertFalse(dto.getExplanation().supportingReasons().isEmpty());
            assertFalse(dto.getExplanation().relevantFeatures().isEmpty());
        }

        @Test
        @DisplayName("Scenario 14b: Popular explanation highlights booking counts and reputation")
        void explanation_popular() {
            List<RecommendationDto> recs = engine.rankPopular(List.of(activeHotelLondon), 5);

            assertEquals(1, recs.size());
            RecommendationDto dto = recs.get(0);
            assertEquals(RecommendationCategory.POPULAR, dto.getCategory());
            assertTrue(dto.getExplanation().primaryReason().contains("50"));
            assertTrue(dto.getExplanation().primaryReason().contains("completed bookings")
                    || dto.getExplanation().primaryReason().contains("total bookings"));
        }

        @Test
        @DisplayName("Scenario 14c: Top-rated explanation highlights rating and verified reviews")
        void explanation_topRated() {
            List<RecommendationDto> recs = engine.rankTopRated(List.of(activeHotelLondon), 5);

            assertEquals(1, recs.size());
            RecommendationDto dto = recs.get(0);
            assertEquals(RecommendationCategory.TOP_RATED, dto.getCategory());
            assertTrue(dto.getExplanation().primaryReason().contains("4.8"));
            assertFalse(dto.getExplanation().supportingReasons().isEmpty());
        }

        @Test
        @DisplayName("Scenario 14d: Similar explanation references source hotel name and shared attributes")
        void explanation_similar() {
            List<RecommendationDto> recs = engine.rankSimilar(activeHotelLondon, List.of(activeHotelParis), 5);

            assertEquals(1, recs.size());
            RecommendationDto dto = recs.get(0);
            assertEquals(RecommendationCategory.SIMILAR, dto.getCategory());
            assertTrue(dto.getExplanation().primaryReason().contains(activeHotelLondon.getName()));
        }
    }

    @Nested
    @DisplayName("Ranking Order and Edge Cases")
    class RankingOrderAndEdgeCasesTests {

        @Test
        @DisplayName("Results are sorted descending by recommendation score and limited properly")
        void ranking_sortsDescendingAndLimits() {
            List<Hotel> pool = List.of(activeHotelTokyo, activeHotelLondon, activeHotelParis);
            UserStayProfile profile = new UserStayProfile(Set.of("London"), Set.of("United Kingdom"), Set.of("Boutique"), 150.0, 1);

            List<RecommendationDto> recs = engine.rankPersonalized(pool, profile, 2);

            assertEquals(2, recs.size());
            // First item should have higher or equal score than second
            assertTrue(recs.get(0).getScore() >= recs.get(1).getScore());
            // London hotel should rank first because of perfect city, type, and price match
            assertEquals("h-1", recs.get(0).getId());
        }

        @Test
        @DisplayName("Handles null and empty inputs across all ranking methods without throwing")
        void ranking_handlesNullAndEmptyCollectionsSafely() {
            assertTrue(engine.rankPersonalized(null, null, 5).isEmpty());
            assertTrue(engine.rankPersonalized(Collections.emptyList(), null, 5).isEmpty());

            assertTrue(engine.rankPopular(null, 5).isEmpty());
            assertTrue(engine.rankPopular(Collections.emptyList(), 5).isEmpty());

            assertTrue(engine.rankTopRated(null, 5).isEmpty());
            assertTrue(engine.rankTopRated(Collections.emptyList(), 5).isEmpty());

            assertTrue(engine.rankSimilar(null, null, 5).isEmpty());
            assertTrue(engine.rankSimilar(activeHotelLondon, null, 5).isEmpty());
            assertTrue(engine.rankSimilar(null, List.of(activeHotelParis), 5).isEmpty());
        }
    }
}

