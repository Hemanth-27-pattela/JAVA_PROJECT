package com.hotel.desktop.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.Locale;

/**
 * Response returned from POST /api/bookings/{id}/cancel.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CancelBookingResponse {

    private Booking booking;
    private Double refundAmount = 0.0;
    private String refundSkipped;

    public CancelBookingResponse() {
    }

    public CancelBookingResponse(Booking booking, Double refundAmount, String refundSkipped) {
        this.booking = booking;
        this.refundAmount = refundAmount;
        this.refundSkipped = refundSkipped;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public Double getRefundAmount() {
        return refundAmount != null ? refundAmount : 0.0;
    }

    public void setRefundAmount(Double refundAmount) {
        this.refundAmount = refundAmount;
    }

    public String getRefundSkipped() {
        return refundSkipped;
    }

    public void setRefundSkipped(String refundSkipped) {
        this.refundSkipped = refundSkipped;
    }

    public String getFormattedRefundAmount() {
        return "£" + String.format(Locale.UK, "%.2f", getRefundAmount());
    }
}

