package com.hotel.booking.services.recommendation;

import com.hotel.booking.entities.Booking;
import com.hotel.booking.entities.Hotel;
import com.hotel.booking.entities.enums.BookingStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserStayProfile Tests")
class UserStayProfileTest {

    @Test
    @DisplayName("Filters confirmed and completed stays, strictly excluding cancelled and pending stays")
    void userStayProfile_filtersValidStaysOnly() {
        Hotel londonHotel = new Hotel();
        londonHotel.setCity("London");
        londonHotel.setCountry("United Kingdom");
        londonHotel.setType(List.of("Boutique"));
        londonHotel.setPricePerNight(BigDecimal.valueOf(150.0));

        Hotel edinburghHotel = new Hotel();
        edinburghHotel.setCity("Edinburgh");
        edinburghHotel.setCountry("United Kingdom");
        edinburghHotel.setType(List.of("Historic"));
        edinburghHotel.setPricePerNight(BigDecimal.valueOf(100.0));

        Hotel tokyoHotel = new Hotel();
        tokyoHotel.setCity("Tokyo");
        tokyoHotel.setCountry("Japan");
        tokyoHotel.setType(List.of("Modern"));
        tokyoHotel.setPricePerNight(BigDecimal.valueOf(250.0));

        Hotel parisHotel = new Hotel();
        parisHotel.setCity("Paris");
        parisHotel.setCountry("France");
        parisHotel.setType(List.of("Romantic"));
        parisHotel.setPricePerNight(BigDecimal.valueOf(300.0));

        // 1. Confirmed stay in London
        Booking b1 = new Booking();
        b1.setStatus(BookingStatus.CONFIRMED);
        b1.setHotel(londonHotel);

        // 2. Completed stay in Edinburgh
        Booking b2 = new Booking();
        b2.setStatus(BookingStatus.COMPLETED);
        b2.setHotel(edinburghHotel);

        // 3. CANCELLED stay in Tokyo - MUST BE EXCLUDED
        Booking b3 = new Booking();
        b3.setStatus(BookingStatus.CANCELLED);
        b3.setHotel(tokyoHotel);

        // 4. PENDING stay in Paris - MUST BE EXCLUDED
        Booking b4 = new Booking();
        b4.setStatus(BookingStatus.PENDING);
        b4.setHotel(parisHotel);

        UserStayProfile profile = UserStayProfile.fromBookings(List.of(b1, b2, b3, b4));

        assertTrue(profile.hasHistory());
        assertEquals(2, profile.getValidStayCount());

        // Confirmed and Completed cities/countries present
        assertTrue(profile.hasBookedCity("London"));
        assertTrue(profile.hasBookedCity("Edinburgh"));
        assertTrue(profile.hasBookedCountry("United Kingdom"));
        assertTrue(profile.getBookedHotelTypes().contains("Boutique"));
        assertTrue(profile.getBookedHotelTypes().contains("Historic"));

        // Cancelled (Tokyo/Japan) and Pending (Paris/France) must NOT be present
        assertFalse(profile.hasBookedCity("Tokyo"));
        assertFalse(profile.hasBookedCountry("Japan"));
        assertFalse(profile.getBookedHotelTypes().contains("Modern"));

        assertFalse(profile.hasBookedCity("Paris"));
        assertFalse(profile.hasBookedCountry("France"));
        assertFalse(profile.getBookedHotelTypes().contains("Romantic"));

        // Average spend across valid stays (£150 + £100) / 2 = £125
        assertEquals(125.0, profile.getAverageNightlySpend(), 0.001);
    }

    @Test
    @DisplayName("Handles empty and null bookings list gracefully")
    void userStayProfile_handlesEmptyList() {
        UserStayProfile profileNull = UserStayProfile.fromBookings(null);
        assertFalse(profileNull.hasHistory());
        assertEquals(0, profileNull.getValidStayCount());
        assertEquals(0.0, profileNull.getAverageNightlySpend(), 0.001);

        UserStayProfile profileEmpty = UserStayProfile.fromBookings(Collections.emptyList());
        assertFalse(profileEmpty.hasHistory());
        assertEquals(0, profileEmpty.getValidStayCount());

        assertFalse(profileEmpty.hasBookedCity("London"));
        assertFalse(profileEmpty.hasBookedCountry("United Kingdom"));
    }

    @Test
    @DisplayName("Computes nightly spend from total cost and duration when hotel price is absent")
    void userStayProfile_computesNightlySpendFromTotalCost() {
        Instant now = Instant.now();
        Instant in3Days = now.plus(3, ChronoUnit.DAYS);

        Booking booking = new Booking();
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setCheckIn(now);
        booking.setCheckOut(in3Days);
        booking.setTotalCost(BigDecimal.valueOf(450.0)); // 3 nights @ £150/night

        Hotel h = new Hotel();
        h.setCity("London");
        booking.setHotel(h);

        UserStayProfile profile = UserStayProfile.fromBookings(List.of(booking));
        assertTrue(profile.hasHistory());
        assertEquals(1, profile.getValidStayCount());
        assertEquals(150.0, profile.getAverageNightlySpend(), 0.001);
    }
}

