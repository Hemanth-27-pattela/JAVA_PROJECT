package com.hotel.booking.services;

import com.hotel.booking.dtos.BookingDto;
import com.hotel.booking.dtos.CancelBookingRequest;
import com.hotel.booking.dtos.CancelBookingResponse;
import com.hotel.booking.dtos.CreateBookingRequest;
import com.hotel.booking.dtos.HotelWithBookingsDto;
import com.hotel.booking.dtos.PaymentIntentRequest;
import com.hotel.booking.dtos.PaymentIntentResponse;
import com.hotel.booking.dtos.UpdateBookingPaymentRequest;
import com.hotel.booking.entities.Booking;
import com.hotel.booking.entities.Hotel;
import com.hotel.booking.entities.User;
import com.hotel.booking.entities.enums.BookingStatus;
import com.hotel.booking.entities.enums.PaymentStatus;
import com.hotel.booking.entities.enums.UserRole;
import com.hotel.booking.repositories.BookingRepository;
import com.hotel.booking.repositories.HotelRepository;
import com.hotel.booking.repositories.UserRepository;
import com.hotel.booking.services.payment.PaymentGatewayService;
import com.hotel.booking.services.payment.RefundResult;
import com.hotel.booking.services.payment.SimulatedPaymentGatewayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
public class BookingServiceTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentGatewayService paymentGatewayService;

    @MockitoBean
    private ImageUploadService imageUploadService;

    private User owner;
    private User guest1;
    private User guest2;
    private Hotel hotel;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        hotelRepository.deleteAll();
        userRepository.deleteAll();

        if (paymentGatewayService instanceof SimulatedPaymentGatewayService sim) {
            sim.clear();
        }

        owner = new User();
        owner.setEmail("owner_booking@test.com");
        owner.setPassword("password123");
        owner.setFirstName("Host");
        owner.setLastName("Owner");
        owner.setRole(UserRole.HOTEL_OWNER);
        owner = userRepository.save(owner);

        guest1 = new User();
        guest1.setEmail("guest1_booking@test.com");
        guest1.setPassword("password123");
        guest1.setFirstName("Alice");
        guest1.setLastName("Guest");
        guest1.setRole(UserRole.USER);
        guest1 = userRepository.save(guest1);

        guest2 = new User();
        guest2.setEmail("guest2_booking@test.com");
        guest2.setPassword("password123");
        guest2.setFirstName("Bob");
        guest2.setLastName("Guest");
        guest2.setRole(UserRole.USER);
        guest2 = userRepository.save(guest2);

        hotel = new Hotel();
        hotel.setUser(owner);
        hotel.setName("Venice Canal Boutique");
        hotel.setCity("Venice");
        hotel.setCountry("Italy");
        hotel.setDescription("Lovely canal views in Venice.");
        hotel.setType(List.of("Boutique"));
        hotel.setFacilities(List.of("WiFi", "Breakfast"));
        hotel.setPricePerNight(new BigDecimal("150.00"));
        hotel.setStarRating(4);
        hotel.setAdultCount(2);
        hotel.setChildCount(1);
        hotel.setIsActive(true);
        hotel.setLastUpdated(Instant.now());
        hotel = hotelRepository.save(hotel);
    }

    private CreateBookingRequest createValidBookingRequest(User guest, int nights) {
        PaymentIntentResponse pi = bookingService.createPaymentIntent(
                hotel.getId(),
                new PaymentIntentRequest(nights),
                guest.getId()
        );

        CreateBookingRequest request = new CreateBookingRequest();
        request.setFirstName(guest.getFirstName());
        request.setLastName(guest.getLastName());
        request.setEmail(guest.getEmail());
        request.setAdultCount(2);
        request.setChildCount(0);
        request.setCheckIn(Instant.now().plus(10, ChronoUnit.DAYS));
        request.setCheckOut(Instant.now().plus(10 + nights, ChronoUnit.DAYS));
        request.setTotalCost(pi.getTotalCost());
        request.setPaymentIntentId(pi.getPaymentIntentId());
        return request;
    }

    @Test
    @DisplayName("Create valid booking and verify hotel and user analytics incremented")
    void testCreateBookingSuccess() {
        CreateBookingRequest request = createValidBookingRequest(guest1, 3);

        BookingDto created = bookingService.createBooking(hotel.getId(), request, guest1.getId());
        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals("Alice", created.getFirstName());
        assertEquals(BookingStatus.CONFIRMED, created.getStatus());
        assertEquals(PaymentStatus.PAID, created.getPaymentStatus());

        // Verify hotel statistics incremented
        Hotel updatedHotel = hotelRepository.findById(hotel.getId()).orElseThrow();
        assertEquals(1, updatedHotel.getTotalBookings());
        assertEquals(0, new BigDecimal("450.00").compareTo(updatedHotel.getTotalRevenue()));

        // Verify user statistics incremented
        User updatedUser = userRepository.findById(guest1.getId()).orElseThrow();
        assertEquals(1, updatedUser.getTotalBookings());
        assertEquals(0, new BigDecimal("450.00").compareTo(updatedUser.getTotalSpent()));
    }

    @Test
    @DisplayName("Create booking with forged PaymentIntent ID throws 400 Bad Request")
    void testCreateBookingRejectsForgedPaymentIntent() {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setFirstName("Alice");
        request.setLastName("Guest");
        request.setEmail("guest1_booking@test.com");
        request.setAdultCount(2);
        request.setCheckIn(Instant.now().plus(10, ChronoUnit.DAYS));
        request.setCheckOut(Instant.now().plus(13, ChronoUnit.DAYS));
        request.setTotalCost(new BigDecimal("450.00"));
        request.setPaymentIntentId("pi_forged_unknown_99999");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            bookingService.createBooking(hotel.getId(), request, guest1.getId());
        });
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("payment intent not found"));

        // Ensure no booking saved and stats untouched
        assertEquals(0, bookingRepository.count());
        Hotel updatedHotel = hotelRepository.findById(hotel.getId()).orElseThrow();
        assertEquals(0, updatedHotel.getTotalBookings());
    }

    @Test
    @DisplayName("Create booking with unpaid PaymentIntent throws 400 Bad Request")
    void testCreateBookingRejectsUnpaidPaymentIntent() {
        if (paymentGatewayService instanceof SimulatedPaymentGatewayService sim) {
            sim.registerSimulatedIntent(
                    "pi_sim_unpaid_123",
                    hotel.getId(),
                    guest1.getId(),
                    new BigDecimal("450.00"),
                    "gbp",
                    "requires_payment_method"
            );
        }

        CreateBookingRequest request = new CreateBookingRequest();
        request.setFirstName("Alice");
        request.setLastName("Guest");
        request.setEmail("guest1_booking@test.com");
        request.setAdultCount(2);
        request.setCheckIn(Instant.now().plus(10, ChronoUnit.DAYS));
        request.setCheckOut(Instant.now().plus(13, ChronoUnit.DAYS));
        request.setTotalCost(new BigDecimal("450.00"));
        request.setPaymentIntentId("pi_sim_unpaid_123");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            bookingService.createBooking(hotel.getId(), request, guest1.getId());
        });
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("payment intent not succeeded"));
        assertEquals(0, bookingRepository.count());
    }

    @Test
    @DisplayName("Create booking with amount mismatch throws 400 Bad Request")
    void testCreateBookingRejectsAmountMismatch() {
        if (paymentGatewayService instanceof SimulatedPaymentGatewayService sim) {
            // Register intent for only 100.00 instead of 450.00
            sim.registerSimulatedIntent(
                    "pi_sim_short_pay",
                    hotel.getId(),
                    guest1.getId(),
                    new BigDecimal("100.00"),
                    "gbp",
                    "succeeded"
            );
        }

        CreateBookingRequest request = new CreateBookingRequest();
        request.setFirstName("Alice");
        request.setLastName("Guest");
        request.setEmail("guest1_booking@test.com");
        request.setAdultCount(2);
        request.setCheckIn(Instant.now().plus(10, ChronoUnit.DAYS));
        request.setCheckOut(Instant.now().plus(13, ChronoUnit.DAYS));
        request.setTotalCost(new BigDecimal("450.00"));
        request.setPaymentIntentId("pi_sim_short_pay");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            bookingService.createBooking(hotel.getId(), request, guest1.getId());
        });
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("amount mismatch"));
        assertEquals(0, bookingRepository.count());
    }

    @Test
    @DisplayName("Create booking with mismatched user metadata throws 400 Bad Request")
    void testCreateBookingRejectsUserMismatch() {
        // Create intent for guest2
        PaymentIntentResponse pi = bookingService.createPaymentIntent(
                hotel.getId(),
                new PaymentIntentRequest(3),
                guest2.getId()
        );

        // Guest 1 tries to use Guest 2's payment intent
        CreateBookingRequest request = new CreateBookingRequest();
        request.setFirstName("Alice");
        request.setLastName("Guest");
        request.setEmail("guest1_booking@test.com");
        request.setAdultCount(2);
        request.setCheckIn(Instant.now().plus(10, ChronoUnit.DAYS));
        request.setCheckOut(Instant.now().plus(13, ChronoUnit.DAYS));
        request.setTotalCost(pi.getTotalCost());
        request.setPaymentIntentId(pi.getPaymentIntentId());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            bookingService.createBooking(hotel.getId(), request, guest1.getId());
        });
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("payment intent mismatch"));
        assertEquals(0, bookingRepository.count());
    }

    @Test
    @DisplayName("Create booking with invalid date range throws IllegalArgumentException")
    void testCreateBookingInvalidDates() {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setFirstName("Alice");
        request.setLastName("Guest");
        request.setEmail("guest1_booking@test.com");
        request.setAdultCount(2);
        request.setCheckIn(Instant.now().plus(15, ChronoUnit.DAYS));
        request.setCheckOut(Instant.now().plus(10, ChronoUnit.DAYS));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(hotel.getId(), request, guest1.getId());
        });
        assertTrue(ex.getMessage().contains("Check-out date must be after check-in date"));
    }

    @Test
    @DisplayName("Create booking on inactive hotel throws IllegalArgumentException")
    void testCreateBookingInactiveHotel() {
        hotel.setIsActive(false);
        hotelRepository.save(hotel);

        CreateBookingRequest request = new CreateBookingRequest();
        request.setFirstName("Alice");
        request.setLastName("Guest");
        request.setEmail("guest1_booking@test.com");
        request.setAdultCount(2);
        request.setCheckIn(Instant.now().plus(5, ChronoUnit.DAYS));
        request.setCheckOut(Instant.now().plus(7, ChronoUnit.DAYS));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(hotel.getId(), request, guest1.getId());
        });
        assertTrue(ex.getMessage().contains("Hotel is currently inactive"));
    }

    @Test
    @DisplayName("Create booking exceeding hotel adult capacity throws IllegalArgumentException")
    void testCreateBookingExceedingCapacity() {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setFirstName("Alice");
        request.setLastName("Guest");
        request.setEmail("guest1_booking@test.com");
        request.setAdultCount(5); // Hotel max is 2
        request.setCheckIn(Instant.now().plus(5, ChronoUnit.DAYS));
        request.setCheckOut(Instant.now().plus(7, ChronoUnit.DAYS));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(hotel.getId(), request, guest1.getId());
        });
        assertTrue(ex.getMessage().contains("Adult count exceeds hotel capacity"));
    }

    @Test
    @DisplayName("Prevent overlapping reservations for the same user at the same hotel")
    void testPreventOverlappingReservations() {
        // First booking: days 10 to 14
        CreateBookingRequest request1 = createValidBookingRequest(guest1, 4);
        bookingService.createBooking(hotel.getId(), request1, guest1.getId());

        // Second booking by same user overlapping: days 12 to 16
        PaymentIntentResponse pi2 = bookingService.createPaymentIntent(
                hotel.getId(),
                new PaymentIntentRequest(4),
                guest1.getId()
        );
        CreateBookingRequest request2 = new CreateBookingRequest();
        request2.setFirstName("Alice");
        request2.setLastName("Guest");
        request2.setEmail("guest1_booking@test.com");
        request2.setAdultCount(2);
        request2.setCheckIn(Instant.now().plus(12, ChronoUnit.DAYS));
        request2.setCheckOut(Instant.now().plus(16, ChronoUnit.DAYS));
        request2.setTotalCost(pi2.getTotalCost());
        request2.setPaymentIntentId(pi2.getPaymentIntentId());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(hotel.getId(), request2, guest1.getId());
        });
        assertTrue(ex.getMessage().contains("already have an active reservation"));
    }

    @Test
    @DisplayName("Cancel upcoming booking by guest refunds and decrements stats")
    void testCancelBookingSuccess() {
        CreateBookingRequest req = createValidBookingRequest(guest1, 3);
        BookingDto created = bookingService.createBooking(hotel.getId(), req, guest1.getId());

        CancelBookingRequest cancelReq = new CancelBookingRequest("Trip postponed");
        CancelBookingResponse cancelRes = bookingService.cancelBooking(created.getId(), cancelReq, guest1.getId(), false);

        assertNotNull(cancelRes);
        assertEquals(BookingStatus.CANCELLED, cancelRes.getBooking().getStatus());
        assertEquals(PaymentStatus.REFUNDED, cancelRes.getBooking().getPaymentStatus());
        assertEquals(450.0, cancelRes.getRefundAmount());
        assertNotNull(cancelRes.getRefundSkipped());
        assertTrue(cancelRes.getRefundSkipped().contains("Simulated refund"));

        // Hotel stats decremented back to 0
        Hotel updatedHotel = hotelRepository.findById(hotel.getId()).orElseThrow();
        assertEquals(0, updatedHotel.getTotalBookings());
        assertEquals(0, BigDecimal.ZERO.compareTo(updatedHotel.getTotalRevenue()));
    }

    @Test
    @DisplayName("Stripe refund failure returns 502 Bad Gateway and keeps booking CONFIRMED & PAID")
    void testCancelBookingRefundFailureReturns502() {
        CreateBookingRequest req = createValidBookingRequest(guest1, 3);
        BookingDto created = bookingService.createBooking(hotel.getId(), req, guest1.getId());

        // Create a custom BookingService instance with a mocked PaymentGatewayService simulating failure
        PaymentGatewayService failingGateway = Mockito.mock(PaymentGatewayService.class);
        when(failingGateway.processRefund(any(), any(), any()))
                .thenReturn(RefundResult.failed("Stripe refund failed: account balance insufficient", false));

        BookingService serviceWithFailingGateway = new BookingService(
                bookingRepository,
                hotelRepository,
                userRepository,
                failingGateway
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            serviceWithFailingGateway.cancelBooking(created.getId(), new CancelBookingRequest("Fail test"), guest1.getId(), false);
        });

        assertEquals(HttpStatus.BAD_GATEWAY, ex.getStatusCode());
        assertTrue(ex.getReason().contains("account balance insufficient"));

        // Verify booking in database was NOT changed to CANCELLED or REFUNDED
        Booking unchanged = bookingRepository.findById(created.getId()).orElseThrow();
        assertEquals(BookingStatus.CONFIRMED, unchanged.getStatus());
        assertEquals(PaymentStatus.PAID, unchanged.getPaymentStatus());

        // Hotel stats remain counted
        Hotel updatedHotel = hotelRepository.findById(hotel.getId()).orElseThrow();
        assertEquals(1, updatedHotel.getTotalBookings());
        assertEquals(0, new BigDecimal("450.00").compareTo(updatedHotel.getTotalRevenue()));
    }

    @Test
    @DisplayName("Duplicate cancellation attempt is rejected with 400 Bad Request idempotently")
    void testDuplicateCancellationRejected() {
        CreateBookingRequest req = createValidBookingRequest(guest1, 3);
        BookingDto created = bookingService.createBooking(hotel.getId(), req, guest1.getId());

        // First cancellation succeeds
        bookingService.cancelBooking(created.getId(), new CancelBookingRequest("First cancel"), guest1.getId(), false);

        // Second cancellation attempt must be rejected
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            bookingService.cancelBooking(created.getId(), new CancelBookingRequest("Second cancel"), guest1.getId(), false);
        });
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Booking cannot be cancelled"));

        // Ensure hotel stats are not double-decremented below 0
        Hotel updatedHotel = hotelRepository.findById(hotel.getId()).orElseThrow();
        assertEquals(0, updatedHotel.getTotalBookings());
        assertEquals(0, BigDecimal.ZERO.compareTo(updatedHotel.getTotalRevenue()));
    }

    @Test
    @DisplayName("Refund failure can be retried successfully")
    void testRefundFailureCanBeRetried() {
        CreateBookingRequest req = createValidBookingRequest(guest1, 3);
        BookingDto created = bookingService.createBooking(hotel.getId(), req, guest1.getId());

        PaymentGatewayService mockGateway = Mockito.mock(PaymentGatewayService.class);
        // First call fails, second call succeeds
        when(mockGateway.processRefund(any(), any(), any()))
                .thenReturn(RefundResult.failed("Temporary Stripe outage", false))
                .thenReturn(RefundResult.success("re_123", new BigDecimal("450.00"), null, false));

        BookingService retryService = new BookingService(
                bookingRepository,
                hotelRepository,
                userRepository,
                mockGateway
        );

        // Attempt 1 fails with 502
        assertThrows(ResponseStatusException.class, () -> {
            retryService.cancelBooking(created.getId(), new CancelBookingRequest("Retry test"), guest1.getId(), false);
        });

        // Booking still paid and confirmed
        Booking bookingAfterFail = bookingRepository.findById(created.getId()).orElseThrow();
        assertEquals(BookingStatus.CONFIRMED, bookingAfterFail.getStatus());
        assertEquals(PaymentStatus.PAID, bookingAfterFail.getPaymentStatus());

        // Attempt 2 succeeds
        CancelBookingResponse retrySuccess = retryService.cancelBooking(
                created.getId(),
                new CancelBookingRequest("Retry test"),
                guest1.getId(),
                false
        );
        assertEquals(BookingStatus.CANCELLED, retrySuccess.getBooking().getStatus());
        assertEquals(PaymentStatus.REFUNDED, retrySuccess.getBooking().getPaymentStatus());
        assertEquals(450.0, retrySuccess.getRefundAmount());

        // Stats decremented once
        Hotel updatedHotel = hotelRepository.findById(hotel.getId()).orElseThrow();
        assertEquals(0, updatedHotel.getTotalBookings());
        assertEquals(0, BigDecimal.ZERO.compareTo(updatedHotel.getTotalRevenue()));
    }

    @Test
    @DisplayName("Cross-user access denied: another guest cannot cancel or view private booking")
    void testCrossUserAccessDenied() {
        CreateBookingRequest req = createValidBookingRequest(guest1, 3);
        BookingDto created = bookingService.createBooking(hotel.getId(), req, guest1.getId());

        // Guest 2 tries to view Guest 1's booking
        assertThrows(AccessDeniedException.class, () -> {
            bookingService.getBookingById(created.getId(), guest2.getId(), false);
        });

        // Guest 2 tries to cancel Guest 1's booking
        assertThrows(AccessDeniedException.class, () -> {
            bookingService.cancelBooking(created.getId(), new CancelBookingRequest(), guest2.getId(), false);
        });
    }

    @Test
    @DisplayName("Hotel owner can view and manage bookings for their hotel")
    void testHotelOwnerCanAccessBooking() {
        CreateBookingRequest req = createValidBookingRequest(guest1, 3);
        BookingDto created = bookingService.createBooking(hotel.getId(), req, guest1.getId());

        // Owner can view booking
        BookingDto fetched = bookingService.getBookingById(created.getId(), owner.getId(), false);
        assertNotNull(fetched);
        assertEquals(created.getId(), fetched.getId());

        // Owner can update payment status
        UpdateBookingPaymentRequest payReq = new UpdateBookingPaymentRequest("PAID", "bank_transfer");
        BookingDto payUpdated = bookingService.updateBookingPayment(created.getId(), payReq, owner.getId(), false);
        assertEquals(PaymentStatus.PAID, payUpdated.getPaymentStatus());
    }

    @Test
    @DisplayName("Get my bookings returns user's bookings with hotel details")
    void testGetMyBookings() {
        CreateBookingRequest req = createValidBookingRequest(guest1, 3);
        bookingService.createBooking(hotel.getId(), req, guest1.getId());

        List<HotelWithBookingsDto> myBookings = bookingService.getMyBookings(guest1.getId());
        assertEquals(1, myBookings.size());
        assertEquals("Venice Canal Boutique", myBookings.get(0).getName());
        assertEquals(1, myBookings.get(0).getBookings().size());
        assertEquals("Alice", myBookings.get(0).getBookings().get(0).getFirstName());
    }

    @Test
    @DisplayName("Monetary precision: booking and refund with £125.50 price per night")
    void testBookingPrecisionWith12550PricePerNight() {
        hotel.setPricePerNight(new BigDecimal("125.50"));
        hotelRepository.save(hotel);

        CreateBookingRequest req = createValidBookingRequest(guest1, 1);
        assertEquals(new BigDecimal("125.50"), req.getTotalCost());

        BookingDto created = bookingService.createBooking(hotel.getId(), req, guest1.getId());
        assertEquals(new BigDecimal("125.50"), created.getTotalCost());
        assertEquals(BookingStatus.CONFIRMED, created.getStatus());
        assertEquals(PaymentStatus.PAID, created.getPaymentStatus());

        CancelBookingResponse cancelRes = bookingService.cancelBooking(
                created.getId(),
                new CancelBookingRequest("Change of plan"),
                guest1.getId(),
                false
        );
        assertEquals(125.50, cancelRes.getRefundAmount());
        assertEquals(BookingStatus.CANCELLED, cancelRes.getBooking().getStatus());
        assertEquals(PaymentStatus.REFUNDED, cancelRes.getBooking().getPaymentStatus());
    }

    @Test
    @DisplayName("Monetary precision: booking and refund with £99.99 price per night for 2 nights (£199.98)")
    void testBookingPrecisionWith9999PricePerNight() {
        hotel.setPricePerNight(new BigDecimal("99.99"));
        hotelRepository.save(hotel);

        CreateBookingRequest req = createValidBookingRequest(guest1, 2);
        assertEquals(new BigDecimal("199.98"), req.getTotalCost());

        BookingDto created = bookingService.createBooking(hotel.getId(), req, guest1.getId());
        assertEquals(new BigDecimal("199.98"), created.getTotalCost());

        CancelBookingResponse cancelRes = bookingService.cancelBooking(
                created.getId(),
                new CancelBookingRequest("Change of plan"),
                guest1.getId(),
                false
        );
        assertEquals(199.98, cancelRes.getRefundAmount());
    }
}
