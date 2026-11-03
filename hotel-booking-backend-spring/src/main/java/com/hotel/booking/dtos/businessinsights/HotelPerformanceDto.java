package com.hotel.booking.dtos.businessinsights;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HotelPerformanceDto {

    @JsonProperty("_id")
    private String id;

    private String name;
    private String city;
    private Integer starRating;
    private Double pricePerNight;
    private Long bookingCount = 0L;
    private Double totalRevenue = 0.0;

    public HotelPerformanceDto() {
    }

    public HotelPerformanceDto(String id, String name, String city, Integer starRating, Double pricePerNight, Long bookingCount, Double totalRevenue) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.starRating = starRating;
        this.pricePerNight = pricePerNight;
        this.bookingCount = bookingCount;
        this.totalRevenue = totalRevenue;
    }

    @JsonProperty("_id")
    public String getId() { return id; }

    @JsonProperty("_id")
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public Integer getStarRating() { return starRating; }
    public void setStarRating(Integer starRating) { this.starRating = starRating; }

    public Double getPricePerNight() { return pricePerNight; }
    public void setPricePerNight(Double pricePerNight) { this.pricePerNight = pricePerNight; }

    public Long getBookingCount() { return bookingCount; }
    public void setBookingCount(Long bookingCount) { this.bookingCount = bookingCount; }

    public Double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(Double totalRevenue) { this.totalRevenue = totalRevenue; }
}

