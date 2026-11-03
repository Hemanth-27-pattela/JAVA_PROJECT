package com.hotel.booking.controllers;

import com.hotel.booking.dtos.HotelWithBookingsDto;
import com.hotel.booking.security.UserPrincipal;
import com.hotel.booking.services.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/my-bookings")
public class MyBookingController {

    private final BookingService bookingService;

    public MyBookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public ResponseEntity<List<HotelWithBookingsDto>> getMyBookings(@AuthenticationPrincipal UserPrincipal principal) {
        List<HotelWithBookingsDto> bookings = bookingService.getMyBookings(principal.getId());
        return ResponseEntity.ok(bookings);
    }
}

