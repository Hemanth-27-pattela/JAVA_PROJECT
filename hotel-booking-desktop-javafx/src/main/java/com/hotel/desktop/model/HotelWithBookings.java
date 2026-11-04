package com.hotel.desktop.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Model matching Spring Boot HotelWithBookingsDto returned by GET /api/my-bookings.
 * Inherits all hotel properties and contains an array of the user's bookings for this hotel.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class HotelWithBookings extends Hotel {

    private List<Booking> bookings = new ArrayList<>();

    public HotelWithBookings() {
        super();
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings != null ? bookings : new ArrayList<>();
        // Propagate hotel name and location to bookings for easy card rendering
        for (Booking b : this.bookings) {
            if (b.getHotelName() == null || b.getHotelName().isBlank()) {
                b.setHotelName(getName());
            }
            if (b.getHotelLocation() == null || b.getHotelLocation().isBlank()) {
                b.setHotelLocation(getLocationDisplay());
            }
        }
    }
}
