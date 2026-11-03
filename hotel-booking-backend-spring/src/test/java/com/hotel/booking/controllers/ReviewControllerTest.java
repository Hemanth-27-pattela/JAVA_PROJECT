package com.hotel.booking.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.booking.dtos.CreateReviewRequest;
import com.hotel.booking.dtos.ReviewCategoriesDto;
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
import com.hotel.booking.security.JwtTokenProvider;
import com.hotel.booking.services.ImageUploadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private ImageUploadService imageUploadService;

    private User adminUser;
    private User regularUser;
    private Hotel testHotel;
    private Booking testBooking;
    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        reviewRepository.deleteAll();
        bookingRepository.deleteAll();
        hotelRepository.deleteAll();
        userRepository.deleteAll();

        adminUser = new User();
        adminUser.setEmail("admin_rev@test.com");
        adminUser.setPassword("password123");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setRole(UserRole.ADMIN);
        adminUser = userRepository.save(adminUser);
        adminToken = jwtTokenProvider.generateToken(adminUser.getId(), adminUser.getEmail(), UserRole.ADMIN);

        regularUser = new User();
        regularUser.setEmail("guest_rev@test.com");
        regularUser.setPassword("password123");
        regularUser.setFirstName("Guest");
        regularUser.setLastName("User");
        regularUser.setRole(UserRole.USER);
        regularUser = userRepository.save(regularUser);
        userToken = jwtTokenProvider.generateToken(regularUser.getId(), regularUser.getEmail(), UserRole.USER);

        testHotel = new Hotel();
        testHotel.setUser(adminUser);
        testHotel.setName("Grand Royal Hotel");
        testHotel.setCity("London");
        testHotel.setCountry("United Kingdom");
        testHotel.setDescription("Luxury central hotel");
        testHotel.setType(new ArrayList<>(List.of("Hotel")));
        testHotel.setPricePerNight(new BigDecimal("150.00"));
        testHotel.setStarRating(5);
        testHotel.setAdultCount(2);
        testHotel.setChildCount(1);
        testHotel.setImageUrls(new ArrayList<>(List.of("https://example.com/h1.jpg")));
        testHotel.setFacilities(new ArrayList<>(List.of("WiFi", "Gym")));
        testHotel.setIsActive(true);
        testHotel.setAverageRating(0.0);
        testHotel.setReviewCount(0);
        testHotel = hotelRepository.save(testHotel);

        Instant now = Instant.now();
        testBooking = new Booking();
        testBooking.setUser(regularUser);
        testBooking.setHotel(testHotel);
        testBooking.setFirstName("Guest");
        testBooking.setLastName("User");
        testBooking.setEmail(regularUser.getEmail());
        testBooking.setCheckIn(now.minus(4, ChronoUnit.DAYS));
        testBooking.setCheckOut(now.minus(1, ChronoUnit.DAYS));
        testBooking.setAdultCount(2);
        testBooking.setChildCount(0);
        testBooking.setTotalCost(new BigDecimal("450.00"));
        testBooking.setStatus(BookingStatus.CONFIRMED);
        testBooking.setPaymentStatus(PaymentStatus.PAID);
        testBooking = bookingRepository.save(testBooking);
    }

    @Test
    @DisplayName("GET /api/reviews/hotel/{hotelId} - Public access returns 200 OK")
    void getHotelReviews_publicAccess_returns200() throws Exception {
        mockMvc.perform(get("/api/reviews/hotel/" + testHotel.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/reviews/hotel/{hotelId}/summary - Public access returns 200 OK")
    void getHotelReviewSummary_publicAccess_returns200() throws Exception {
        mockMvc.perform(get("/api/reviews/hotel/" + testHotel.getId() + "/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hotelId", is(testHotel.getId())))
                .andExpect(jsonPath("$.averageRating", is(0.0)))
                .andExpect(jsonPath("$.reviewCount", is(0)));
    }

    @Test
    @DisplayName("POST /api/reviews - Authenticated user creates review and returns 201 Created")
    void createReview_authenticatedUser_returns201() throws Exception {
        CreateReviewRequest request = new CreateReviewRequest(
                testHotel.getId(),
                testBooking.getId(),
                5,
                "Outstanding hotel! Excellent cleanliness and service.",
                new ReviewCategoriesDto(5, 5, 5, 5, 5)
        );

        mockMvc.perform(post("/api/reviews")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$._id", notNullValue()))
                .andExpect(jsonPath("$.hotelId", is(testHotel.getId())))
                .andExpect(jsonPath("$.userId", is(regularUser.getId())))
                .andExpect(jsonPath("$.bookingId", is(testBooking.getId())))
                .andExpect(jsonPath("$.rating", is(5)))
                .andExpect(jsonPath("$.comment", is("Outstanding hotel! Excellent cleanliness and service.")))
                .andExpect(jsonPath("$.isVerified", is(true)))
                .andExpect(jsonPath("$.categories.cleanliness", is(5)))
                .andExpect(jsonPath("$.createdAt", notNullValue()));

        // Verify summary endpoint reflects the new review
        mockMvc.perform(get("/api/reviews/hotel/" + testHotel.getId() + "/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating", is(5.0)))
                .andExpect(jsonPath("$.reviewCount", is(1)));
    }

    @Test
    @DisplayName("POST /api/reviews - Unauthenticated user returns 401 Unauthorized")
    void createReview_unauthenticated_returns401() throws Exception {
        CreateReviewRequest request = new CreateReviewRequest(
                testHotel.getId(),
                testBooking.getId(),
                5,
                "Great stay!",
                new ReviewCategoriesDto(5, 5, 5, 5, 5)
        );

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/reviews - Validation error for short comment returns 400")
    void createReview_shortComment_returns400() throws Exception {
        CreateReviewRequest request = new CreateReviewRequest(
                testHotel.getId(),
                testBooking.getId(),
                5,
                "Hi", // less than 3 characters
                new ReviewCategoriesDto(5, 5, 5, 5, 5)
        );

        mockMvc.perform(post("/api/reviews")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Comment must be at least 3 characters")));
    }

    @Test
    @DisplayName("POST /api/reviews - Validation error for rating out of range returns 400")
    void createReview_invalidRating_returns400() throws Exception {
        CreateReviewRequest request = new CreateReviewRequest(
                testHotel.getId(),
                testBooking.getId(),
                6, // greater than 5
                "Good hotel",
                new ReviewCategoriesDto(5, 5, 5, 5, 5)
        );

        mockMvc.perform(post("/api/reviews")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Rating must be between 1 and 5")));
    }

    @Test
    @DisplayName("POST /api/reviews - Duplicate review on same booking returns 409 Conflict")
    void createReview_duplicateReview_returns409() throws Exception {
        CreateReviewRequest request = new CreateReviewRequest(
                testHotel.getId(),
                testBooking.getId(),
                5,
                "First review for booking",
                new ReviewCategoriesDto(5, 5, 5, 5, 5)
        );

        // First attempt succeeds
        mockMvc.perform(post("/api/reviews")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Second attempt fails with 409
        mockMvc.perform(post("/api/reviews")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", is("Review already exists for this booking")));
    }

    @Test
    @DisplayName("GET /api/reviews - Admin access returns 200 OK")
    void getAllReviews_adminUser_returns200() throws Exception {
        mockMvc.perform(get("/api/reviews")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/reviews - Regular user returns 403 Forbidden")
    void getAllReviews_regularUser_returns403() throws Exception {
        mockMvc.perform(get("/api/reviews")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/reviews - Unauthenticated returns 401 Unauthorized")
    void getAllReviews_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/reviews"))
                .andExpect(status().isUnauthorized());
    }
}

