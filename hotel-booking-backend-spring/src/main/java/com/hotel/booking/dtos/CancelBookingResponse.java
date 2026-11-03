package com.hotel.booking.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CancelBookingResponse {

    private BookingDto booking;
    private Double refundAmount = 0.0;
    private String refundSkipped;

    public CancelBookingResponse() {
    }

    public CancelBookingResponse(BookingDto booking, Double refundAmount, String refundSkipped) {
        this.booking = booking;
        this.refundAmount = refundAmount;
        this.refundSkipped = refundSkipped;
    }

    public BookingDto getBooking() {
        return booking;
    }

    public void setBooking(BookingDto booking) {
        this.booking = booking;
    }

    public Double getRefundAmount() {
        return refundAmount;
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
}

