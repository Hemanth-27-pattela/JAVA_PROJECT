package com.hotel.desktop.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.desktop.util.JsonUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookingModelTest {

    private final ObjectMapper mapper = JsonUtils.getMapper();

    @Test
    @DisplayName("BookingStatus deserializes case-insensitively and returns fallback")
    void testBookingStatusEnum() {
        assertEquals(BookingStatus.CONFIRMED, BookingStatus.fromValue("confirmed"));
        assertEquals(BookingStatus.CONFIRMED, BookingStatus.fromValue("CONFIRMED"));
        assertEquals(BookingStatus.CANCELLED, BookingStatus.fromValue("cancelled"));
        assertEquals(BookingStatus.PENDING, BookingStatus.fromValue("pending"));
        assertEquals(BookingStatus.COMPLETED, BookingStatus.fromValue("completed"));
        assertEquals(BookingStatus.REFUNDED, BookingStatus.fromValue("refunded"));
        assertEquals(BookingStatus.PENDING, BookingStatus.fromValue("unknown_status"));
        assertEquals(BookingStatus.PENDING, BookingStatus.fromValue(null));
    }

    @Test
    @DisplayName("PaymentStatus deserializes case-insensitively and returns fallback")
    void testPaymentStatusEnum() {
        assertEquals(PaymentStatus.PAID, PaymentStatus.fromValue("paid"));
        assertEquals(PaymentStatus.PAID, PaymentStatus.fromValue("PAID"));
        assertEquals(PaymentStatus.FAILED, PaymentStatus.fromValue("failed"));
        assertEquals(PaymentStatus.REFUNDED, PaymentStatus.fromValue("refunded"));
        assertEquals(PaymentStatus.PENDING, PaymentStatus.fromValue("pending"));
        assertEquals(PaymentStatus.PENDING, PaymentStatus.fromValue(null));
    }

    @Test
    @DisplayName("Booking JSON deserializes properly with _id alias, Instant dates, and DTO fields")
    void testBookingDeserialization() throws Exception {
        String json = """
            {
              "_id": "bk-999",
              "userId": "user-123",
              "hotelId": "hotel-456",
              "firstName": "John",
              "lastName": "Doe",
              "email": "john.doe@example.com",
              "phone": "+44 7700 900123",
              "adultCount": 2,
              "childCount": 1,
              "checkIn": "2026-11-20T14:00:00Z",
              "checkOut": "2026-11-23T10:00:00Z",
              "totalCost": 495.00,
              "status": "confirmed",
              "paymentStatus": "paid",
              "specialRequests": "High floor room preferred",
              "createdAt": "2026-11-01T12:00:00Z"
            }
            """;

        Booking booking = mapper.readValue(json, Booking.class);

        assertNotNull(booking);
        assertEquals("bk-999", booking.getId());
        assertEquals("user-123", booking.getUserId());
        assertEquals("hotel-456", booking.getHotelId());
        assertEquals("John Doe", booking.getFullName());
        assertEquals(2, booking.getAdultCount());
        assertEquals(1, booking.getChildCount());
        assertEquals(3, booking.getNightsCount());
        assertEquals(new BigDecimal("495.00"), booking.getTotalCost());
        assertEquals("£495.00", booking.getFormattedTotalCost());
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        assertEquals(PaymentStatus.PAID, booking.getPaymentStatus());
        assertEquals("High floor room preferred", booking.getSpecialRequests());
    }

    @Test
    @DisplayName("Booking isCancellable correctly evaluates future vs past dates and active statuses")
    void testBookingCancellableLogic() {
        Booking futureConfirmed = new Booking();
        futureConfirmed.setStatus(BookingStatus.CONFIRMED);
        futureConfirmed.setCheckIn(Instant.now().plus(5, ChronoUnit.DAYS));
        futureConfirmed.setCheckOut(Instant.now().plus(8, ChronoUnit.DAYS));
        assertTrue(futureConfirmed.isCancellable());

        Booking futurePending = new Booking();
        futurePending.setStatus(BookingStatus.PENDING);
        futurePending.setCheckIn(Instant.now().plus(3, ChronoUnit.DAYS));
        futurePending.setCheckOut(Instant.now().plus(5, ChronoUnit.DAYS));
        assertTrue(futurePending.isCancellable());

        Booking pastConfirmed = new Booking();
        pastConfirmed.setStatus(BookingStatus.CONFIRMED);
        pastConfirmed.setCheckIn(Instant.now().minus(5, ChronoUnit.DAYS));
        pastConfirmed.setCheckOut(Instant.now().minus(2, ChronoUnit.DAYS));
        assertFalse(pastConfirmed.isCancellable());

        Booking alreadyCancelled = new Booking();
        alreadyCancelled.setStatus(BookingStatus.CANCELLED);
        alreadyCancelled.setCheckIn(Instant.now().plus(5, ChronoUnit.DAYS));
        assertFalse(alreadyCancelled.isCancellable());

        Booking completed = new Booking();
        completed.setStatus(BookingStatus.COMPLETED);
        completed.setCheckIn(Instant.now().minus(2, ChronoUnit.DAYS));
        assertFalse(completed.isCancellable());
        assertTrue(completed.isPastOrCompleted());
    }

    @Test
    @DisplayName("HotelWithBookings properly inherits hotel metadata and propagates to bookings")
    void testHotelWithBookingsPropagation() throws Exception {
        String json = """
            {
              "_id": "h-london-1",
              "name": "Thames View Boutique",
              "city": "London",
              "country": "United Kingdom",
              "pricePerNight": 165.00,
              "starRating": 4,
              "bookings": [
                {
                  "_id": "b-1",
                  "checkIn": "2026-12-01T14:00:00Z",
                  "checkOut": "2026-12-03T10:00:00Z",
                  "totalCost": 330.00,
                  "status": "confirmed",
                  "paymentStatus": "paid"
                }
              ]
            }
            """;

        HotelWithBookings hotelWithBookings = mapper.readValue(json, HotelWithBookings.class);

        assertNotNull(hotelWithBookings);
        assertEquals("h-london-1", hotelWithBookings.getId());
        assertEquals("Thames View Boutique", hotelWithBookings.getName());
        assertEquals("London, United Kingdom", hotelWithBookings.getLocationDisplay());

        List<Booking> bookings = hotelWithBookings.getBookings();
        assertEquals(1, bookings.size());

        Booking b = bookings.get(0);
        assertEquals("b-1", b.getId());
        assertEquals("Thames View Boutique", b.getHotelName());
        assertEquals("London, United Kingdom", b.getHotelLocation());
    }

    @Test
    @DisplayName("PaymentIntent and Cancel models serialize and deserialize correctly")
    void testPaymentAndCancelModels() throws Exception {
        PaymentIntentRequest req = new PaymentIntentRequest(3);
        String reqJson = mapper.writeValueAsString(req);
        assertTrue(reqJson.contains("\"numberOfNights\":3"));

        String respJson = """
            {
              "paymentIntentId": "pi_sim_123456",
              "clientSecret": "secret_abc",
              "totalCost": 495.00
            }
            """;
        PaymentIntentResponse resp = mapper.readValue(respJson, PaymentIntentResponse.class);
        assertEquals("pi_sim_123456", resp.getPaymentIntentId());
        assertEquals("secret_abc", resp.getClientSecret());
        assertEquals(new BigDecimal("495.00"), resp.getTotalCost());

        CancelBookingRequest cancelReq = new CancelBookingRequest("Trip postponed");
        String cancelReqJson = mapper.writeValueAsString(cancelReq);
        assertTrue(cancelReqJson.contains("Trip postponed"));

        String cancelRespJson = """
            {
              "booking": {
                "_id": "bk-100",
                "status": "cancelled",
                "paymentStatus": "refunded"
              },
              "refundAmount": 495.00
            }
            """;
        CancelBookingResponse cancelResp = mapper.readValue(cancelRespJson, CancelBookingResponse.class);
        assertNotNull(cancelResp.getBooking());
        assertEquals("bk-100", cancelResp.getBooking().getId());
        assertEquals(BookingStatus.CANCELLED, cancelResp.getBooking().getStatus());
        assertEquals(495.00, cancelResp.getRefundAmount());
        assertEquals("£495.00", cancelResp.getFormattedRefundAmount());
    }
}
