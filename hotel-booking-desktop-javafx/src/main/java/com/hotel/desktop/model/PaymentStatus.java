package com.hotel.desktop.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Payment transaction status matching Spring Boot backend PaymentStatus enum.
 */
public enum PaymentStatus {
    PENDING("pending", "Pending", "badge-pending"),
    PAID("paid", "Paid", "badge-paid"),
    FAILED("failed", "Failed", "badge-failed"),
    REFUNDED("refunded", "Refunded", "badge-refunded");

    private final String value;
    private final String displayName;
    private final String styleClass;

    PaymentStatus(String value, String displayName, String styleClass) {
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
    public static PaymentStatus fromValue(String value) {
        if (value == null || value.isBlank()) {
            return PENDING;
        }
        for (PaymentStatus status : values()) {
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
