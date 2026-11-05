package com.hotel.booking.services.recommendation;

import com.hotel.booking.dtos.recommendation.RecommendationDto;
import com.hotel.booking.entities.Booking;
import com.hotel.booking.entities.Hotel;
import com.hotel.booking.entities.User;
import com.hotel.booking.repositories.BookingRepository;
import com.hotel.booking.repositories.HotelRepository;
import com.hotel.booking.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Service orchestrating recommendation queries across the database and recommendation engine.
 */
@Service
@Transactional(readOnly = true)
public class RecommendationService {

    private final RecommendationEngine recommendationEngine;
    private final HotelRepository hotelRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    public RecommendationService(
            RecommendationEngine recommendationEngine,
            HotelRepository hotelRepository,
            BookingRepository bookingRepository,
            UserRepository userRepository
    ) {
        this.recommendationEngine = recommendationEngine;
        this.hotelRepository = hotelRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
    }

    /**
     * Generates personalized recommendations for an authenticated guest based on
     * their confirmed/completed booking history and saved preferences.
     */
    public List<RecommendationDto> getPersonalizedRecommendations(String userId, int limit) {
        List<Hotel> allHotels = hotelRepository.findAll();
        if (allHotels.isEmpty()) {
            return Collections.emptyList();
        }

        UserStayProfile profile = UserStayProfile.empty();
        if (userId != null && !userId.isBlank()) {
            Optional<User> userOpt = userRepository.findById(userId);
            List<Booking> userBookings = bookingRepository.findByUserIdOrderByCreatedAtDesc(userId);
            if (userOpt.isPresent()) {
                profile = UserStayProfile.fromBookingsAndPreferences(userBookings, userOpt.get().getPreferences());
            } else {
                profile = UserStayProfile.fromBookings(userBookings);
            }
        }

        return recommendationEngine.rankPersonalized(allHotels, profile, limit);
    }

    /**
     * Generates popularity-ranked recommendations across active hotels.
     */
    public List<RecommendationDto> getPopularRecommendations(int limit) {
        List<Hotel> allHotels = hotelRepository.findAll();
        if (allHotels.isEmpty()) {
            return Collections.emptyList();
        }
        return recommendationEngine.rankPopular(allHotels, limit);
    }

    /**
     * Generates Bayesian top-rated recommendations across active hotels.
     */
    public List<RecommendationDto> getTopRatedRecommendations(int limit) {
        List<Hotel> allHotels = hotelRepository.findAll();
        if (allHotels.isEmpty()) {
            return Collections.emptyList();
        }
        return recommendationEngine.rankTopRated(allHotels, limit);
    }

    /**
     * Generates item-to-item similar recommendations for a specific hotel.
     * Returns 404 NOT_FOUND if the source hotel does not exist.
     */
    public List<RecommendationDto> getSimilarRecommendations(String hotelId, int limit) {
        if (hotelId == null || hotelId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hotel ID cannot be blank");
        }

        Hotel sourceHotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hotel not found with id: " + hotelId));

        List<Hotel> allHotels = hotelRepository.findAll();
        return recommendationEngine.rankSimilar(sourceHotel, allHotels, limit);
    }
}

