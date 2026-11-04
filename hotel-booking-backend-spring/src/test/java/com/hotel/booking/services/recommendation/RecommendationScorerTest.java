package com.hotel.booking.services.recommendation;

import com.hotel.booking.entities.Hotel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RecommendationScorer Unit Tests")
class RecommendationScorerTest {

    private RecommendationScorer scorer;

    @BeforeEach
    void setUp() {
        scorer = new RecommendationScorer();
    }

    @Nested
    @DisplayName("1 & 2: Bayesian Rating Tests")
    class BayesianRatingTests {

        @Test
        @DisplayName("Scenario 1: Bayesian rating with multiple reviews calculates accurately")
        void bayesianRating_withMultipleReviews() {
            // v = 10, R = 4.8, m = 3.0, C = 4.0
            // expected = (10 * 4.8 + 3.0 * 4.0) / ((10 + 3.0) * 5.0)
            // = (48.0 + 12.0) / (13.0 * 5.0) = 60.0 / 65.0 = 0.9230769...
            double score = scorer.calculateBayesianRatingScore(10, 4.8);
            assertEquals(60.0 / 65.0, score, 0.0001);
            assertTrue(score >= 0.0 && score <= 1.0);
        }

        @Test
        @DisplayName("Scenario 2: Bayesian rating with zero reviews returns neutral prior 0.80")
        void bayesianRating_withZeroReviews() {
            // v = 0, R = 0.0, m = 3.0, C = 4.0
            // expected = (0 + 12.0) / (3.0 * 5.0) = 12.0 / 15.0 = 0.80
            double score = scorer.calculateBayesianRatingScore(0, 0.0);
            assertEquals(0.80, score, 0.0001);

            // Null checks should also default safely to 0.80
            double nullScore = scorer.calculateBayesianRatingScore(null, null);
            assertEquals(0.80, nullScore, 0.0001);
        }

        @Test
        @DisplayName("Bayesian rating with poor average rating pulls score below prior")
        void bayesianRating_withPoorRating() {
            // v = 10, R = 1.0 => (10 + 12) / 65 = 22 / 65 = 0.33846...
            double score = scorer.calculateBayesianRatingScore(10, 1.0);
            assertEquals(22.0 / 65.0, score, 0.0001);
            assertTrue(score < 0.80);
        }
    }

    @Nested
    @DisplayName("3 & 4: Popularity Scoring Tests")
    class PopularityScoringTests {

        @Test
        @DisplayName("Scenario 3: Popularity normalization uses sublinear logarithmic scaling")
        void popularity_normalizesLogarithmically() {
            // b = 20, maxB = 50
            // expected = ln(21) / ln(52)
            double score = scorer.calculatePopularityScore(20, 50);
            double expected = Math.log(21.0) / Math.log(52.0);
            assertEquals(expected, score, 0.0001);
            assertTrue(score > 0.0 && score < 1.0);
        }

        @Test
        @DisplayName("Scenario 4: Popularity when all booking counts are zero returns 0.0 without division by zero")
        void popularity_whenAllZero_returnsZero() {
            double score = scorer.calculatePopularityScore(0, 0);
            assertEquals(0.0, score, 0.0001);

            double scoreNull = scorer.calculatePopularityScore(null, 0);
            assertEquals(0.0, scoreNull, 0.0001);

            double scoreNegative = scorer.calculatePopularityScore(-5, -10);
            assertEquals(0.0, scoreNegative, 0.0001);
        }

        @Test
        @DisplayName("Popularity for top booked hotel is near 1.0")
        void popularity_forTopBookedHotel() {
            // b = 100, maxB = 100 => ln(101) / ln(102) ~ 0.9978
            double score = scorer.calculatePopularityScore(100, 100);
            double expected = Math.log(101.0) / Math.log(102.0);
            assertEquals(expected, score, 0.0001);
            assertTrue(score > 0.99 && score <= 1.0);
        }
    }

    @Nested
    @DisplayName("5, 6 & 7: Destination Affinity Tests")
    class DestinationAffinityTests {

        private UserStayProfile londonUkProfile;

        @BeforeEach
        void initProfile() {
            londonUkProfile = new UserStayProfile(
                    Set.of("London"),
                    Set.of("United Kingdom"),
                    Set.of("Boutique"),
                    150.0,
                    1
            );
        }

        @Test
        @DisplayName("Scenario 5: Same-city destination match returns 1.0")
        void destination_sameCity_returnsOne() {
            double score = scorer.calculateDestinationAffinity("London", "United Kingdom", londonUkProfile);
            assertEquals(1.0, score, 0.0001);

            // Case-insensitive test
            double caseInsensitive = scorer.calculateDestinationAffinity("london", "united kingdom", londonUkProfile);
            assertEquals(1.0, caseInsensitive, 0.0001);
        }

        @Test
        @DisplayName("Scenario 6: Same-country destination match returns 0.6")
        void destination_sameCountry_returnsPointSix() {
            // City is Edinburgh, but country is United Kingdom
            double score = scorer.calculateDestinationAffinity("Edinburgh", "United Kingdom", londonUkProfile);
            assertEquals(0.6, score, 0.0001);

            double caseInsensitive = scorer.calculateDestinationAffinity("edinburgh", "united kingdom", londonUkProfile);
            assertEquals(0.6, caseInsensitive, 0.0001);
        }

        @Test
        @DisplayName("Scenario 7: No destination match returns 0.0")
        void destination_noMatch_returnsZero() {
            // Tokyo, Japan does not match London, UK
            double score = scorer.calculateDestinationAffinity("Tokyo", "Japan", londonUkProfile);
            assertEquals(0.0, score, 0.0001);

            // Null inputs should return 0.0
            double nullCity = scorer.calculateDestinationAffinity(null, null, londonUkProfile);
            assertEquals(0.0, nullCity, 0.0001);

            // Empty profile should return 0.0
            double emptyProf = scorer.calculateDestinationAffinity("London", "United Kingdom", UserStayProfile.empty());
            assertEquals(0.0, emptyProf, 0.0001);
        }
    }

    @Nested
    @DisplayName("8: Jaccard Type Similarity Tests")
    class JaccardTypeSimilarityTests {

        @Test
        @DisplayName("Scenario 8: Jaccard type similarity computes intersection over union")
        void jaccard_calculatesIntersectionOverUnion() {
            List<String> hotelTypes = List.of("Boutique", "Luxury");
            Set<String> userTypes = Set.of("Boutique", "Resort");

            // intersection = ["boutique"] (size 1)
            // union = ["boutique", "luxury", "resort"] (size 3)
            // expected = 1.0 / 3.0 = 0.3333...
            double score = scorer.calculateTypeAffinity(hotelTypes, userTypes);
            assertEquals(1.0 / 3.0, score, 0.0001);
        }

        @Test
        @DisplayName("Jaccard similarity handles exact type matches")
        void jaccard_exactMatch_returnsOne() {
            List<String> hotelTypes = List.of("Boutique", "Historic");
            Set<String> userTypes = Set.of("boutique", "historic"); // case-insensitive

            double score = scorer.calculateTypeAffinity(hotelTypes, userTypes);
            assertEquals(1.0, score, 0.0001);
        }

        @Test
        @DisplayName("Jaccard similarity with disjoint types returns 0.0")
        void jaccard_disjointTypes_returnsZero() {
            List<String> hotelTypes = List.of("Resort", "Spa");
            Set<String> userTypes = Set.of("City", "Business");

            double score = scorer.calculateTypeAffinity(hotelTypes, userTypes);
            assertEquals(0.0, score, 0.0001);
        }
    }

    @Nested
    @DisplayName("9: Price Compatibility Tests")
    class PriceCompatibilityTests {

        @Test
        @DisplayName("Scenario 9: Price compatibility matches formula max(0, 1 - |P - avg| / max(P, avg))")
        void priceCompatibility_calculatesFormulaAccurately() {
            // Exactly matching spend
            double exact = scorer.calculatePriceCompatibility(150.0, 150.0);
            assertEquals(1.0, exact, 0.0001);

            // Hotel £200, average spend £100
            // diff = 100, max = 200 => 1 - (100 / 200) = 0.5
            double half = scorer.calculatePriceCompatibility(200.0, 100.0);
            assertEquals(0.5, half, 0.0001);

            // Hotel £100, average spend £200
            // diff = 100, max = 200 => 1 - (100 / 200) = 0.5
            double halfReversed = scorer.calculatePriceCompatibility(100.0, 200.0);
            assertEquals(0.5, halfReversed, 0.0001);

            // Extremely divergent price (£500 vs £50): 1 - (450 / 500) = 0.1
            double divergent = scorer.calculatePriceCompatibility(500.0, 50.0);
            assertEquals(0.1, divergent, 0.0001);

            // Zero spend or zero price should return 0.0 safely
            assertEquals(0.0, scorer.calculatePriceCompatibility(0.0, 100.0), 0.0001);
            assertEquals(0.0, scorer.calculatePriceCompatibility(150.0, 0.0), 0.0001);
            assertEquals(0.0, scorer.calculatePriceCompatibility(-50.0, -50.0), 0.0001);
        }
    }

    @Nested
    @DisplayName("10: Personalized Hybrid Scoring Tests")
    class PersonalizedHybridScoringTests {

        @Test
        @DisplayName("Scenario 10: Personalized hybrid scoring combines all 4 components with weights summing to 1.0")
        void personalizedHybrid_calculatesExpectedScore() {
            UserStayProfile profile = new UserStayProfile(
                    Set.of("London"),
                    Set.of("United Kingdom"),
                    Set.of("Boutique"),
                    150.0,
                    2
            );

            Hotel hotel = new Hotel();
            hotel.setCity("London");
            hotel.setCountry("United Kingdom");
            hotel.setType(List.of("Boutique"));
            hotel.setPricePerNight(BigDecimal.valueOf(150.0));
            hotel.setStarRating(5);
            hotel.setFacilities(List.of("WiFi", "Gym", "Spa", "Pool", "Bar", "Restaurant"));
            hotel.setReviewCount(10);
            hotel.setAverageRating(4.8);
            hotel.setTotalBookings(20);

            // Personal components:
            // S_dest = 1.0
            // S_type = 1.0
            // S_price = 1.0
            // S_personal = 0.45 * 1.0 + 0.35 * 1.0 + 0.20 * 1.0 = 1.0
            double sPers = scorer.calculatePersonalScore(hotel, profile);
            assertEquals(1.0, sPers, 0.0001);

            // Content: 0.5 * (5/5) + 0.5 * (6/6) = 1.0
            double sContent = scorer.calculateContentMatchScore(hotel.getStarRating(), hotel.getFacilities().size());
            assertEquals(1.0, sContent, 0.0001);

            // Rating: (10 * 4.8 + 12) / 65 = 60 / 65 ~ 0.9230769
            double sRating = scorer.calculateBayesianRatingScore(hotel.getReviewCount(), hotel.getAverageRating());
            assertEquals(60.0 / 65.0, sRating, 0.0001);

            // Popularity: max = 50 => ln(21) / ln(52) ~ 0.7699
            double sPop = scorer.calculatePopularityScore(20, 50);

            // Expected hybrid:
            // 0.45 * 1.0 + 0.20 * 1.0 + 0.20 * (60/65) + 0.15 * sPop
            double expected = (0.45 * 1.0) + (0.20 * 1.0) + (0.20 * (60.0 / 65.0)) + (0.15 * sPop);

            double actual = scorer.calculateHybridScore(hotel, profile, 50);
            assertEquals(expected, actual, 0.0001);

            // Verify weights sum to 1.0
            assertEquals(1.00, RecommendationScorer.WEIGHT_PERSONAL + RecommendationScorer.WEIGHT_CONTENT
                    + RecommendationScorer.WEIGHT_RATING + RecommendationScorer.WEIGHT_POPULARITY, 0.0001);
        }
    }

    @Nested
    @DisplayName("11: Cold-Start Hybrid Scoring Tests")
    class ColdStartHybridScoringTests {

        @Test
        @DisplayName("Scenario 11: Cold-start hybrid scoring uses 0.30 content, 0.45 rating, 0.25 popularity")
        void coldStartHybrid_calculatesExpectedScore() {
            Hotel hotel = new Hotel();
            hotel.setStarRating(4);
            hotel.setFacilities(List.of("WiFi", "Gym", "Restaurant"));
            hotel.setReviewCount(0); // Zero reviews => prior 0.80
            hotel.setAverageRating(0.0);
            hotel.setTotalBookings(0);

            // S_content = 0.5 * (4/5) + 0.5 * (3/6) = 0.40 + 0.25 = 0.65
            double sContent = scorer.calculateContentMatchScore(4, 3);
            assertEquals(0.65, sContent, 0.0001);

            // S_rating = 0.80
            double sRating = scorer.calculateBayesianRatingScore(0, 0.0);
            assertEquals(0.80, sRating, 0.0001);

            // S_pop = 0.0
            double sPop = scorer.calculatePopularityScore(0, 50);
            assertEquals(0.0, sPop, 0.0001);

            // Cold start weights: 0.30 * 0.65 + 0.45 * 0.80 + 0.25 * 0.0 = 0.195 + 0.36 = 0.555
            double expected = (0.30 * 0.65) + (0.45 * 0.80) + (0.25 * 0.0);
            assertEquals(0.555, expected, 0.0001);

            double actual = scorer.calculateColdStartScore(hotel, 50);
            assertEquals(expected, actual, 0.0001);

            // Passing null profile to calculateHybridScore also invokes cold start
            double actualNull = scorer.calculateHybridScore(hotel, null, 50);
            assertEquals(expected, actualNull, 0.0001);

            // Passing empty profile also invokes cold start
            double actualEmpty = scorer.calculateHybridScore(hotel, UserStayProfile.empty(), 50);
            assertEquals(expected, actualEmpty, 0.0001);

            // Verify cold start weights sum to 1.0
            assertEquals(1.00, RecommendationScorer.COLD_WEIGHT_CONTENT + RecommendationScorer.COLD_WEIGHT_RATING
                    + RecommendationScorer.COLD_WEIGHT_POPULARITY, 0.0001);
        }
    }

    @Nested
    @DisplayName("12: Item-to-Item Similarity Tests")
    class SimilarityTests {

        @Test
        @DisplayName("Scenario 12: Similarity scoring evaluates multi-attribute alignment")
        void similarity_evaluatesMultiAttributeAlignment() {
            Hotel src = new Hotel();
            src.setId("h-source");
            src.setName("Thames Boutique");
            src.setCity("London");
            src.setCountry("United Kingdom");
            src.setType(List.of("Boutique", "Luxury"));
            src.setFacilities(List.of("WiFi", "Gym", "Spa"));
            src.setPricePerNight(BigDecimal.valueOf(150.0));
            src.setStarRating(4);

            Hotel cand = new Hotel();
            cand.setId("h-candidate");
            cand.setName("Mayfair Suites");
            cand.setCity("London"); // Same city => 1.0
            cand.setCountry("United Kingdom");
            cand.setType(List.of("Boutique", "Luxury")); // Jaccard = 1.0
            cand.setFacilities(List.of("WiFi", "Gym", "Spa")); // Jaccard = 1.0
            cand.setPricePerNight(BigDecimal.valueOf(150.0)); // Price diff = 0 => 1.0
            cand.setStarRating(4); // Star diff = 0 => 1.0

            double score = scorer.calculateSimilarityScore(src, cand);
            assertEquals(1.0, score, 0.0001);

            // Same hotel (same ID) must return 0.0 (exclude self)
            double selfScore = scorer.calculateSimilarityScore(src, src);
            assertEquals(0.0, selfScore, 0.0001);

            // Partial similarity
            Hotel differentCity = new Hotel();
            differentCity.setId("h-diff");
            differentCity.setCity("Edinburgh");
            differentCity.setCountry("United Kingdom"); // Same country => 0.6
            differentCity.setType(List.of("Boutique", "Historic")); // overlap 1/3 ~ 0.333
            differentCity.setFacilities(List.of("WiFi")); // overlap 1/3 ~ 0.333
            differentCity.setPricePerNight(BigDecimal.valueOf(200.0)); // 1 - 50/200 = 0.75
            differentCity.setStarRating(3); // 1 - 1/4 = 0.75

            double partScore = scorer.calculateSimilarityScore(src, differentCity);
            assertTrue(partScore > 0.0 && partScore < 1.0);
        }
    }

    @Nested
    @DisplayName("15: Null and Empty Collection Handling Tests")
    class NullAndEmptyHandlingTests {

        @Test
        @DisplayName("Scenario 15: Scorers safely handle null, empty, and boundary inputs without crashing")
        void scorers_handleNullAndEmptyInputsSafely() {
            // Null hotels
            assertEquals(0.0, scorer.calculatePersonalScore(null, null), 0.0001);
            assertEquals(0.0, scorer.calculateHybridScore(null, null, 10), 0.0001);
            assertEquals(0.0, scorer.calculateSimilarityScore(null, null), 0.0001);

            // Jaccard with nulls and empty sets
            assertEquals(0.0, scorer.calculateJaccardSimilarity(null, null), 0.0001);
            assertEquals(0.0, scorer.calculateJaccardSimilarity(Collections.emptyList(), List.of("WiFi")), 0.0001);
            assertEquals(0.0, scorer.calculateJaccardSimilarity(List.of(""), List.of("   ")), 0.0001);

            // Content match with nulls / negative values
            double contentNull = scorer.calculateContentMatchScore(null, null);
            assertEquals(0.0, contentNull, 0.0001);

            // Content match with values exceeding limits clamps safely
            double contentOver = scorer.calculateContentMatchScore(10, 20);
            assertEquals(1.0, contentOver, 0.0001);

            // Price compatibility with negative/zero
            assertEquals(0.0, scorer.calculatePriceCompatibility(-10.0, 50.0), 0.0001);
            assertEquals(0.0, scorer.calculatePriceCompatibility(50.0, -10.0), 0.0001);
        }
    }
}

