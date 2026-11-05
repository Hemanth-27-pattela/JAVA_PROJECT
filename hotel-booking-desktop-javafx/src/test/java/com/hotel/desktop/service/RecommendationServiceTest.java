package com.hotel.desktop.service;

import com.hotel.desktop.model.RecommendationCategory;
import com.hotel.desktop.model.RecommendationDto;
import com.hotel.desktop.model.RecommendationExplanation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RecommendationServiceTest {

    private ApiClient mockApiClient;
    private RecommendationService recommendationService;

    @BeforeEach
    void setUp() {
        mockApiClient = mock(ApiClient.class);
        recommendationService = new RecommendationService(mockApiClient);
    }

    @Test
    @DisplayName("getPersonalizedRecommendations calls endpoint with auth=true and limit parameter")
    void testGetPersonalizedRecommendationsSuccess() throws Exception {
        RecommendationDto dto1 = createSampleDto("hotel-1", "Seaside Resort", RecommendationCategory.PERSONALIZED);
        RecommendationDto dto2 = createSampleDto("hotel-2", "Mountain View", RecommendationCategory.PERSONALIZED);
        RecommendationDto[] responseArray = new RecommendationDto[]{dto1, dto2};

        when(mockApiClient.get(eq("/api/recommendations/personalized?limit=5"), eq(RecommendationDto[].class), eq(true)))
                .thenReturn(responseArray);

        List<RecommendationDto> result = recommendationService.getPersonalizedRecommendations(5);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("hotel-1", result.get(0).getId());
        assertEquals("hotel-2", result.get(1).getId());
        verify(mockApiClient, times(1)).get("/api/recommendations/personalized?limit=5", RecommendationDto[].class, true);
    }

    @Test
    @DisplayName("getPersonalizedRecommendations defaults to limit=6 when negative or zero limit is passed")
    void testGetPersonalizedRecommendationsDefaultLimit() throws Exception {
        when(mockApiClient.get(eq("/api/recommendations/personalized?limit=6"), eq(RecommendationDto[].class), eq(true)))
                .thenReturn(new RecommendationDto[0]);

        List<RecommendationDto> result = recommendationService.getPersonalizedRecommendations(0);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(mockApiClient, times(1)).get("/api/recommendations/personalized?limit=6", RecommendationDto[].class, true);
    }

    @Test
    @DisplayName("getPopularRecommendations calls public popular endpoint with auth=false")
    void testGetPopularRecommendationsSuccess() throws Exception {
        RecommendationDto dto = createSampleDto("hotel-pop-1", "Central London Plaza", RecommendationCategory.POPULAR);
        when(mockApiClient.get(eq("/api/recommendations/popular?limit=6"), eq(RecommendationDto[].class), eq(false)))
                .thenReturn(new RecommendationDto[]{dto});

        List<RecommendationDto> result = recommendationService.getPopularRecommendations(6);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(RecommendationCategory.POPULAR, result.get(0).getCategory());
        verify(mockApiClient, times(1)).get("/api/recommendations/popular?limit=6", RecommendationDto[].class, false);
    }

    @Test
    @DisplayName("getTopRatedRecommendations calls public top-rated endpoint with auth=false")
    void testGetTopRatedRecommendationsSuccess() throws Exception {
        RecommendationDto dto = createSampleDto("hotel-top-1", "The Kyoto Sanctuary", RecommendationCategory.TOP_RATED);
        when(mockApiClient.get(eq("/api/recommendations/top-rated?limit=4"), eq(RecommendationDto[].class), eq(false)))
                .thenReturn(new RecommendationDto[]{dto});

        List<RecommendationDto> result = recommendationService.getTopRatedRecommendations(4);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(RecommendationCategory.TOP_RATED, result.get(0).getCategory());
        verify(mockApiClient, times(1)).get("/api/recommendations/top-rated?limit=4", RecommendationDto[].class, false);
    }

    @Test
    @DisplayName("getSimilarRecommendations calls public similar endpoint with validated hotelId")
    void testGetSimilarRecommendationsSuccess() throws Exception {
        RecommendationDto dto = createSampleDto("hotel-sim-1", "Tokyo Ryokan", RecommendationCategory.SIMILAR);
        when(mockApiClient.get(eq("/api/recommendations/similar/ref-hotel-456?limit=3"), eq(RecommendationDto[].class), eq(false)))
                .thenReturn(new RecommendationDto[]{dto});

        List<RecommendationDto> result = recommendationService.getSimilarRecommendations("ref-hotel-456", 3);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Tokyo Ryokan", result.get(0).getName());
        verify(mockApiClient, times(1)).get("/api/recommendations/similar/ref-hotel-456?limit=3", RecommendationDto[].class, false);
    }

    @Test
    @DisplayName("getSimilarRecommendations throws ApiException when hotelId is empty or null")
    void testGetSimilarRecommendationsValidation() {
        ApiException ex1 = assertThrows(ApiException.class, () -> recommendationService.getSimilarRecommendations(null, 4));
        assertEquals(400, ex1.getStatusCode());

        ApiException ex2 = assertThrows(ApiException.class, () -> recommendationService.getSimilarRecommendations("   ", 4));
        assertEquals(400, ex2.getStatusCode());

        verifyNoInteractions(mockApiClient);
    }

    @Test
    @DisplayName("Service gracefully returns empty list when backend returns null")
    void testNullResponseHandling() throws Exception {
        when(mockApiClient.get(anyString(), eq(RecommendationDto[].class), anyBoolean()))
                .thenReturn(null);

        List<RecommendationDto> res1 = recommendationService.getPersonalizedRecommendations(6);
        assertNotNull(res1);
        assertTrue(res1.isEmpty());

        List<RecommendationDto> res2 = recommendationService.getPopularRecommendations(6);
        assertNotNull(res2);
        assertTrue(res2.isEmpty());

        List<RecommendationDto> res3 = recommendationService.getTopRatedRecommendations(6);
        assertNotNull(res3);
        assertTrue(res3.isEmpty());
    }

    @Test
    @DisplayName("Service propagates ApiException from ApiClient on unauthorized or error")
    void testApiExceptionPropagation() throws Exception {
        when(mockApiClient.get(anyString(), eq(RecommendationDto[].class), anyBoolean()))
                .thenThrow(new ApiException(401, "Session expired or invalid token"));

        ApiException thrown = assertThrows(ApiException.class, () -> recommendationService.getPersonalizedRecommendations(6));
        assertEquals(401, thrown.getStatusCode());
        assertEquals("Session expired or invalid token", thrown.getMessage());
    }

    private RecommendationDto createSampleDto(String id, String name, RecommendationCategory category) {
        RecommendationDto dto = new RecommendationDto();
        dto.setId(id);
        dto.setName(name);
        dto.setCity("London");
        dto.setCountry("United Kingdom");
        dto.setPricePerNight(BigDecimal.valueOf(175));
        dto.setStarRating(4);
        dto.setAverageRating(4.7);
        dto.setReviewCount(95);
        dto.setScore(0.89);
        dto.setCategory(category);
        dto.setExplanation(new RecommendationExplanation(
                "Popular choice among guests visiting London",
                List.of("Competitive pricing", "Central location"),
                List.of("Free WiFi", "Breakfast")
        ));
        return dto;
    }
}

