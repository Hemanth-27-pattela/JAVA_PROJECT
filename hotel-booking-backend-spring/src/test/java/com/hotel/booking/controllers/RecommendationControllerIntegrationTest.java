package com.hotel.booking.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.booking.entities.Booking;
import com.hotel.booking.entities.Hotel;
import com.hotel.booking.entities.User;
import com.hotel.booking.entities.enums.BookingStatus;
import com.hotel.booking.entities.enums.UserRole;
import com.hotel.booking.repositories.BookingRepository;
import com.hotel.booking.repositories.HotelRepository;
import com.hotel.booking.repositories.UserRepository;
import com.hotel.booking.security.JwtTokenProvider;
import com.hotel.booking.services.ImageUploadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("RecommendationController Integration Tests")
public class RecommendationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private ImageUploadService imageUploadService;

    private User guestUser;
    private User guestUserB;
    private User ownerUser;
    private User adminUser;

    private String guestToken;
    private String guestTokenB;
    private String ownerToken;
    private String adminToken;

    private Hotel londonHotel;
    private Hotel edinburghHotel;
    private Hotel tokyoHotel;
    private Hotel inactiveHotel;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        hotelRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Users
        guestUser = new User();
        guestUser.setEmail("guest@rec-test.com");
        guestUser.setPassword(passwordEncoder.encode("password123"));
        guestUser.setFirstName("Alice");
        guestUser.setLastName("Guest");
        guestUser.setRole(UserRole.USER);
        guestUser.setIsActive(true);
        guestUser = userRepository.save(guestUser);
        guestToken = jwtTokenProvider.generateToken(guestUser.getId(), guestUser.getEmail(), guestUser.getRole());

        guestUserB = new User();
        guestUserB.setEmail("guestB@rec-test.com");
        guestUserB.setPassword(passwordEncoder.encode("password123"));
        guestUserB.setFirstName("Bob");
        guestUserB.setLastName("GuestB");
        guestUserB.setRole(UserRole.USER);
        guestUserB.setIsActive(true);
        guestUserB = userRepository.save(guestUserB);
        guestTokenB = jwtTokenProvider.generateToken(guestUserB.getId(), guestUserB.getEmail(), guestUserB.getRole());

        ownerUser = new User();
        ownerUser.setEmail("owner@rec-test.com");
        ownerUser.setPassword(passwordEncoder.encode("password123"));
        ownerUser.setFirstName("Oliver");
        ownerUser.setLastName("Owner");
        ownerUser.setRole(UserRole.HOTEL_OWNER);
        ownerUser.setIsActive(true);
        ownerUser = userRepository.save(ownerUser);
        ownerToken = jwtTokenProvider.generateToken(ownerUser.getId(), ownerUser.getEmail(), ownerUser.getRole());

        adminUser = new User();
        adminUser.setEmail("admin@rec-test.com");
        adminUser.setPassword(passwordEncoder.encode("password123"));
        adminUser.setFirstName("Arthur");
        adminUser.setLastName("Admin");
        adminUser.setRole(UserRole.ADMIN);
        adminUser.setIsActive(true);
        adminUser = userRepository.save(adminUser);
        adminToken = jwtTokenProvider.generateToken(adminUser.getId(), adminUser.getEmail(), adminUser.getRole());

        // 2. Hotels
        londonHotel = createHotel(
                "Thames Riverside Boutique", "London", "United Kingdom",
                List.of("Boutique", "Luxury"), List.of("WiFi", "Gym", "Spa", "Pool"),
                BigDecimal.valueOf(160.00), 5, 4.8, 30, 45, true, ownerUser
        );

        edinburghHotel = createHotel(
                "Royal Mile Suites", "Edinburgh", "United Kingdom",
                List.of("Boutique", "Historic"), List.of("WiFi", "Restaurant", "Bar"),
                BigDecimal.valueOf(120.00), 4, 4.6, 20, 25, true, ownerUser
        );

        tokyoHotel = createHotel(
                "Shinjuku City View", "Tokyo", "Japan",
                List.of("Modern", "Business"), List.of("WiFi", "Business Center"),
                BigDecimal.valueOf(210.00), 4, 4.3, 15, 10, true, ownerUser
        );

        inactiveHotel = createHotel(
                "Old Closed Inn", "London", "United Kingdom",
                List.of("Boutique"), List.of("WiFi"),
                BigDecimal.valueOf(90.00), 3, 4.9, 100, 200, false, ownerUser
        );

        // 3. Stays for Alice (guestUser) in London
        Booking stay = new Booking();
        stay.setUser(guestUser);
        stay.setHotel(londonHotel);
        stay.setFirstName("Alice");
        stay.setLastName("Guest");
        stay.setEmail(guestUser.getEmail());
        stay.setAdultCount(2);
        stay.setChildCount(0);
        stay.setCheckIn(Instant.now().minus(10, ChronoUnit.DAYS));
        stay.setCheckOut(Instant.now().minus(7, ChronoUnit.DAYS));
        stay.setTotalCost(BigDecimal.valueOf(480.00));
        stay.setStatus(BookingStatus.COMPLETED);
        bookingRepository.save(stay);
    }

    private Hotel createHotel(
            String name, String city, String country,
            List<String> types, List<String> facilities,
            BigDecimal price, int starRating, double avgRating,
            int reviewCount, int bookings, boolean isActive, User owner
    ) {
        Hotel h = new Hotel();
        h.setUser(owner);
        h.setName(name);
        h.setCity(city);
        h.setCountry(country);
        h.setDescription("High quality hotel accommodation in " + city);
        h.setType(types);
        h.setFacilities(facilities);
        h.setPricePerNight(price);
        h.setStarRating(starRating);
        h.setAverageRating(avgRating);
        h.setReviewCount(reviewCount);
        h.setTotalBookings(bookings);
        h.setIsActive(isActive);
        h.setImageUrls(List.of("https://example.com/h.jpg"));
        return hotelRepository.save(h);
    }

    @Test
    @DisplayName("Scenario 1: Public popular endpoint returns 200 OK without authentication")
    void testGetPopular_publicAccess() throws Exception {
        mockMvc.perform(get("/api/recommendations/popular")
                        .param("limit", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$[0].category", is("POPULAR")))
                .andExpect(jsonPath("$[0].explanation.primaryReason", containsString("bookings")));
    }

    @Test
    @DisplayName("Scenario 2: Public top-rated endpoint returns 200 OK without authentication")
    void testGetTopRated_publicAccess() throws Exception {
        mockMvc.perform(get("/api/recommendations/top-rated")
                        .param("limit", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$[0].category", is("TOP_RATED")))
                .andExpect(jsonPath("$[0].explanation.primaryReason", containsString("rated")));
    }

    @Test
    @DisplayName("Scenario 3: Public similar endpoint returns 200 OK and excludes source hotel")
    void testGetSimilar_publicAccess_excludesSource() throws Exception {
        mockMvc.perform(get("/api/recommendations/similar/" + londonHotel.getId())
                        .param("limit", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$[0].category", is("SIMILAR")))
                .andExpect(jsonPath("$[*]._id", not(hasItem(londonHotel.getId()))));
    }

    @Test
    @DisplayName("Scenario 4: Missing hotel ID in similar endpoint returns 404 NOT FOUND")
    void testGetSimilar_missingHotel_returns404() throws Exception {
        mockMvc.perform(get("/api/recommendations/similar/non-existent-uuid-999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Scenario 5: Authenticated personalized endpoint returns 200 OK for ROLE_USER")
    void testGetPersonalized_authenticatedUser_returns200() throws Exception {
        mockMvc.perform(get("/api/recommendations/personalized")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + guestToken)
                        .param("limit", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$[0].category", is("PERSONALIZED")))
                // Alice previously stayed in London => London hotel should be #1 with London reason
                .andExpect(jsonPath("$[0]._id", is(londonHotel.getId())))
                .andExpect(jsonPath("$[0].explanation.primaryReason", containsString("London")));
    }

    @Test
    @DisplayName("Scenario 6: Unauthenticated request to personalized endpoint returns 401 UNAUTHORIZED")
    void testGetPersonalized_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/recommendations/personalized")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Scenario 7: HOTEL_OWNER access to personalized endpoint is denied with 403 FORBIDDEN")
    void testGetPersonalized_hotelOwner_returns403() throws Exception {
        mockMvc.perform(get("/api/recommendations/personalized")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Scenario 8: ADMIN access to personalized endpoint is denied with 403 FORBIDDEN")
    void testGetPersonalized_admin_returns403() throws Exception {
        mockMvc.perform(get("/api/recommendations/personalized")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Scenario 9: Cross-user access prevention - principal identity is authoritative")
    void testGetPersonalized_crossUserPrevention() throws Exception {
        // Attacker attempts to spoof Alice's recommendations while logged in as Bob
        mockMvc.perform(get("/api/recommendations/personalized")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + guestTokenB)
                        .param("userId", guestUser.getId()) // injected param
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // Bob has no stays in London, so cold-start or general hybrid applies rather than Alice's London preference
                .andExpect(jsonPath("$", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("Scenario 10: Inactive hotels are excluded from all recommendation endpoints")
    void testInactiveHotelExclusion_acrossAllEndpoints() throws Exception {
        String inactiveId = inactiveHotel.getId();

        // 1. Popular
        mockMvc.perform(get("/api/recommendations/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*]._id", not(hasItem(inactiveId))));

        // 2. Top-rated
        mockMvc.perform(get("/api/recommendations/top-rated"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*]._id", not(hasItem(inactiveId))));

        // 3. Similar
        mockMvc.perform(get("/api/recommendations/similar/" + londonHotel.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*]._id", not(hasItem(inactiveId))));

        // 4. Personalized
        mockMvc.perform(get("/api/recommendations/personalized")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + guestToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*]._id", not(hasItem(inactiveId))));
    }

    @Test
    @DisplayName("Scenario 11: Limit validation rejects invalid limits and respects valid limit")
    void testLimitValidation() throws Exception {
        // limit = 0 -> 400
        mockMvc.perform(get("/api/recommendations/popular").param("limit", "0"))
                .andExpect(status().isBadRequest());

        // limit = -5 -> 400
        mockMvc.perform(get("/api/recommendations/popular").param("limit", "-5"))
                .andExpect(status().isBadRequest());

        // limit = 100 -> 400 (max is 50)
        mockMvc.perform(get("/api/recommendations/popular").param("limit", "100"))
                .andExpect(status().isBadRequest());

        // limit = 1 -> returns exactly 1
        mockMvc.perform(get("/api/recommendations/popular").param("limit", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("Scenario 12: Empty recommendation results return 200 OK with empty array")
    void testEmptyRecommendations_whenNoHotels() throws Exception {
        bookingRepository.deleteAll();
        hotelRepository.deleteAll();

        mockMvc.perform(get("/api/recommendations/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        mockMvc.perform(get("/api/recommendations/top-rated"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        mockMvc.perform(get("/api/recommendations/personalized")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + guestToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Scenario 13: Response DTO serialization contains all required fields")
    void testResponseDtoSerialization() throws Exception {
        mockMvc.perform(get("/api/recommendations/popular").param("limit", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]._id", notNullValue()))
                .andExpect(jsonPath("$[0].name", notNullValue()))
                .andExpect(jsonPath("$[0].city", notNullValue()))
                .andExpect(jsonPath("$[0].country", notNullValue()))
                .andExpect(jsonPath("$[0].pricePerNight", notNullValue()))
                .andExpect(jsonPath("$[0].averageRating", notNullValue()))
                .andExpect(jsonPath("$[0].reviewCount", notNullValue()))
                .andExpect(jsonPath("$[0].starRating", notNullValue()))
                .andExpect(jsonPath("$[0].score", notNullValue()))
                .andExpect(jsonPath("$[0].category", notNullValue()))
                .andExpect(jsonPath("$[0].explanation", notNullValue()))
                .andExpect(jsonPath("$[0].explanation.primaryReason", notNullValue()))
                .andExpect(jsonPath("$[0].explanation.supportingReasons", notNullValue()))
                .andExpect(jsonPath("$[0].explanation.relevantFeatures", notNullValue()));
    }
}

