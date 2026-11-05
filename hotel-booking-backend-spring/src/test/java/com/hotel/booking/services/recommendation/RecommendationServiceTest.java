package com.hotel.booking.services.recommendation;

import com.hotel.booking.dtos.recommendation.RecommendationCategory;
import com.hotel.booking.dtos.recommendation.RecommendationDto;
import com.hotel.booking.dtos.recommendation.RecommendationExplanationDto;
import com.hotel.booking.entities.Booking;
import com.hotel.booking.entities.Hotel;
import com.hotel.booking.entities.User;
import com.hotel.booking.repositories.BookingRepository;
import com.hotel.booking.repositories.HotelRepository;
import com.hotel.booking.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecommendationService Unit Tests")
class RecommendationServiceTest {

    @Mock
    private RecommendationEngine recommendationEngine;

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    private RecommendationService service;
    private Hotel hotelLondon;
    private Hotel hotelParis;

    @BeforeEach
    void setUp() {
        service = new RecommendationService(
                recommendationEngine,
                hotelRepository,
                bookingRepository,
                userRepository
        );

        hotelLondon = new Hotel();
        hotelLondon.setId("hotel-london");
        hotelLondon.setName("London Boutique");
        hotelLondon.setCity("London");
        hotelLondon.setIsActive(true);

        hotelParis = new Hotel();
        hotelParis.setId("hotel-paris");
        hotelParis.setName("Paris Retreat");
        hotelParis.setCity("Paris");
        hotelParis.setIsActive(true);
    }

    @Test
    @DisplayName("getPersonalizedRecommendations delegates to engine with user profile")
    void getPersonalized_withValidUser_delegatesToEngine() {
        User user = new User();
        user.setId("user-1");
        when(hotelRepository.findAll()).thenReturn(List.of(hotelLondon, hotelParis));
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(bookingRepository.findByUserIdOrderByCreatedAtDesc("user-1")).thenReturn(Collections.emptyList());

        RecommendationDto dto = new RecommendationDto();
        dto.setId("hotel-london");
        dto.setScore(0.9);
        dto.setCategory(RecommendationCategory.PERSONALIZED);
        when(recommendationEngine.rankPersonalized(anyList(), any(UserStayProfile.class), eq(5)))
                .thenReturn(List.of(dto));

        List<RecommendationDto> results = service.getPersonalizedRecommendations("user-1", 5);

        assertEquals(1, results.size());
        assertEquals("hotel-london", results.get(0).getId());
        verify(recommendationEngine).rankPersonalized(anyList(), any(UserStayProfile.class), eq(5));
    }

    @Test
    @DisplayName("getPersonalizedRecommendations returns empty list when hotel catalog is empty")
    void getPersonalized_whenCatalogEmpty_returnsEmptyList() {
        when(hotelRepository.findAll()).thenReturn(Collections.emptyList());

        List<RecommendationDto> results = service.getPersonalizedRecommendations("user-1", 5);

        assertTrue(results.isEmpty());
        verifyNoInteractions(recommendationEngine);
    }

    @Test
    @DisplayName("getPopularRecommendations delegates to engine")
    void getPopular_delegatesToEngine() {
        when(hotelRepository.findAll()).thenReturn(List.of(hotelLondon));
        RecommendationDto dto = new RecommendationDto();
        dto.setId("hotel-london");
        dto.setCategory(RecommendationCategory.POPULAR);
        when(recommendationEngine.rankPopular(anyList(), eq(10))).thenReturn(List.of(dto));

        List<RecommendationDto> results = service.getPopularRecommendations(10);

        assertEquals(1, results.size());
        assertEquals(RecommendationCategory.POPULAR, results.get(0).getCategory());
        verify(recommendationEngine).rankPopular(anyList(), eq(10));
    }

    @Test
    @DisplayName("getTopRatedRecommendations delegates to engine")
    void getTopRated_delegatesToEngine() {
        when(hotelRepository.findAll()).thenReturn(List.of(hotelParis));
        RecommendationDto dto = new RecommendationDto();
        dto.setId("hotel-paris");
        dto.setCategory(RecommendationCategory.TOP_RATED);
        when(recommendationEngine.rankTopRated(anyList(), eq(5))).thenReturn(List.of(dto));

        List<RecommendationDto> results = service.getTopRatedRecommendations(5);

        assertEquals(1, results.size());
        assertEquals(RecommendationCategory.TOP_RATED, results.get(0).getCategory());
        verify(recommendationEngine).rankTopRated(anyList(), eq(5));
    }

    @Test
    @DisplayName("getSimilarRecommendations throws 404 when hotel does not exist")
    void getSimilar_whenHotelNotFound_throws404() {
        when(hotelRepository.findById("unknown-id")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.getSimilarRecommendations("unknown-id", 5)
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Hotel not found with id: unknown-id"));
    }

    @Test
    @DisplayName("getSimilarRecommendations throws 400 when hotel ID is blank")
    void getSimilar_whenHotelIdBlank_throws400() {
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.getSimilarRecommendations("   ", 5)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    @DisplayName("getSimilarRecommendations delegates to engine when hotel exists")
    void getSimilar_whenHotelExists_delegatesToEngine() {
        when(hotelRepository.findById("hotel-london")).thenReturn(Optional.of(hotelLondon));
        when(hotelRepository.findAll()).thenReturn(List.of(hotelLondon, hotelParis));

        RecommendationDto dto = new RecommendationDto();
        dto.setId("hotel-paris");
        dto.setCategory(RecommendationCategory.SIMILAR);
        when(recommendationEngine.rankSimilar(eq(hotelLondon), anyList(), eq(5)))
                .thenReturn(List.of(dto));

        List<RecommendationDto> results = service.getSimilarRecommendations("hotel-london", 5);

        assertEquals(1, results.size());
        assertEquals("hotel-paris", results.get(0).getId());
        verify(recommendationEngine).rankSimilar(eq(hotelLondon), anyList(), eq(5));
    }
}

