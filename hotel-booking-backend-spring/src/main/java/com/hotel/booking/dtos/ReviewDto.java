package com.hotel.booking.dtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hotel.booking.entities.Review;
import java.time.Instant;

public class ReviewDto {

    @JsonProperty("_id")
    private String id;

    private String userId;
    private String hotelId;
    private String bookingId;
    private Integer rating = 5;
    private String comment;
    private ReviewCategoriesDto categories = new ReviewCategoriesDto();
    private Boolean isVerified = false;
    private Integer helpfulCount = 0;
    private Instant createdAt;
    private Instant updatedAt;

    public ReviewDto() {
    }

    @JsonProperty("_id")
    public String getId() {
        return id;
    }

    @JsonProperty("_id")
    @JsonAlias("id")
    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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
        this.categories = categories != null ? categories : new ReviewCategoriesDto();
    }

    public Boolean getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(Boolean verified) {
        isVerified = verified;
    }

    public Integer getHelpfulCount() {
        return helpfulCount;
    }

    public void setHelpfulCount(Integer helpfulCount) {
        this.helpfulCount = helpfulCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static ReviewDto fromEntity(Review review) {
        if (review == null) {
            return null;
        }
        ReviewDto dto = new ReviewDto();
        dto.setId(review.getId());
        dto.setUserId(review.getUserId());
        dto.setHotelId(review.getHotelId());
        dto.setBookingId(review.getBookingId());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        if (review.getCategories() != null) {
            ReviewCategoriesDto catDto = new ReviewCategoriesDto(
                    review.getCategories().getCleanliness(),
                    review.getCategories().getService(),
                    review.getCategories().getLocation(),
                    review.getCategories().getValue(),
                    review.getCategories().getAmenities()
            );
            dto.setCategories(catDto);
        }
        dto.setIsVerified(review.getIsVerified());
        dto.setHelpfulCount(review.getHelpfulCount());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setUpdatedAt(review.getUpdatedAt());
        return dto;
    }
}

