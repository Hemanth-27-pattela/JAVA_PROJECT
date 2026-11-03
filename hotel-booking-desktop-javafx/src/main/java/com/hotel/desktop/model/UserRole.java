package com.hotel.desktop.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * User roles supported by the Hotel Booking platform.
 * Mirrors the Spring Boot backend UserRole enum.
 */
public enum UserRole {
    USER("user", "Guest"),
    HOTEL_OWNER("hotel_owner", "Hotel Owner"),
    ADMIN("admin", "Administrator");

    private final String value;
    private final String displayName;

    UserRole(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static UserRole fromValue(String value) {
        if (value == null) {
            return USER;
        }
        for (UserRole role : values()) {
            if (role.value.equalsIgnoreCase(value) || role.name().equalsIgnoreCase(value)) {
                return role;
            }
        }
        return USER;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

