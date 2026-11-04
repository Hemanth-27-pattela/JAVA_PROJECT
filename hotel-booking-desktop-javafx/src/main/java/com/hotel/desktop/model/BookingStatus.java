package com.hotel.desktop.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Booking lifecycle status matching Spring Boot backend BookingStatus enum.
 */
public enum BookingStatus {
    PENDING("pending", "Pending", "badge-pending"),
    CONFIRMED("confirmed", "Confirmed", "badge-confirmed"),
    CANCELLED("cancelled", "Cancelled", "badge-cancelled"),
    COMPLETED("completed", "Completed", "badge-completed"),
    REFUNDED("refunded", "Refunded", "badge-refunded");

    private final String value;
    private final String displayName;
    private final String styleClass;

    BookingStatus(String value, String displayName, String styleClass) {
        this.value = value;
        this.displayName = displayName;
        this.styleClass = styleClass;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getStyleClass() {
        return styleClass;
    }

    @JsonCreator
    public static BookingStatus fromValue(String value) {
        if (value == null || value.isBlank()) {
            return PENDING;
        }
        for (BookingStatus status : values()) {
            if (status.value.equalsIgnoreCase(value.trim()) || status.name().equalsIgnoreCase(value.trim())) {
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
