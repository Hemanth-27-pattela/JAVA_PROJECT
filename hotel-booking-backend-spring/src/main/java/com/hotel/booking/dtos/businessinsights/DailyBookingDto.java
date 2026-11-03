package com.hotel.booking.dtos.businessinsights;

public class DailyBookingDto {
    private String date;
    private Integer bookings = 0;

    public DailyBookingDto() {
    }

    public DailyBookingDto(String date, Integer bookings) {
        this.date = date;
        this.bookings = bookings;
    }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public Integer getBookings() { return bookings; }
    public void setBookings(Integer bookings) { this.bookings = bookings; }
}

