package com.hotel.booking.dtos.businessinsights;

public class WeeklyHistoricalItemDto {
    private String week;
    private Integer bookings = 0;
    private Double revenue = 0.0;

    public WeeklyHistoricalItemDto() {
    }

    public WeeklyHistoricalItemDto(String week, Integer bookings, Double revenue) {
        this.week = week;
        this.bookings = bookings;
        this.revenue = revenue;
    }

    public String getWeek() { return week; }
    public void setWeek(String week) { this.week = week; }

    public Integer getBookings() { return bookings; }
    public void setBookings(Integer bookings) { this.bookings = bookings; }

    public Double getRevenue() { return revenue; }
    public void setRevenue(Double revenue) { this.revenue = revenue; }
}

