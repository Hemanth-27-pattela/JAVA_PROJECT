package com.hotel.booking.dtos.businessinsights;

public class WeeklyForecastItemDto {
    private String week;
    private Integer bookings = 0;
    private Double revenue = 0.0;
    private Double confidence = 1.0;

    public WeeklyForecastItemDto() {
    }

    public WeeklyForecastItemDto(String week, Integer bookings, Double revenue, Double confidence) {
        this.week = week;
        this.bookings = bookings;
        this.revenue = revenue;
        this.confidence = confidence;
    }

    public String getWeek() { return week; }
    public void setWeek(String week) { this.week = week; }

    public Integer getBookings() { return bookings; }
    public void setBookings(Integer bookings) { this.bookings = bookings; }

    public Double getRevenue() { return revenue; }
    public void setRevenue(Double revenue) { this.revenue = revenue; }

    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }
}

