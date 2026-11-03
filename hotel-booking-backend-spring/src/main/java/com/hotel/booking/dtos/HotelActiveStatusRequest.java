package com.hotel.booking.dtos;

import jakarta.validation.constraints.NotNull;

public class HotelActiveStatusRequest {

    @NotNull(message = "isActive boolean required")
    private Boolean isActive;

    public HotelActiveStatusRequest() {
    }

    public HotelActiveStatusRequest(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }
}

