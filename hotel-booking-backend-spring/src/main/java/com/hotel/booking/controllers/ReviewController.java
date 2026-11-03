package com.hotel.booking.controllers;

import com.hotel.booking.dtos.CreateReviewRequest;
import com.hotel.booking.dtos.ReviewDto;
import com.hotel.booking.dtos.ReviewSummaryDto;
import com.hotel.booking.security.UserPrincipal;
import com.hotel.booking.services.ReviewService;
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
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * Admin: global review list (newest first).
     * GET /api/reviews
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReviewDto>> getAllReviews(
            @RequestParam(required = false, defaultValue = "100") Integer limit
    ) {
        return ResponseEntity.ok(reviewService.getAllReviews(limit));
    }

    /**
     * Public: list reviews for a hotel (newest first).
     * GET /api/reviews/hotel/{hotelId}
     */
    @GetMapping("/hotel/{hotelId}")
    public ResponseEntity<List<ReviewDto>> getHotelReviews(@PathVariable String hotelId) {
        return ResponseEntity.ok(reviewService.getReviewsByHotelId(hotelId));
    }

    /**
     * Public: aggregate rating and review count for a hotel.
     * GET /api/reviews/hotel/{hotelId}/summary
     */
    @GetMapping("/hotel/{hotelId}/summary")
    public ResponseEntity<ReviewSummaryDto> getHotelReviewSummary(@PathVariable String hotelId) {
        return ResponseEntity.ok(reviewService.getReviewSummary(hotelId));
    }

    /**
     * Authenticated: create a review for a completed/confirmed booking at a hotel.
     * POST /api/reviews
     */
    @PostMapping
    public ResponseEntity<ReviewDto> createReview(
            @Valid @RequestBody CreateReviewRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        ReviewDto created = reviewService.createReview(request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}

