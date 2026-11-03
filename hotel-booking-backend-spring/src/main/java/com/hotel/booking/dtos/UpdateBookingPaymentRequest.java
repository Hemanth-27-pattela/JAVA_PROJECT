package com.hotel.booking.dtos;

import jakarta.validation.constraints.NotBlank;

public class UpdateBookingPaymentRequest {

    @NotBlank(message = "Invalid payment status")
    private String paymentStatus;

    private String paymentMethod;

    public UpdateBookingPaymentRequest() {
    }

    public UpdateBookingPaymentRequest(String paymentStatus, String paymentMethod) {
        this.paymentStatus = paymentStatus;
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}

