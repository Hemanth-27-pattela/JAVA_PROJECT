package com.hotel.booking.dtos.businessinsights;

public class HotelsByStarDto {
    private Integer starRating = 0;
    private Long count = 0L;

    public HotelsByStarDto() {
    }

    public HotelsByStarDto(Integer starRating, Long count) {
        this.starRating = starRating;
        this.count = count;
    }

    public Integer getStarRating() { return starRating; }
    public void setStarRating(Integer starRating) { this.starRating = starRating; }

    public Long getCount() { return count; }
    public void setCount(Long count) { this.count = count; }
}

