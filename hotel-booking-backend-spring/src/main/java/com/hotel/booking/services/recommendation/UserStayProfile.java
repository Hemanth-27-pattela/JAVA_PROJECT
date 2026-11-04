package com.hotel.booking.services.recommendation;

import com.hotel.booking.entities.Booking;
import com.hotel.booking.entities.Hotel;
import com.hotel.booking.entities.enums.BookingStatus;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Encapsulates a guest's historical travel profile derived from valid past stays.
 * Only confirmed and completed bookings contribute positively.
 * Cancelled bookings are strictly excluded.
 */
public class UserStayProfile {

    private final Set<String> bookedCities;
    private final Set<String> bookedCountries;
    private final Set<String> bookedHotelTypes;
    private final double averageNightlySpend;
    private final int validStayCount;

    public UserStayProfile() {
        this(Collections.emptySet(), Collections.emptySet(), Collections.emptySet(), 0.0, 0);
    }

    public UserStayProfile(
            Set<String> bookedCities,
            Set<String> bookedCountries,
            Set<String> bookedHotelTypes,
            double averageNightlySpend,
            int validStayCount
    ) {
        this.bookedCities = bookedCities != null ? Collections.unmodifiableSet(new HashSet<>(bookedCities)) : Collections.emptySet();
        this.bookedCountries = bookedCountries != null ? Collections.unmodifiableSet(new HashSet<>(bookedCountries)) : Collections.emptySet();
        this.bookedHotelTypes = bookedHotelTypes != null ? Collections.unmodifiableSet(new HashSet<>(bookedHotelTypes)) : Collections.emptySet();
        this.averageNightlySpend = Math.max(0.0, averageNightlySpend);
        this.validStayCount = Math.max(0, validStayCount);
    }

    public static UserStayProfile empty() {
        return new UserStayProfile();
    }

    /**
     * Builds a travel profile from the user's booking history.
     * Enforces that only CONFIRMED and COMPLETED stays are considered.
     */
    public static UserStayProfile fromBookings(Collection<Booking> bookings) {
        if (bookings == null || bookings.isEmpty()) {
            return empty();
        }

        Set<String> cities = new HashSet<>();
        Set<String> countries = new HashSet<>();
        Set<String> types = new HashSet<>();
        double totalSpend = 0.0;
        int validCount = 0;

        for (Booking booking : bookings) {
            if (booking == null) {
                continue;
            }
            BookingStatus status = booking.getStatus();
            // Strictly exclude CANCELLED and unconfirmed/pending bookings
            if (status != BookingStatus.CONFIRMED && status != BookingStatus.COMPLETED) {
                continue;
            }

            Hotel hotel = booking.getHotel();
            if (hotel != null) {
                if (hotel.getCity() != null && !hotel.getCity().isBlank()) {
                    cities.add(hotel.getCity().trim());
                }
                if (hotel.getCountry() != null && !hotel.getCountry().isBlank()) {
                    countries.add(hotel.getCountry().trim());
                }
                if (hotel.getType() != null) {
                    for (String t : hotel.getType()) {
                        if (t != null && !t.isBlank()) {
                            types.add(t.trim());
                        }
                    }
                }
            }

            // Calculate nightly spend
            double nightlySpend = 0.0;
            if (hotel != null && hotel.getPricePerNight() != null && hotel.getPricePerNight().compareTo(BigDecimal.ZERO) > 0) {
                nightlySpend = hotel.getPricePerNight().doubleValue();
            } else if (booking.getTotalCost() != null && booking.getTotalCost().compareTo(BigDecimal.ZERO) > 0) {
                long nights = 1;
                if (booking.getCheckIn() != null && booking.getCheckOut() != null) {
                    nights = Math.max(1, ChronoUnit.DAYS.between(
                            booking.getCheckIn().atZone(java.time.ZoneOffset.UTC).toLocalDate(),
                            booking.getCheckOut().atZone(java.time.ZoneOffset.UTC).toLocalDate()
                    ));
                }
                nightlySpend = booking.getTotalCost().doubleValue() / nights;
            }

            if (nightlySpend > 0.0) {
                totalSpend += nightlySpend;
            }
            validCount++;
        }

        if (validCount == 0) {
            return empty();
        }

        double avgSpend = validCount > 0 ? (totalSpend / validCount) : 0.0;
        return new UserStayProfile(cities, countries, types, avgSpend, validCount);
    }

    public boolean hasHistory() {
        return validStayCount > 0;
    }

    public boolean hasBookedCity(String city) {
        if (city == null || city.isBlank() || bookedCities.isEmpty()) {
            return false;
        }
        String target = city.trim().toLowerCase(Locale.ROOT);
        return bookedCities.stream().anyMatch(c -> c.trim().toLowerCase(Locale.ROOT).equals(target));
    }

    public boolean hasBookedCountry(String country) {
        if (country == null || country.isBlank() || bookedCountries.isEmpty()) {
            return false;
        }
        String target = country.trim().toLowerCase(Locale.ROOT);
        return bookedCountries.stream().anyMatch(c -> c.trim().toLowerCase(Locale.ROOT).equals(target));
    }

    public Set<String> getBookedCities() {
        return bookedCities;
    }

    public Set<String> getBookedCountries() {
        return bookedCountries;
    }

    public Set<String> getBookedHotelTypes() {
        return bookedHotelTypes;
    }

    public double getAverageNightlySpend() {
        return averageNightlySpend;
    }

    public int getValidStayCount() {
        return validStayCount;
    }
}

