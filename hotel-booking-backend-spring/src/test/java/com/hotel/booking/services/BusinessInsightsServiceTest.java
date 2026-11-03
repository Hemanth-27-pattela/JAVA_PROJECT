package com.hotel.booking.services;

import com.hotel.booking.dtos.AnalyticsSnapshotDto;
import com.hotel.booking.dtos.businessinsights.BusinessInsightsDashboardDto;
import com.hotel.booking.dtos.businessinsights.BusinessInsightsForecastDto;
import com.hotel.booking.dtos.businessinsights.SystemStatsDto;
import com.hotel.booking.entities.Booking;
import com.hotel.booking.entities.Hotel;
import com.hotel.booking.entities.Review;
import com.hotel.booking.entities.User;
import com.hotel.booking.entities.embeddables.ReviewCategories;
import com.hotel.booking.entities.enums.BookingStatus;
import com.hotel.booking.entities.enums.PaymentStatus;
import com.hotel.booking.entities.enums.UserRole;
import com.hotel.booking.repositories.AnalyticsSnapshotRepository;
import com.hotel.booking.repositories.BookingRepository;
import com.hotel.booking.repositories.HotelRepository;
import com.hotel.booking.repositories.ReviewRepository;
import com.hotel.booking.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class BusinessInsightsServiceTest {

    @Autowired
    private BusinessInsightsService businessInsightsService;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private AnalyticsSnapshotRepository analyticsSnapshotRepository;

    private User testUser;
    private Hotel testHotel;
    private Booking testBooking;

    @BeforeEach
    void setUp() {
        analyticsSnapshotRepository.deleteAll();
        reviewRepository.deleteAll();
        bookingRepository.deleteAll();
        hotelRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setEmail("insights_user@test.com");
        testUser.setPassword("password123");
        testUser.setFirstName("Alice");
        testUser.setLastName("Wonder");
        testUser.setRole(UserRole.USER);
        testUser = userRepository.save(testUser);

        testHotel = new Hotel();
        testHotel.setUser(testUser);
        testHotel.setName("Insights Grand Palace");
        testHotel.setCity("Edinburgh");
        testHotel.setCountry("United Kingdom");
        testHotel.setDescription("Palatial stay");
        testHotel.setType(new ArrayList<>(List.of("Hotel", "Boutique")));
        testHotel.setPricePerNight(new BigDecimal("200.00"));
        testHotel.setStarRating(5);
        testHotel.setAdultCount(2);
        testHotel.setChildCount(1);
        testHotel.setImageUrls(new ArrayList<>(List.of("https://example.com/h.jpg")));
        testHotel.setFacilities(new ArrayList<>(List.of("WiFi", "Spa")));
        testHotel.setIsActive(true);
        testHotel = hotelRepository.save(testHotel);

        Instant now = Instant.now();
        testBooking = new Booking();
        testBooking.setUser(testUser);
        testBooking.setHotel(testHotel);
        testBooking.setFirstName("Alice");
        testBooking.setLastName("Wonder");
        testBooking.setEmail(testUser.getEmail());
        testBooking.setCheckIn(now.minus(4, ChronoUnit.DAYS));
        testBooking.setCheckOut(now.minus(1, ChronoUnit.DAYS));
        testBooking.setAdultCount(2);
        testBooking.setChildCount(1);
        testBooking.setTotalCost(new BigDecimal("600.00"));
        testBooking.setStatus(BookingStatus.CONFIRMED);
        testBooking.setPaymentStatus(PaymentStatus.PAID);
        testBooking = bookingRepository.save(testBooking);

        Review review = new Review();
        review.setUser(testUser);
        review.setHotel(testHotel);
        review.setBooking(testBooking);
        review.setRating(5);
        review.setComment("Incredible experience!");
        review.setCategories(new ReviewCategories(5, 5, 5, 5, 5));
        review.setIsVerified(true);
        reviewRepository.save(review);
    }

    @Test
    @DisplayName("getDashboardData accurately aggregates overview, destinations, performance, and guest mix")
    void getDashboardData_accurateAggregations() {
        BusinessInsightsDashboardDto dashboard = businessInsightsService.getDashboardData();

        assertNotNull(dashboard);
        assertEquals(1, dashboard.getOverview().getTotalHotels());
        assertEquals(1, dashboard.getOverview().getTotalUsers());
        assertEquals(1, dashboard.getOverview().getTotalBookings());
        assertEquals(600.0, dashboard.getOverview().getTotalRevenue());
        assertEquals(1, dashboard.getOverview().getConfirmedBookings());
        assertEquals(0, dashboard.getOverview().getCancelledBookings());
        assertEquals(0.0, dashboard.getOverview().getCancellationRate());
        assertEquals(1, dashboard.getOverview().getTotalReviews());
        assertEquals(5.0, dashboard.getOverview().getAvgReviewRating());
        assertEquals(3.0, dashboard.getOverview().getAvgLos()); // 3 nights
        assertEquals(200.0, dashboard.getOverview().getAdr()); // 600 / 3 = 200
        assertEquals(3.0, dashboard.getOverview().getAvgPartySize()); // 2 adults + 1 child

        // Popular destinations
        assertFalse(dashboard.getPopularDestinations().isEmpty());
        assertEquals("Edinburgh", dashboard.getPopularDestinations().get(0).getId());
        assertEquals(1, dashboard.getPopularDestinations().get(0).getCount());

        // Hotel performance
        assertFalse(dashboard.getHotelPerformance().isEmpty());
        assertEquals("Insights Grand Palace", dashboard.getHotelPerformance().get(0).getName());

        // Guest mix
        assertEquals(2, dashboard.getGuestMix().getAdults());
        assertEquals(1, dashboard.getGuestMix().getChildren());

        // Star distribution
        assertFalse(dashboard.getHotelsByStar().isEmpty());
    }

    @Test
    @DisplayName("getForecastData generates 4-week projections and trend flags")
    void getForecastData_generatesProjections() {
        BusinessInsightsForecastDto forecast = businessInsightsService.getForecastData();

        assertNotNull(forecast);
        assertNotNull(forecast.getForecasts());
        assertEquals(4, forecast.getForecasts().size());

        for (int i = 0; i < 4; i++) {
            assertNotNull(forecast.getForecasts().get(i).getWeek());
            assertTrue(forecast.getForecasts().get(i).getConfidence() >= 0.6);
        }

        assertNotNull(forecast.getTrends());
        assertNotNull(forecast.getTrends().getBookingTrend());
        assertNotNull(forecast.getTrends().getRevenueTrend());
    }

    @Test
    @DisplayName("getSystemStats omits system telemetry when public and includes when authenticated")
    void getSystemStats_privacySeparation() {
        // Public call (includeSystemTelemetry = false)
        SystemStatsDto publicStats = businessInsightsService.getSystemStats(false);
        assertNotNull(publicStats.getDatabase());
        assertEquals(1, publicStats.getDatabase().getTotalHotels());
        assertEquals(1, publicStats.getDatabase().getTotalBookings());
        assertNotNull(publicStats.getApplication());
        assertNull(publicStats.getSystem()); // CWE-200 prevention: no memory telemetry exposed publicly

        // Authenticated call (includeSystemTelemetry = true)
        SystemStatsDto authStats = businessInsightsService.getSystemStats(true);
        assertNotNull(authStats.getDatabase());
        assertNotNull(authStats.getSystem());
        assertNotNull(authStats.getSystem().getMemory());
        assertTrue(authStats.getSystem().getMemory().getTotal() > 0);
    }

    @Test
    @DisplayName("createRollup and getRollups capture and retrieve point-in-time snapshots")
    void rollups_captureAndRetrieve() {
        AnalyticsSnapshotDto captured = businessInsightsService.createRollup();

        assertNotNull(captured.getId());
        assertNotNull(captured.getDate());
        assertEquals(1, captured.getMetrics().getTotalBookings());
        assertEquals(new BigDecimal("600.00"), captured.getMetrics().getTotalRevenue());

        List<AnalyticsSnapshotDto> rollups = businessInsightsService.getRollups(10);
        assertFalse(rollups.isEmpty());
        assertEquals(captured.getId(), rollups.get(0).getId());
    }
}

