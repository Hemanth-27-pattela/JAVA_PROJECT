package com.hotel.booking.entities.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum BookingStatus {
    PENDING("pending"),
    CONFIRMED("confirmed"),
    CANCELLED("cancelled"),
    COMPLETED("completed"),
    REFUNDED("refunded");

    private final String value;

    BookingStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static BookingStatus fromValue(String value) {
        if (value == null) {
            return PENDING;
        }
        for (BookingStatus status : values()) {
            if (status.value.equalsIgnoreCase(value) || status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }
        return PENDING;
    }

    @Override
    public String toString() {
        return value;
    }
}

