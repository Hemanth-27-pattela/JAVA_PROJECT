package com.hotel.booking.controllers;

import com.hotel.booking.dtos.BookingDto;
import com.hotel.booking.dtos.CancelBookingRequest;
import com.hotel.booking.dtos.CancelBookingResponse;
import com.hotel.booking.dtos.MessageResponse;
import com.hotel.booking.dtos.UpdateBookingPaymentRequest;
import com.hotel.booking.dtos.UpdateBookingStatusRequest;
import com.hotel.booking.security.UserPrincipal;
import com.hotel.booking.services.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    // Get all bookings (admin only)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BookingDto>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    // Get bookings by hotel ID (for hotel owners or admin)
    @GetMapping("/hotel/{hotelId}")
    public ResponseEntity<List<BookingDto>> getBookingsByHotelId(
            @PathVariable String hotelId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(bookingService.getBookingsByHotelId(hotelId, principal.getId(), isAdmin));
    }

    // Get booking by ID (guest, hotel owner, or admin)
    @GetMapping("/{id}")
    public ResponseEntity<BookingDto> getBookingById(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(bookingService.getBookingById(id, principal.getId(), isAdmin));
    }

    // Cancel booking (guest, hotel owner, or admin)
    @PostMapping("/{id}/cancel")
    public ResponseEntity<CancelBookingResponse> cancelBooking(
            @PathVariable String id,
            @RequestBody(required = false) CancelBookingRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        CancelBookingResponse response = bookingService.cancelBooking(id, request, principal.getId(), isAdmin);
        return ResponseEntity.ok(response);
    }

    // Update booking status (owner or admin only)
    @PatchMapping("/{id}/status")
    public ResponseEntity<BookingDto> updateBookingStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateBookingStatusRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        BookingDto updated = bookingService.updateBookingStatus(id, request, principal.getId(), isAdmin);
        return ResponseEntity.ok(updated);
    }

    // Update payment status (owner or admin only)
    @PatchMapping("/{id}/payment")
    public ResponseEntity<BookingDto> updateBookingPayment(
            @PathVariable String id,
            @Valid @RequestBody UpdateBookingPaymentRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        BookingDto updated = bookingService.updateBookingPayment(id, request, principal.getId(), isAdmin);
        return ResponseEntity.ok(updated);
    }

    // Delete booking (admin only)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteBooking(@PathVariable String id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.ok(new MessageResponse("Booking deleted successfully"));
    }
}

