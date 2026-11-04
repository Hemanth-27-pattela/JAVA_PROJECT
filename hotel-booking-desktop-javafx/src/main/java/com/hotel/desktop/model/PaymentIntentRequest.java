package com.hotel.desktop.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Request payload for creating a simulated payment intent on backend.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentIntentRequest {

    private Integer numberOfNights = 1;

    public PaymentIntentRequest() {
    }

    public PaymentIntentRequest(Integer numberOfNights) {
        this.numberOfNights = numberOfNights;
    }

    public Integer getNumberOfNights() {
        return numberOfNights != null && numberOfNights > 0 ? numberOfNights : 1;
    }

    public void setNumberOfNights(Integer numberOfNights) {
        this.numberOfNights = numberOfNights;
    }
}

