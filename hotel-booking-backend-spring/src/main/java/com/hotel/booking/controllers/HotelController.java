package com.hotel.booking.controllers;

import com.hotel.booking.dtos.CreateBookingRequest;
import com.hotel.booking.dtos.HotelActiveStatusRequest;
import com.hotel.booking.dtos.HotelDto;
import com.hotel.booking.dtos.HotelSearchParams;
import com.hotel.booking.dtos.HotelSearchResponse;
import com.hotel.booking.dtos.PaymentIntentRequest;
import com.hotel.booking.dtos.PaymentIntentResponse;
import com.hotel.booking.security.UserPrincipal;
import com.hotel.booking.services.BookingService;
import com.hotel.booking.services.HotelService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/hotels")
public class HotelController {

    private final HotelService hotelService;
    private final BookingService bookingService;

    public HotelController(HotelService hotelService, BookingService bookingService) {
        this.hotelService = hotelService;
        this.bookingService = bookingService;
    }

    @GetMapping("/search")
    public ResponseEntity<HotelSearchResponse> searchHotels(
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) String checkIn,
            @RequestParam(required = false) String checkOut,
            @RequestParam(required = false) Integer adultCount,
            @RequestParam(required = false) Integer childCount,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String sortOption,
            @RequestParam(required = false) List<String> facilities,
            @RequestParam(required = false) List<String> types,
            @RequestParam(required = false) List<Integer> stars
    ) {
        HotelSearchParams params = new HotelSearchParams();
        params.setDestination(destination);
        params.setCheckIn(checkIn);
        params.setCheckOut(checkOut);
        params.setAdultCount(adultCount);
        params.setChildCount(childCount);
        params.setPage(page);
        params.setMaxPrice(maxPrice);
        params.setSortOption(sortOption);
        params.setFacilities(facilities);
        params.setTypes(types);
        params.setStars(stars);

        HotelSearchResponse response = hotelService.searchHotels(params);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<HotelDto>> getAllHotels() {
        return ResponseEntity.ok(hotelService.getAllHotels());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HotelDto> getHotelById(@PathVariable String id) {
        return ResponseEntity.ok(hotelService.getHotelById(id));
    }

    @PatchMapping("/{id}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HotelDto> updateHotelActive(
            @PathVariable String id,
            @Valid @RequestBody HotelActiveStatusRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        HotelDto updated = hotelService.updateHotelActiveStatus(id, request.getIsActive(), principal.getId(), true);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{hotelId}/bookings/payment-intent")
    public ResponseEntity<PaymentIntentResponse> createPaymentIntent(
            @PathVariable String hotelId,
            @RequestBody(required = false) PaymentIntentRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        PaymentIntentResponse response = bookingService.createPaymentIntent(hotelId, request, principal.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{hotelId}/bookings")
    public ResponseEntity<Void> createBooking(
            @PathVariable String hotelId,
            @Valid @RequestBody CreateBookingRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        bookingService.createBooking(hotelId, request, principal.getId());
        return ResponseEntity.ok().build();
    }
}

