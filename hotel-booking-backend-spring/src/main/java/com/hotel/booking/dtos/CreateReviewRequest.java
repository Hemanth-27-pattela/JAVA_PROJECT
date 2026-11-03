package com.hotel.booking.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateReviewRequest {

    @NotBlank(message = "Hotel ID is required")
    private String hotelId;

    @NotBlank(message = "Booking ID is required")
    private String bookingId;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer rating;

    @NotBlank(message = "Comment is required")
    @Size(min = 3, message = "Comment must be at least 3 characters")
    private String comment;

    @NotNull(message = "Categories are required")
    @Valid
    private ReviewCategoriesDto categories;

    public CreateReviewRequest() {
    }

    public CreateReviewRequest(String hotelId, String bookingId, Integer rating, String comment, ReviewCategoriesDto categories) {
        this.hotelId = hotelId;
        this.bookingId = bookingId;
        this.rating = rating;
        this.comment = comment;
        this.categories = categories;
    }

    public String getHotelId() {
        return hotelId;
    }

    public void setHotelId(String hotelId) {
        this.hotelId = hotelId;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public ReviewCategoriesDto getCategories() {
        return categories;
    }

    public void setCategories(ReviewCategoriesDto categories) {
        this.categories = categories;
    }
}

