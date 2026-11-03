package com.hotel.booking.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.booking.dtos.CancelBookingRequest;
import com.hotel.booking.dtos.CreateBookingRequest;
import com.hotel.booking.dtos.PaymentIntentRequest;
import com.hotel.booking.dtos.UpdateBookingPaymentRequest;
import com.hotel.booking.dtos.UpdateBookingStatusRequest;
import com.hotel.booking.entities.Booking;
import com.hotel.booking.entities.Hotel;
import com.hotel.booking.entities.User;
import com.hotel.booking.entities.enums.BookingStatus;
import com.hotel.booking.entities.enums.PaymentStatus;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    private User admin;
    private User owner;
    private User guest1;
    private User guest2;
    private Hotel hotel;
    private Booking booking1;

    private String adminToken;
    private String ownerToken;
    private String guest1Token;
    private String guest2Token;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        hotelRepository.deleteAll();
        userRepository.deleteAll();

        admin = new User();
        admin.setEmail("admin_bctrl@test.com");
        admin.setPassword("password123");
        admin.setFirstName("Super");
        admin.setLastName("Admin");
        admin.setRole(UserRole.ADMIN);
        admin = userRepository.save(admin);
        adminToken = jwtTokenProvider.generateToken(admin.getId(), admin.getEmail(), UserRole.ADMIN);

        owner = new User();
        owner.setEmail("owner_bctrl@test.com");
        owner.setPassword("password123");
        owner.setFirstName("Hotel");
        owner.setLastName("Host");
        owner.setRole(UserRole.HOTEL_OWNER);
        owner = userRepository.save(owner);
        ownerToken = jwtTokenProvider.generateToken(owner.getId(), owner.getEmail(), UserRole.HOTEL_OWNER);

        guest1 = new User();
        guest1.setEmail("guest1_bctrl@test.com");
        guest1.setPassword("password123");
        guest1.setFirstName("Alice");
        guest1.setLastName("Walker");
        guest1.setRole(UserRole.USER);
        guest1 = userRepository.save(guest1);
        guest1Token = jwtTokenProvider.generateToken(guest1.getId(), guest1.getEmail(), UserRole.USER);

        guest2 = new User();
        guest2.setEmail("guest2_bctrl@test.com");
        guest2.setPassword("password123");
        guest2.setFirstName("Bob");
        guest2.setLastName("Walker");
        guest2.setRole(UserRole.USER);
        guest2 = userRepository.save(guest2);
        guest2Token = jwtTokenProvider.generateToken(guest2.getId(), guest2.getEmail(), UserRole.USER);

        hotel = new Hotel();
        hotel.setUser(owner);
        hotel.setName("Milan Duomo Suites");
        hotel.setCity("Milan");
        hotel.setCountry("Italy");
        hotel.setDescription("Central Milan luxury suites.");
        hotel.setType(List.of("Luxury", "City"));
        hotel.setFacilities(List.of("WiFi", "Bar"));
        hotel.setPricePerNight(new BigDecimal("200.00"));
        hotel.setStarRating(5);
        hotel.setAdultCount(2);
        hotel.setChildCount(1);
        hotel.setIsActive(true);
        hotel.setLastUpdated(Instant.now());
        hotel = hotelRepository.save(hotel);

        booking1 = new Booking();
        booking1.setUser(guest1);
        booking1.setUserId(guest1.getId());
        booking1.setHotel(hotel);
        booking1.setHotelId(hotel.getId());
        booking1.setFirstName("Alice");
        booking1.setLastName("Walker");
        booking1.setEmail("guest1_bctrl@test.com");
        booking1.setAdultCount(2);
        booking1.setChildCount(0);
        booking1.setCheckIn(Instant.now().plus(7, ChronoUnit.DAYS));
        booking1.setCheckOut(Instant.now().plus(10, ChronoUnit.DAYS));
        booking1.setTotalCost(new BigDecimal("600.00"));
        booking1.setStatus(BookingStatus.CONFIRMED);
        booking1.setPaymentStatus(PaymentStatus.PAID);
        booking1.setStripePaymentIntentId("pi_test_valid_mock");
        booking1 = bookingRepository.save(booking1);
    }

    @Test
    @DisplayName("POST /api/hotels/{hotelId}/bookings creates room booking successfully")
    void testCreateRoomBookingEndpoint() throws Exception {
        PaymentIntentRequest piReq = new PaymentIntentRequest(3);
        String piResJson = mockMvc.perform(post("/api/hotels/" + hotel.getId() + "/bookings/payment-intent")
                        .header("Authorization", "Bearer " + guest1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(piReq)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String piId = objectMapper.readTree(piResJson).path("paymentIntentId").asText();

        CreateBookingRequest request = new CreateBookingRequest();
        request.setFirstName("Alice");
        request.setLastName("Walker");
        request.setEmail("guest1_bctrl@test.com");
        request.setAdultCount(2);
        request.setChildCount(0);
        request.setCheckIn(Instant.now().plus(20, ChronoUnit.DAYS));
        request.setCheckOut(Instant.now().plus(23, ChronoUnit.DAYS));
        request.setTotalCost(new BigDecimal("600.00"));
        request.setPaymentIntentId(piId);

        mockMvc.perform(post("/api/hotels/" + hotel.getId() + "/bookings")
                        .header("Authorization", "Bearer " + guest1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/hotels/{hotelId}/bookings with forged paymentIntentId returns 400 Bad Request")
    void testCreateRoomBookingForgedPaymentIntentReturns400() throws Exception {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setFirstName("Alice");
        request.setLastName("Walker");
        request.setEmail("guest1_bctrl@test.com");
        request.setAdultCount(2);
        request.setChildCount(0);
        request.setCheckIn(Instant.now().plus(20, ChronoUnit.DAYS));
        request.setCheckOut(Instant.now().plus(23, ChronoUnit.DAYS));
        request.setTotalCost(new BigDecimal("600.00"));
        request.setPaymentIntentId("pi_forged_unknown_99999");

        mockMvc.perform(post("/api/hotels/" + hotel.getId() + "/bookings")
                        .header("Authorization", "Bearer " + guest1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("payment intent not found")));
    }

    @Test
    @DisplayName("POST /api/hotels/{hotelId}/bookings/payment-intent creates intent with totalCost")
    void testCreatePaymentIntentEndpoint() throws Exception {
        PaymentIntentRequest request = new PaymentIntentRequest(3); // 3 nights * $200 = $600

        mockMvc.perform(post("/api/hotels/" + hotel.getId() + "/bookings/payment-intent")
                        .header("Authorization", "Bearer " + guest1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentIntentId", notNullValue()))
                .andExpect(jsonPath("$.clientSecret", notNullValue()))
                .andExpect(jsonPath("$.totalCost", is(600.00)));
    }

    @Test
    @DisplayName("GET /api/my-bookings returns user bookings with hotel information")
    void testGetMyBookingsEndpoint() throws Exception {
        mockMvc.perform(get("/api/my-bookings")
                        .header("Authorization", "Bearer " + guest1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Milan Duomo Suites")))
                .andExpect(jsonPath("$[0].bookings", hasSize(1)))
                .andExpect(jsonPath("$[0].bookings[0].firstName", is("Alice")));
    }

    @Test
    @DisplayName("GET /api/bookings returns all bookings for ADMIN")
    void testAdminGetAllBookings() throws Exception {
        mockMvc.perform(get("/api/bookings")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0]._id", is(booking1.getId())));
    }

    @Test
    @DisplayName("GET /api/bookings returns 403 for ordinary USER")
    void testAdminGetAllBookingsForbiddenForUser() throws Exception {
        mockMvc.perform(get("/api/bookings")
                        .header("Authorization", "Bearer " + guest1Token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", is("Access denied")));
    }

    @Test
    @DisplayName("GET /api/bookings/hotel/{hotelId} returns bookings for hotel owner")
    void testGetBookingsByHotelIdForOwner() throws Exception {
        mockMvc.perform(get("/api/bookings/hotel/" + hotel.getId())
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].firstName", is("Alice")));
    }

    @Test
    @DisplayName("GET /api/bookings/{id} returns booking details for guest owner")
    void testGetBookingByIdForGuest() throws Exception {
        mockMvc.perform(get("/api/bookings/" + booking1.getId())
                        .header("Authorization", "Bearer " + guest1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._id", is(booking1.getId())))
                .andExpect(jsonPath("$.totalCost", is(600.00)));
    }

    @Test
    @DisplayName("GET /api/bookings/{id} returns 403 for unrelated guest")
    void testGetBookingByIdForbiddenForOtherGuest() throws Exception {
        mockMvc.perform(get("/api/bookings/" + booking1.getId())
                        .header("Authorization", "Bearer " + guest2Token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", is("Access denied")));
    }

    @Test
    @DisplayName("POST /api/bookings/{id}/cancel cancels booking and refunds payment")
    void testCancelBookingEndpoint() throws Exception {
        CancelBookingRequest request = new CancelBookingRequest("Plans changed");

        mockMvc.perform(post("/api/bookings/" + booking1.getId() + "/cancel")
                        .header("Authorization", "Bearer " + guest1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.booking.status", is("cancelled")))
                .andExpect(jsonPath("$.booking.paymentStatus", is("refunded")))
                .andExpect(jsonPath("$.refundAmount", is(600.00)));
    }

    @Test
    @DisplayName("POST /api/bookings/{id}/cancel rejects duplicate cancel attempt with 400 Bad Request")
    void testDuplicateCancelBookingReturns400() throws Exception {
        CancelBookingRequest request = new CancelBookingRequest("Plans changed");

        // First cancel succeeds
        mockMvc.perform(post("/api/bookings/" + booking1.getId() + "/cancel")
                        .header("Authorization", "Bearer " + guest1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Second cancel attempt must return 400 Bad Request
        mockMvc.perform(post("/api/bookings/" + booking1.getId() + "/cancel")
                        .header("Authorization", "Bearer " + guest1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Booking cannot be cancelled (must be upcoming pending/confirmed)")));
    }

    @Test
    @DisplayName("PATCH /api/bookings/{id}/status updates booking status by owner")
    void testUpdateBookingStatusByOwner() throws Exception {
        UpdateBookingStatusRequest request = new UpdateBookingStatusRequest("completed", null);

        mockMvc.perform(patch("/api/bookings/" + booking1.getId() + "/status")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("completed")));
    }

    @Test
    @DisplayName("PATCH /api/bookings/{id}/status rejects cancel status with 400")
    void testUpdateBookingStatusRejectsDirectCancel() throws Exception {
        UpdateBookingStatusRequest request = new UpdateBookingStatusRequest("cancelled", null);

        mockMvc.perform(patch("/api/bookings/" + booking1.getId() + "/status")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Use POST /api/bookings/:id/cancel for cancel/refund")));
    }

    @Test
    @DisplayName("DELETE /api/bookings/{id} deletes booking for ADMIN")
    void testDeleteBookingByAdmin() throws Exception {
        mockMvc.perform(delete("/api/bookings/" + booking1.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Booking deleted successfully")));
    }
}

