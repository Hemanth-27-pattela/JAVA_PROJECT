package com.hotel.desktop.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.desktop.model.Booking;
import com.hotel.desktop.model.BookingStatus;
import com.hotel.desktop.model.CancelBookingResponse;
import com.hotel.desktop.model.CreateBookingRequest;
import com.hotel.desktop.model.HotelWithBookings;
import com.hotel.desktop.model.PaymentIntentResponse;
import com.hotel.desktop.model.PaymentStatus;
import com.hotel.desktop.util.JsonUtils;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookingServiceTest {

    private HttpServer server;
    private String serverBaseUrl;
    private SessionManager sessionManager;
    private ObjectMapper objectMapper;
    private HttpClient realHttpClient;
    private ApiClient apiClient;
    private BookingService bookingService;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        int port = server.getAddress().getPort();
        serverBaseUrl = "http://127.0.0.1:" + port;

        sessionManager = SessionManager.getInstance();
        sessionManager.clearSession();
        sessionManager.setSession("test-guest-token", null);
        objectMapper = JsonUtils.getMapper();

        realHttpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();

        apiClient = new ApiClient(serverBaseUrl, realHttpClient, objectMapper, sessionManager);
        bookingService = new BookingService(apiClient);
        server.start();
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
        if (sessionManager != null) {
            sessionManager.clearSession();
        }
    }

    @Test
    @DisplayName("createPaymentIntent sends request and returns PaymentIntentResponse")
    void testCreatePaymentIntent() throws Exception {
        server.createContext("/api/hotels/hotel-1/bookings/payment-intent", exchange -> {
            assertEquals("POST", exchange.getRequestMethod());
            assertTrue(exchange.getRequestHeaders().getFirst("Authorization").startsWith("Bearer "));

            String responseJson = """
                {
                  "paymentIntentId": "pi_sim_999888",
                  "clientSecret": "pi_sim_999888_secret",
                  "totalCost": 495.00
                }
                """;
            byte[] bytes = responseJson.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });

        PaymentIntentResponse resp = bookingService.createPaymentIntent("hotel-1", 3);
        assertNotNull(resp);
        assertEquals("pi_sim_999888", resp.getPaymentIntentId());
        assertEquals("pi_sim_999888_secret", resp.getClientSecret());
        assertEquals(new BigDecimal("495.00"), resp.getTotalCost());
    }

    @Test
    @DisplayName("createBooking sends full reservation payload and succeeds")
    void testCreateBookingSuccess() throws Exception {
        server.createContext("/api/hotels/hotel-1/bookings", exchange -> {
            assertEquals("POST", exchange.getRequestMethod());
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(body.contains("pi_sim_999888"));
            assertTrue(body.contains("john.doe@example.com"));

            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });

        CreateBookingRequest req = new CreateBookingRequest();
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setEmail("john.doe@example.com");
        req.setAdultCount(2);
        req.setChildCount(0);
        req.setCheckIn(Instant.parse("2026-11-20T14:00:00Z"));
        req.setCheckOut(Instant.parse("2026-11-23T10:00:00Z"));
        req.setTotalCost(new BigDecimal("495.00"));
        req.setPaymentIntentId("pi_sim_999888");

        bookingService.createBooking("hotel-1", req);
    }

    @Test
    @DisplayName("createBooking propagates overlapping booking error from backend")
    void testCreateBookingOverlappingError() {
        server.createContext("/api/hotels/hotel-1/bookings", exchange -> {
            String errorJson = """
                {
                  "message": "You already have an active reservation for this hotel during the selected dates"
                }
                """;
            byte[] bytes = errorJson.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(400, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });

        CreateBookingRequest req = new CreateBookingRequest();
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setEmail("john.doe@example.com");
        req.setAdultCount(2);
        req.setChildCount(0);
        req.setCheckIn(Instant.parse("2026-11-20T14:00:00Z"));
        req.setCheckOut(Instant.parse("2026-11-23T10:00:00Z"));
        req.setTotalCost(new BigDecimal("495.00"));
        req.setPaymentIntentId("pi_sim_999888");

        ApiException ex = assertThrows(ApiException.class, () -> bookingService.createBooking("hotel-1", req));
        assertEquals(400, ex.getStatusCode());
        assertTrue(ex.getMessage().contains("You already have an active reservation"));
    }

    @Test
    @DisplayName("getMyBookings retrieves user reservations grouped by hotel")
    void testGetMyBookings() throws Exception {
        server.createContext("/api/my-bookings", exchange -> {
            assertEquals("GET", exchange.getRequestMethod());
            String responseJson = """
                [
                  {
                    "_id": "h-london",
                    "name": "Thames View Boutique",
                    "city": "London",
                    "country": "United Kingdom",
                    "pricePerNight": 165.00,
                    "starRating": 4,
                    "bookings": [
                      {
                        "_id": "bk-001",
                        "firstName": "Guest",
                        "lastName": "Traveler",
                        "email": "guest@hotel.com",
                        "adultCount": 2,
                        "childCount": 0,
                        "checkIn": "2026-12-01T14:00:00Z",
                        "checkOut": "2026-12-04T10:00:00Z",
                        "totalCost": 495.00,
                        "status": "confirmed",
                        "paymentStatus": "paid"
                      }
                    ]
                  }
                ]
                """;
            byte[] bytes = responseJson.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });

        List<HotelWithBookings> result = bookingService.getMyBookings();
        assertNotNull(result);
        assertEquals(1, result.size());

        HotelWithBookings h = result.get(0);
        assertEquals("h-london", h.getId());
        assertEquals("Thames View Boutique", h.getName());
        assertEquals(1, h.getBookings().size());

        Booking b = h.getBookings().get(0);
        assertEquals("bk-001", b.getId());
        assertEquals("Thames View Boutique", b.getHotelName());
        assertEquals(BookingStatus.CONFIRMED, b.getStatus());
        assertEquals(PaymentStatus.PAID, b.getPaymentStatus());
    }

    @Test
    @DisplayName("getBookingById retrieves single booking details")
    void testGetBookingById() throws Exception {
        server.createContext("/api/bookings/bk-123", exchange -> {
            assertEquals("GET", exchange.getRequestMethod());
            String json = """
                {
                  "_id": "bk-123",
                  "userId": "user-1",
                  "hotelId": "hotel-1",
                  "firstName": "Jane",
                  "lastName": "Smith",
                  "email": "jane@example.com",
                  "totalCost": 350.00,
                  "status": "confirmed",
                  "paymentStatus": "paid"
                }
                """;
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });

        Booking b = bookingService.getBookingById("bk-123");
        assertNotNull(b);
        assertEquals("bk-123", b.getId());
        assertEquals("Jane Smith", b.getFullName());
        assertEquals(new BigDecimal("350.00"), b.getTotalCost());
    }

    @Test
    @DisplayName("cancelBooking sends cancellation request and receives refund response")
    void testCancelBookingSuccess() throws Exception {
        server.createContext("/api/bookings/bk-123/cancel", exchange -> {
            assertEquals("POST", exchange.getRequestMethod());
            String json = """
                {
                  "booking": {
                    "_id": "bk-123",
                    "status": "cancelled",
                    "paymentStatus": "refunded",
                    "totalCost": 495.00
                  },
                  "refundAmount": 495.00
                }
                """;
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });

        CancelBookingResponse resp = bookingService.cancelBooking("bk-123", "Plans changed");
        assertNotNull(resp);
        assertNotNull(resp.getBooking());
        assertEquals(BookingStatus.CANCELLED, resp.getBooking().getStatus());
        assertEquals(PaymentStatus.REFUNDED, resp.getBooking().getPaymentStatus());
        assertEquals(495.00, resp.getRefundAmount());
    }
}

