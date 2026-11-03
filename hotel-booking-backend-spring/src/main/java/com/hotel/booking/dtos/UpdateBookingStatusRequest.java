package com.hotel.booking.dtos;

import jakarta.validation.constraints.NotBlank;

public class UpdateBookingStatusRequest {

    @NotBlank(message = "Invalid status")
    private String status;

    private String cancellationReason;

    public UpdateBookingStatusRequest() {
    }

    public UpdateBookingStatusRequest(String status, String cancellationReason) {
        this.status = status;
        this.cancellationReason = cancellationReason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }
}

