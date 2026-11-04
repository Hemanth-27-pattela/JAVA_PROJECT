package com.hotel.desktop.service;

import com.hotel.desktop.model.Booking;
import com.hotel.desktop.model.CancelBookingRequest;
import com.hotel.desktop.model.CancelBookingResponse;
import com.hotel.desktop.model.CreateBookingRequest;
import com.hotel.desktop.model.HotelWithBookings;
import com.hotel.desktop.model.PaymentIntentRequest;
import com.hotel.desktop.model.PaymentIntentResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Client service interacting with Spring Boot booking and payment endpoints.
 * Provides booking intent initialization, reservation creation, user booking queries,
 * booking details retrieval, and booking cancellation.
 */
public class BookingService {

    private final ApiClient apiClient;

    public BookingService() {
        this(new ApiClient());
    }

    public BookingService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Initializes a simulated payment intent for the specified hotel and duration.
     *
     * @param hotelId        the ID of the hotel to book
     * @param numberOfNights duration in nights (>= 1)
     * @return PaymentIntentResponse containing intentId, clientSecret, and totalCost
     * @throws ApiException on communication or validation error
     */
    public PaymentIntentResponse createPaymentIntent(String hotelId, int numberOfNights) throws ApiException {
        if (hotelId == null || hotelId.trim().isEmpty()) {
            throw new ApiException(400, "Hotel ID cannot be empty");
        }
        int nights = Math.max(1, numberOfNights);
        String endpoint = "/api/hotels/" + hotelId.trim() + "/bookings/payment-intent";
        PaymentIntentRequest request = new PaymentIntentRequest(nights);
        return apiClient.post(endpoint, request, PaymentIntentResponse.class, true);
    }

    /**
     * Submits a confirmed hotel booking to the backend.
     *
     * @param hotelId the ID of the hotel to book
     * @param request the reservation details and verified paymentIntentId
     * @throws ApiException on conflict (e.g. overlapping booking) or validation failure
     */
    public void createBooking(String hotelId, CreateBookingRequest request) throws ApiException {
        if (hotelId == null || hotelId.trim().isEmpty()) {
            throw new ApiException(400, "Hotel ID cannot be empty");
        }
        if (request == null) {
            throw new ApiException(400, "Booking request cannot be null");
        }
        String endpoint = "/api/hotels/" + hotelId.trim() + "/bookings";
        apiClient.post(endpoint, request, Void.class, true);
    }

    /**
     * Retrieves all bookings made by the currently authenticated user, grouped by hotel.
     *
     * @return list of hotels with user reservations
     * @throws ApiException on unauthorized or network failure
     */
    public List<HotelWithBookings> getMyBookings() throws ApiException {
        HotelWithBookings[] array = apiClient.get("/api/my-bookings", HotelWithBookings[].class, true);
        if (array == null || array.length == 0) {
            return Collections.emptyList();
        }
        return new ArrayList<>(Arrays.asList(array));
    }

    /**
     * Retrieves a single booking by its unique identifier.
     *
     * @param bookingId booking ID
     * @return full Booking details
     * @throws ApiException on 404 Not Found or 403 Forbidden
     */
    public Booking getBookingById(String bookingId) throws ApiException {
        if (bookingId == null || bookingId.trim().isEmpty()) {
            throw new ApiException(400, "Booking ID cannot be empty");
        }
        String endpoint = "/api/bookings/" + bookingId.trim();
        return apiClient.get(endpoint, Booking.class, true);
    }

    /**
     * Cancels an existing booking and requests a refund.
     *
     * @param bookingId booking ID
     * @param reason    optional cancellation reason
     * @return CancelBookingResponse containing updated booking and refund info
     * @throws ApiException if booking cannot be cancelled (past check-in or invalid status)
     */
    public CancelBookingResponse cancelBooking(String bookingId, String reason) throws ApiException {
        if (bookingId == null || bookingId.trim().isEmpty()) {
            throw new ApiException(400, "Booking ID cannot be empty");
        }
        String endpoint = "/api/bookings/" + bookingId.trim() + "/cancel";
        CancelBookingRequest request = new CancelBookingRequest(reason != null ? reason.trim() : null);
        return apiClient.post(endpoint, request, CancelBookingResponse.class, true);
    }
}
