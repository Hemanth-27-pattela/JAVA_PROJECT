package com.hotel.booking.dtos;

import java.util.ArrayList;
import java.util.List;

public class HotelWithBookingsDto extends HotelDto {

    private List<BookingDto> bookings = new ArrayList<>();

    public HotelWithBookingsDto() {
        super();
    }

    public List<BookingDto> getBookings() {
        return bookings;
    }

    public void setBookings(List<BookingDto> bookings) {
        this.bookings = bookings != null ? bookings : new ArrayList<>();
    }
}

