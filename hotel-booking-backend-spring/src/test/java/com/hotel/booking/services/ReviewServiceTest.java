package com.hotel.booking.services;

import com.hotel.booking.dtos.CreateReviewRequest;
import com.hotel.booking.dtos.ReviewCategoriesDto;
import com.hotel.booking.dtos.ReviewDto;
import com.hotel.booking.dtos.ReviewSummaryDto;
import com.hotel.booking.entities.Booking;
import com.hotel.booking.entities.Hotel;
import com.hotel.booking.entities.User;
import com.hotel.booking.entities.enums.BookingStatus;
import com.hotel.booking.entities.enums.PaymentStatus;
import com.hotel.booking.entities.enums.UserRole;
import com.hotel.booking.repositories.BookingRepository;
import com.hotel.booking.repositories.HotelRepository;
import com.hotel.booking.repositories.ReviewRepository;
import com.hotel.booking.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
public class ReviewServiceTest {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private User otherUser;
    private Hotel testHotel;
    private Hotel otherHotel;
    private Booking paidBooking;

    @BeforeEach
    void setUp() {
        reviewRepository.deleteAll();
        bookingRepository.deleteAll();
        hotelRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setEmail("guest@example.com");
        testUser.setPassword("password123");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setRole(UserRole.USER);
        testUser = userRepository.save(testUser);

        otherUser = new User();
        otherUser.setEmail("other@example.com");
        otherUser.setPassword("password123");
        otherUser.setFirstName("Jane");
        otherUser.setLastName("Smith");
        otherUser.setRole(UserRole.USER);
        otherUser = userRepository.save(otherUser);

        testHotel = new Hotel();
        testHotel.setUser(testUser);
        testHotel.setName("Grand Palace Hotel");
        testHotel.setCity("London");
        testHotel.setCountry("UK");
        testHotel.setDescription("Luxury hotel");
        testHotel.setType(new ArrayList<>(List.of("Hotel")));
        testHotel.setPricePerNight(new BigDecimal("120.00"));
        testHotel.setStarRating(5);
        testHotel.setAdultCount(2);
        testHotel.setChildCount(1);
        testHotel.setImageUrls(new ArrayList<>(List.of("https://example.com/h1.jpg")));
        testHotel.setFacilities(new ArrayList<>(List.of("WiFi", "Pool")));
        testHotel.setIsActive(true);
        testHotel.setAverageRating(0.0);
        testHotel.setReviewCount(0);
        testHotel = hotelRepository.save(testHotel);

        otherHotel = new Hotel();
        otherHotel.setUser(otherUser);
        otherHotel.setName("Seaside Resort");
        otherHotel.setCity("Brighton");
        otherHotel.setCountry("UK");
        otherHotel.setDescription("Beach resort");
        otherHotel.setType(new ArrayList<>(List.of("Resort")));
        otherHotel.setPricePerNight(new BigDecimal("150.00"));
        otherHotel.setStarRating(4);
        otherHotel.setAdultCount(2);
        otherHotel.setChildCount(0);
        otherHotel.setImageUrls(new ArrayList<>(List.of("https://example.com/h2.jpg")));
        otherHotel.setFacilities(new ArrayList<>(List.of("WiFi", "Beach access")));
        otherHotel.setIsActive(true);
        otherHotel = hotelRepository.save(otherHotel);

        Instant now = Instant.now();
        paidBooking = new Booking();
        paidBooking.setUser(testUser);
        paidBooking.setHotel(testHotel);
        paidBooking.setFirstName("John");
        paidBooking.setLastName("Doe");
        paidBooking.setEmail(testUser.getEmail());
        paidBooking.setCheckIn(now.minus(5, ChronoUnit.DAYS));
        paidBooking.setCheckOut(now.minus(2, ChronoUnit.DAYS));
        paidBooking.setAdultCount(2);
        paidBooking.setChildCount(0);
        paidBooking.setTotalCost(new BigDecimal("360.00"));
        paidBooking.setStatus(BookingStatus.CONFIRMED);
        paidBooking.setPaymentStatus(PaymentStatus.PAID);
        paidBooking = bookingRepository.save(paidBooking);
    }

    @Test
    @DisplayName("Successfully creates review for paid booking and updates hotel average/count")
    void createReview_success_updatesHotelAverageAndCount() {
        ReviewCategoriesDto categories = new ReviewCategoriesDto(5, 4, 5, 4, 5);
        CreateReviewRequest request = new CreateReviewRequest(
                testHotel.getId(),
                paidBooking.getId(),
                5,
                "Amazing stay! Beautiful rooms and great location.",
                categories
        );

        ReviewDto created = reviewService.createReview(request, testUser.getId());

        assertNotNull(created.getId());
        assertEquals(testHotel.getId(), created.getHotelId());
        assertEquals(testUser.getId(), created.getUserId());
        assertEquals(paidBooking.getId(), created.getBookingId());
        assertEquals(5, created.getRating());
        assertEquals("Amazing stay! Beautiful rooms and great location.", created.getComment());
        assertTrue(created.getIsVerified());
        assertNotNull(created.getCreatedAt());

        // Check hotel average and review count updated in database
        Hotel updatedHotel = hotelRepository.findById(testHotel.getId()).orElseThrow();
        assertEquals(1, updatedHotel.getReviewCount());
        assertEquals(5.0, updatedHotel.getAverageRating());
    }

    @Test
    @DisplayName("Review for unpaid booking sets isVerified to false")
    void createReview_unpaidBooking_notVerified() {
        Instant now = Instant.now();
        Booking unpaidBooking = new Booking();
        unpaidBooking.setUser(testUser);
        unpaidBooking.setHotel(testHotel);
        unpaidBooking.setFirstName("John");
        unpaidBooking.setLastName("Doe");
        unpaidBooking.setEmail(testUser.getEmail());
        unpaidBooking.setCheckIn(now.minus(4, ChronoUnit.DAYS));
        unpaidBooking.setCheckOut(now.minus(1, ChronoUnit.DAYS));
        unpaidBooking.setAdultCount(1);
        unpaidBooking.setChildCount(0);
        unpaidBooking.setTotalCost(new BigDecimal("120.00"));
        unpaidBooking.setStatus(BookingStatus.CONFIRMED);
        unpaidBooking.setPaymentStatus(PaymentStatus.PENDING);
        unpaidBooking = bookingRepository.save(unpaidBooking);

        CreateReviewRequest request = new CreateReviewRequest(
                testHotel.getId(),
                unpaidBooking.getId(),
                4,
                "Good experience overall.",
                new ReviewCategoriesDto(4, 4, 4, 4, 4)
        );

        ReviewDto created = reviewService.createReview(request, testUser.getId());
        assertFalse(created.getIsVerified());
    }

    @Test
    @DisplayName("Multiple reviews calculate rounded average rating correctly (HALF_UP, 1 decimal)")
    void createReview_multipleReviews_averageRatingRoundedCorrectly() {
        // Review 1: Rating 4
        CreateReviewRequest req1 = new CreateReviewRequest(
                testHotel.getId(),
                paidBooking.getId(),
                4,
                "Great stay, quiet room.",
                new ReviewCategoriesDto(4, 4, 4, 4, 4)
        );
        reviewService.createReview(req1, testUser.getId());

        // Create booking 2 for otherUser
        Instant now = Instant.now();
        Booking booking2 = new Booking();
        booking2.setUser(otherUser);
        booking2.setHotel(testHotel);
        booking2.setFirstName("Jane");
        booking2.setLastName("Smith");
        booking2.setEmail(otherUser.getEmail());
        booking2.setCheckIn(now.minus(3, ChronoUnit.DAYS));
        booking2.setCheckOut(now.minus(1, ChronoUnit.DAYS));
        booking2.setAdultCount(1);
        booking2.setTotalCost(new BigDecimal("240.00"));
        booking2.setStatus(BookingStatus.CONFIRMED);
        booking2.setPaymentStatus(PaymentStatus.PAID);
        booking2 = bookingRepository.save(booking2);

        // Review 2: Rating 5 -> (4 + 5) / 2 = 4.5
        CreateReviewRequest req2 = new CreateReviewRequest(
                testHotel.getId(),
                booking2.getId(),
                5,
                "Exceptional service!",
                new ReviewCategoriesDto(5, 5, 5, 5, 5)
        );
        reviewService.createReview(req2, otherUser.getId());

        Hotel hotelAfterTwo = hotelRepository.findById(testHotel.getId()).orElseThrow();
        assertEquals(2, hotelAfterTwo.getReviewCount());
        assertEquals(4.5, hotelAfterTwo.getAverageRating());

        // Create booking 3 for testUser
        Booking booking3 = new Booking();
        booking3.setUser(testUser);
        booking3.setHotel(testHotel);
        booking3.setFirstName("John");
        booking3.setLastName("Doe");
        booking3.setEmail(testUser.getEmail());
        booking3.setCheckIn(now.minus(10, ChronoUnit.DAYS));
        booking3.setCheckOut(now.minus(8, ChronoUnit.DAYS));
        booking3.setAdultCount(1);
        booking3.setTotalCost(new BigDecimal("240.00"));
        booking3.setStatus(BookingStatus.CONFIRMED);
        booking3.setPaymentStatus(PaymentStatus.PAID);
        booking3 = bookingRepository.save(booking3);

        // Review 3: Rating 4 -> (4 + 5 + 4) / 3 = 4.333... -> rounded to 4.3
        CreateReviewRequest req3 = new CreateReviewRequest(
                testHotel.getId(),
                booking3.getId(),
                4,
                "Consistently good hotel.",
                new ReviewCategoriesDto(4, 4, 4, 4, 4)
        );
        reviewService.createReview(req3, testUser.getId());

        Hotel hotelAfterThree = hotelRepository.findById(testHotel.getId()).orElseThrow();
        assertEquals(3, hotelAfterThree.getReviewCount());
        assertEquals(4.3, hotelAfterThree.getAverageRating());
    }

    @Test
    @DisplayName("Fails with 404 when hotel does not exist")
    void createReview_hotelNotFound_throws404() {
        CreateReviewRequest request = new CreateReviewRequest(
                "non-existent-hotel-id",
                paidBooking.getId(),
                5,
                "Great hotel!",
                new ReviewCategoriesDto(5, 5, 5, 5, 5)
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> reviewService.createReview(request, testUser.getId()));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Hotel not found", ex.getReason());
    }

    @Test
    @DisplayName("Fails with 403 when booking belongs to another user")
    void createReview_bookingBelongsToOtherUser_throws403() {
        CreateReviewRequest request = new CreateReviewRequest(
                testHotel.getId(),
                paidBooking.getId(),
                5,
                "Trying to review someone else's booking",
                new ReviewCategoriesDto(5, 5, 5, 5, 5)
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> reviewService.createReview(request, otherUser.getId()));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        assertEquals("Booking not found for this user/hotel", ex.getReason());
    }

    @Test
    @DisplayName("Fails with 403 when booking does not match requested hotel")
    void createReview_bookingHotelMismatch_throws403() {
        CreateReviewRequest request = new CreateReviewRequest(
                otherHotel.getId(), // different hotel
                paidBooking.getId(),
                5,
                "Mismatching hotel and booking",
                new ReviewCategoriesDto(5, 5, 5, 5, 5)
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> reviewService.createReview(request, testUser.getId()));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        assertEquals("Booking not found for this user/hotel", ex.getReason());
    }

    @Test
    @DisplayName("Fails with 409 when review already exists for booking")
    void createReview_duplicateReview_throws409() {
        CreateReviewRequest request = new CreateReviewRequest(
                testHotel.getId(),
                paidBooking.getId(),
                5,
                "First review for booking",
                new ReviewCategoriesDto(5, 5, 5, 5, 5)
        );
        reviewService.createReview(request, testUser.getId());

        CreateReviewRequest duplicateRequest = new CreateReviewRequest(
                testHotel.getId(),
                paidBooking.getId(),
                4,
                "Second review for same booking",
                new ReviewCategoriesDto(4, 4, 4, 4, 4)
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> reviewService.createReview(duplicateRequest, testUser.getId()));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertEquals("Review already exists for this booking", ex.getReason());
    }

    @Test
    @DisplayName("getReviewsByHotelId returns newest reviews first")
    void getReviewsByHotelId_returnsNewestFirst() {
        Instant now = Instant.now();
        Booking booking2 = new Booking();
        booking2.setUser(otherUser);
        booking2.setHotel(testHotel);
        booking2.setFirstName("Jane");
        booking2.setLastName("Smith");
        booking2.setEmail(otherUser.getEmail());
        booking2.setCheckIn(now.minus(4, ChronoUnit.DAYS));
        booking2.setCheckOut(now.minus(2, ChronoUnit.DAYS));
        booking2.setAdultCount(1);
        booking2.setTotalCost(new BigDecimal("240.00"));
        booking2.setStatus(BookingStatus.CONFIRMED);
        booking2.setPaymentStatus(PaymentStatus.PAID);
        booking2 = bookingRepository.save(booking2);

        CreateReviewRequest req1 = new CreateReviewRequest(
                testHotel.getId(),
                paidBooking.getId(),
                4,
                "First review submitted",
                new ReviewCategoriesDto(4, 4, 4, 4, 4)
        );
        reviewService.createReview(req1, testUser.getId());

        CreateReviewRequest req2 = new CreateReviewRequest(
                testHotel.getId(),
                booking2.getId(),
                5,
                "Second review submitted",
                new ReviewCategoriesDto(5, 5, 5, 5, 5)
        );
        reviewService.createReview(req2, otherUser.getId());

        List<ReviewDto> reviews = reviewService.getReviewsByHotelId(testHotel.getId());
        assertEquals(2, reviews.size());
        assertEquals("Second review submitted", reviews.get(0).getComment());
        assertEquals("First review submitted", reviews.get(1).getComment());
    }

    @Test
    @DisplayName("getReviewSummary returns accurate hotel summary")
    void getReviewSummary_returnsAccurateSummary() {
        CreateReviewRequest req1 = new CreateReviewRequest(
                testHotel.getId(),
                paidBooking.getId(),
                5,
                "Great stay!",
                new ReviewCategoriesDto(5, 5, 5, 5, 5)
        );
        reviewService.createReview(req1, testUser.getId());

        ReviewSummaryDto summary = reviewService.getReviewSummary(testHotel.getId());
        assertEquals(testHotel.getId(), summary.getHotelId());
        assertEquals(5.0, summary.getAverageRating());
        assertEquals(1, summary.getReviewCount());
    }

    @Test
    @DisplayName("getReviewSummary returns 0.0 and count 0 when hotel has no reviews")
    void getReviewSummary_noReviews_returnsZero() {
        ReviewSummaryDto summary = reviewService.getReviewSummary(otherHotel.getId());
        assertEquals(otherHotel.getId(), summary.getHotelId());
        assertEquals(0.0, summary.getAverageRating());
        assertEquals(0, summary.getReviewCount());
    }

    @Test
    @DisplayName("getAllReviews limits results according to parameter (max 200)")
    void getAllReviews_respectsLimitAndClampsTo200() {
        CreateReviewRequest req = new CreateReviewRequest(
                testHotel.getId(),
                paidBooking.getId(),
                5,
                "Global review check",
                new ReviewCategoriesDto(5, 5, 5, 5, 5)
        );
        reviewService.createReview(req, testUser.getId());

        List<ReviewDto> list = reviewService.getAllReviews(10);
        assertEquals(1, list.size());

        List<ReviewDto> clamped = reviewService.getAllReviews(500);
        assertEquals(1, clamped.size());
    }
}

