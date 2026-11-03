package com.hotel.booking.dtos;

public class CancelBookingRequest {

    private String cancellationReason;

    public CancelBookingRequest() {
    }

    public CancelBookingRequest(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }
}

