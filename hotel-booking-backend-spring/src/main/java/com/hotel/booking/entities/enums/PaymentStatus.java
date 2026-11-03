package com.hotel.booking.entities.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PaymentStatus {
    PENDING("pending"),
    PAID("paid"),
    FAILED("failed"),
    REFUNDED("refunded");

    private final String value;

    PaymentStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static PaymentStatus fromValue(String value) {
        if (value == null) {
            return PENDING;
        }
        for (PaymentStatus status : values()) {
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

