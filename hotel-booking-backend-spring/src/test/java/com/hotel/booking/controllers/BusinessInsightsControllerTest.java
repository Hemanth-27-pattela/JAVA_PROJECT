package com.hotel.booking.controllers;

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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class BusinessInsightsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private ImageUploadService imageUploadService;

    private User adminUser;
    private User owner1;
    private User owner2;
    private User emptyOwner;
    private User guestUser;

    private String adminToken;
    private String owner1Token;
    private String owner2Token;
    private String emptyOwnerToken;
    private String guestToken;

    private Hotel hotel1;
    private Hotel hotel2;

    @BeforeEach
    void setUp() {
        reviewRepository.deleteAll();
        bookingRepository.deleteAll();
        hotelRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Admin
        adminUser = new User();
        adminUser.setEmail("admin_bi@test.com");
        adminUser.setPassword("password123");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setRole(UserRole.ADMIN);
        adminUser = userRepository.save(adminUser);
        adminToken = jwtTokenProvider.generateToken(adminUser.getId(), adminUser.getEmail(), UserRole.ADMIN);

        // 2. Owner 1
        owner1 = new User();
        owner1.setEmail("owner1_bi@test.com");
        owner1.setPassword("password123");
        owner1.setFirstName("Owner");
        owner1.setLastName("One");
        owner1.setRole(UserRole.HOTEL_OWNER);
        owner1 = userRepository.save(owner1);
        owner1Token = jwtTokenProvider.generateToken(owner1.getId(), owner1.getEmail(), UserRole.HOTEL_OWNER);

        // 3. Owner 2
        owner2 = new User();
        owner2.setEmail("owner2_bi@test.com");
        owner2.setPassword("password123");
        owner2.setFirstName("Owner");
        owner2.setLastName("Two");
        owner2.setRole(UserRole.HOTEL_OWNER);
        owner2 = userRepository.save(owner2);
        owner2Token = jwtTokenProvider.generateToken(owner2.getId(), owner2.getEmail(), UserRole.HOTEL_OWNER);

        // 4. Owner with no hotels
        emptyOwner = new User();
        emptyOwner.setEmail("empty_owner_bi@test.com");
        emptyOwner.setPassword("password123");
        emptyOwner.setFirstName("Empty");
        emptyOwner.setLastName("Owner");
        emptyOwner.setRole(UserRole.HOTEL_OWNER);
        emptyOwner = userRepository.save(emptyOwner);
        emptyOwnerToken = jwtTokenProvider.generateToken(emptyOwner.getId(), emptyOwner.getEmail(), UserRole.HOTEL_OWNER);

        // 5. Guest (USER)
        guestUser = new User();
        guestUser.setEmail("guest_bi@test.com");
        guestUser.setPassword("password123");
        guestUser.setFirstName("Guest");
        guestUser.setLastName("User");
        guestUser.setRole(UserRole.USER);
        guestUser = userRepository.save(guestUser);
        guestToken = jwtTokenProvider.generateToken(guestUser.getId(), guestUser.getEmail(), UserRole.USER);

        // Hotel 1 belonging to Owner 1
        hotel1 = new Hotel();
        hotel1.setName("Seaside Resort");
        hotel1.setCity("Brighton");
        hotel1.setCountry("UK");
        hotel1.setDescription("Charming resort by the sea");
        hotel1.setUser(owner1);
        hotel1.setPricePerNight(BigDecimal.valueOf(200.00));
        hotel1.setStarRating(4);
        hotel1.setLastUpdated(Instant.now());
        hotel1 = hotelRepository.save(hotel1);

        // Hotel 2 belonging to Owner 2
        hotel2 = new Hotel();
        hotel2.setName("Mountain Lodge");
        hotel2.setCity("Inverness");
        hotel2.setCountry("UK");
        hotel2.setDescription("Rustic mountain retreat");
        hotel2.setUser(owner2);
        hotel2.setPricePerNight(BigDecimal.valueOf(350.00));
        hotel2.setStarRating(5);
        hotel2.setLastUpdated(Instant.now());
        hotel2 = hotelRepository.save(hotel2);

        // Booking 1 on Hotel 1: £400
        Booking b1 = new Booking();
        b1.setUser(guestUser);
        b1.setUserId(guestUser.getId());
        b1.setHotel(hotel1);
        b1.setHotelId(hotel1.getId());
        b1.setFirstName("Guest");
        b1.setLastName("User");
        b1.setEmail("guest@test.com");
        b1.setAdultCount(2);
        b1.setChildCount(0);
        b1.setCheckIn(Instant.now().minus(5, ChronoUnit.DAYS));
        b1.setCheckOut(Instant.now().minus(3, ChronoUnit.DAYS));
        b1.setTotalCost(BigDecimal.valueOf(400.00));
        b1.setStatus(BookingStatus.CONFIRMED);
        b1.setPaymentStatus(PaymentStatus.PAID);
        bookingRepository.save(b1);

        // Booking 2 on Hotel 2: £1050
        Booking b2 = new Booking();
        b2.setUser(guestUser);
        b2.setUserId(guestUser.getId());
        b2.setHotel(hotel2);
        b2.setHotelId(hotel2.getId());
        b2.setFirstName("Guest");
        b2.setLastName("User");
        b2.setEmail("guest@test.com");
        b2.setAdultCount(2);
        b2.setChildCount(1);
        b2.setCheckIn(Instant.now().minus(10, ChronoUnit.DAYS));
        b2.setCheckOut(Instant.now().minus(7, ChronoUnit.DAYS));
        b2.setTotalCost(BigDecimal.valueOf(1050.00));
        b2.setStatus(BookingStatus.CONFIRMED);
        b2.setPaymentStatus(PaymentStatus.PAID);
        bookingRepository.save(b2);
    }

    // --- Access Control & Authentication Tests ---

    @Test
    @DisplayName("GET /api/business-insights/dashboard/public - Unauthenticated returns 401")
    void getPublicDashboard_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/business-insights/dashboard/public"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/business-insights/dashboard - Unauthenticated returns 401")
    void getDashboard_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/business-insights/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/business-insights/dashboard - USER (Guest) returns 403 Forbidden")
    void getDashboard_guest_returns403() throws Exception {
        mockMvc.perform(get("/api/business-insights/dashboard")
                        .header("Authorization", "Bearer " + guestToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/business-insights/forecast - USER (Guest) returns 403 Forbidden")
    void getForecast_guest_returns403() throws Exception {
        mockMvc.perform(get("/api/business-insights/forecast")
                        .header("Authorization", "Bearer " + guestToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/business-insights/system-stats - USER (Guest) returns 403 Forbidden")
    void getSystemStats_guest_returns403() throws Exception {
        mockMvc.perform(get("/api/business-insights/system-stats")
                        .header("Authorization", "Bearer " + guestToken))
                .andExpect(status().isForbidden());
    }

    // --- Scoping & Owner Isolation Tests ---

    @Test
    @DisplayName("GET /api/business-insights/dashboard - Owner 1 sees only their own hotel metrics")
    void getDashboard_owner1_seesOnlyOwner1Hotels() throws Exception {
        mockMvc.perform(get("/api/business-insights/dashboard")
                        .header("Authorization", "Bearer " + owner1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overview.totalHotels", is(1)))
                .andExpect(jsonPath("$.overview.totalBookings", is(1)))
                .andExpect(jsonPath("$.overview.totalRevenue", is(400.0)))
                .andExpect(jsonPath("$.hotelPerformance", hasSize(1)))
                .andExpect(jsonPath("$.hotelPerformance[0]._id", is(hotel1.getId())))
                .andExpect(jsonPath("$.hotelPerformance[0].name", is("Seaside Resort")));
    }

    @Test
    @DisplayName("GET /api/business-insights/dashboard - Owner 2 sees only their own hotel metrics")
    void getDashboard_owner2_seesOnlyOwner2Hotels() throws Exception {
        mockMvc.perform(get("/api/business-insights/dashboard")
                        .header("Authorization", "Bearer " + owner2Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overview.totalHotels", is(1)))
                .andExpect(jsonPath("$.overview.totalBookings", is(1)))
                .andExpect(jsonPath("$.overview.totalRevenue", is(1050.0)))
                .andExpect(jsonPath("$.hotelPerformance", hasSize(1)))
                .andExpect(jsonPath("$.hotelPerformance[0]._id", is(hotel2.getId())))
                .andExpect(jsonPath("$.hotelPerformance[0].name", is("Mountain Lodge")));
    }

    @Test
    @DisplayName("GET /api/business-insights/dashboard - Owner with no hotels receives empty state")
    void getDashboard_emptyOwner_returnsEmptyState() throws Exception {
        mockMvc.perform(get("/api/business-insights/dashboard")
                        .header("Authorization", "Bearer " + emptyOwnerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overview.totalHotels", is(0)))
                .andExpect(jsonPath("$.overview.totalBookings", is(0)))
                .andExpect(jsonPath("$.overview.totalRevenue", is(0.0)))
                .andExpect(jsonPath("$.hotelPerformance", hasSize(0)))
                .andExpect(jsonPath("$.popularDestinations", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/business-insights/dashboard - Admin sees global platform-wide metrics")
    void getDashboard_admin_seesGlobalMetrics() throws Exception {
        mockMvc.perform(get("/api/business-insights/dashboard")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overview.totalHotels", is(2)))
                .andExpect(jsonPath("$.overview.totalBookings", is(2)))
                .andExpect(jsonPath("$.overview.totalRevenue", is(1450.0)))
                .andExpect(jsonPath("$.hotelPerformance", hasSize(2)));
    }

    // --- System Stats & Telemetry Isolation Tests ---

    @Test
    @DisplayName("GET /api/business-insights/system-stats - Owner omits system telemetry")
    void getSystemStats_owner_omitsSystemTelemetry() throws Exception {
        mockMvc.perform(get("/api/business-insights/system-stats")
                        .header("Authorization", "Bearer " + owner1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.database.totalHotels", is(1)))
                .andExpect(jsonPath("$.database.totalBookings", is(1)))
                .andExpect(jsonPath("$.database.totalRevenue", is(400.0)))
                .andExpect(jsonPath("$.system").doesNotExist());
    }

    @Test
    @DisplayName("GET /api/business-insights/system-stats - Admin includes system telemetry")
    void getSystemStats_admin_includesSystemTelemetry() throws Exception {
        mockMvc.perform(get("/api/business-insights/system-stats")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.database.totalHotels", is(2)))
                .andExpect(jsonPath("$.database.totalBookings", is(2)))
                .andExpect(jsonPath("$.database.totalRevenue", is(1450.0)))
                .andExpect(jsonPath("$.system.memory", notNullValue()));
    }

    // --- Rollups Access Control Tests ---

    @Test
    @DisplayName("GET /api/business-insights/rollups - Admin allowed, Owner forbidden, User forbidden, Anonymous unauthorized")
    void getRollups_authorizationEnforced() throws Exception {
        // Admin: 200 OK
        mockMvc.perform(get("/api/business-insights/rollups")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // Hotel Owner: 403 Forbidden
        mockMvc.perform(get("/api/business-insights/rollups")
                        .header("Authorization", "Bearer " + owner1Token))
                .andExpect(status().isForbidden());

        // Regular User: 403 Forbidden
        mockMvc.perform(get("/api/business-insights/rollups")
                        .header("Authorization", "Bearer " + guestToken))
                .andExpect(status().isForbidden());

        // Anonymous: 401 Unauthorized
        mockMvc.perform(get("/api/business-insights/rollups"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/business-insights/rollups - Admin captures rollup, Owner and User forbidden")
    void createRollup_adminOnly() throws Exception {
        // Owner forbidden
        mockMvc.perform(post("/api/business-insights/rollups")
                        .header("Authorization", "Bearer " + owner1Token))
                .andExpect(status().isForbidden());

        // User forbidden
        mockMvc.perform(post("/api/business-insights/rollups")
                        .header("Authorization", "Bearer " + guestToken))
                .andExpect(status().isForbidden());

        // Admin: 201 Created
        mockMvc.perform(post("/api/business-insights/rollups")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$._id", notNullValue()))
                .andExpect(jsonPath("$.metrics", notNullValue()))
                .andExpect(jsonPath("$.breakdown", notNullValue()));
    }
}
