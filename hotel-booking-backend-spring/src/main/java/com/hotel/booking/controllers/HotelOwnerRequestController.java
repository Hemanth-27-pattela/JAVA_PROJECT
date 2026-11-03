package com.hotel.booking.controllers;

import com.hotel.booking.dtos.ownerrequest.CreateOwnerRequestDto;
import com.hotel.booking.dtos.ownerrequest.OwnerRequestResponseDto;
import com.hotel.booking.dtos.ownerrequest.ReviewOwnerRequestDto;
import com.hotel.booking.entities.enums.RequestStatus;
import com.hotel.booking.security.UserPrincipal;
import com.hotel.booking.services.HotelOwnerRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/owner-requests")
@Tag(name = "Hotel Owner Requests", description = "Hotel owner access request and admin review lifecycle")
public class HotelOwnerRequestController {

    private final HotelOwnerRequestService requestService;

    public HotelOwnerRequestController(HotelOwnerRequestService requestService) {
        this.requestService = requestService;
    }

    /**
     * Submit a hotel-owner access request (Authenticated users with USER role).
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Submit owner access request", description = "Submit a request to become a verified hotel owner")
    public ResponseEntity<OwnerRequestResponseDto> submitRequest(
            @Valid @RequestBody CreateOwnerRequestDto dto,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        OwnerRequestResponseDto response = requestService.submitRequest(principal.getId(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get calling user's request history.
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user's owner requests", description = "Retrieve list of owner requests for the current user")
    public ResponseEntity<List<OwnerRequestResponseDto>> getMyRequests(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(requestService.getMyRequests(principal.getId()));
    }

    /**
     * Check if the calling user has a currently pending owner request.
     */
    @GetMapping("/me/pending")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current pending request", description = "Retrieve active pending request if one exists")
    public ResponseEntity<OwnerRequestResponseDto> getMyPendingRequest(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return requestService.getMyPendingRequest(principal.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    /**
     * Admin: List all owner requests (optional filter by status).
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List owner requests (Admin only)", description = "Retrieve all owner access requests across the system")
    public ResponseEntity<List<OwnerRequestResponseDto>> getAllRequests(
            @RequestParam(value = "status", required = false) RequestStatus status
    ) {
        return ResponseEntity.ok(requestService.getAllRequests(status));
    }

    /**
     * Admin: Approve a pending owner request. Promotes user to HOTEL_OWNER.
     */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Approve owner request (Admin only)", description = "Approve request and elevate user to HOTEL_OWNER")
    public ResponseEntity<OwnerRequestResponseDto> approveRequest(
            @PathVariable("id") String id,
            @RequestBody(required = false) ReviewOwnerRequestDto reviewDto,
            @AuthenticationPrincipal UserPrincipal adminPrincipal
    ) {
        String adminNotes = reviewDto != null ? reviewDto.getAdminNotes() : null;
        OwnerRequestResponseDto response = requestService.approveRequest(id, adminPrincipal.getId(), adminNotes);
        return ResponseEntity.ok(response);
    }

    /**
     * Admin: Reject a pending owner request.
     */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reject owner request (Admin only)", description = "Reject owner request with a stated reason")
    public ResponseEntity<OwnerRequestResponseDto> rejectRequest(
            @PathVariable("id") String id,
            @RequestBody(required = false) ReviewOwnerRequestDto reviewDto,
            @AuthenticationPrincipal UserPrincipal adminPrincipal
    ) {
        String rejectionReason = reviewDto != null ? reviewDto.getRejectionReason() : null;
        String adminNotes = reviewDto != null ? reviewDto.getAdminNotes() : null;
        OwnerRequestResponseDto response = requestService.rejectRequest(id, adminPrincipal.getId(), rejectionReason, adminNotes);
        return ResponseEntity.ok(response);
    }
}

