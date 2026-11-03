package com.hotel.booking.entities.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum UserRole {
    USER("user"),
    ADMIN("admin"),
    HOTEL_OWNER("hotel_owner");

    private final String value;

    UserRole(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
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
        return value;
    }
}

