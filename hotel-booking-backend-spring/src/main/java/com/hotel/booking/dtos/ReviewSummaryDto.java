package com.hotel.booking.dtos;

public class ReviewSummaryDto {

    private String hotelId;
    private Double averageRating;
    private Integer reviewCount;

    public ReviewSummaryDto() {
    }

    public ReviewSummaryDto(String hotelId, Double averageRating, Integer reviewCount) {
        this.hotelId = hotelId;
        this.averageRating = averageRating;
        this.reviewCount = reviewCount;
    }

    public String getHotelId() {
        return hotelId;
    }

    public void setHotelId(String hotelId) {
        this.hotelId = hotelId;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Integer getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(Integer reviewCount) {
        this.reviewCount = reviewCount;
    }
}

