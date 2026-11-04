package com.hotel.desktop.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/**
 * Response payload returned after initializing a payment intent.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentIntentResponse {

    private String paymentIntentId;
    private String clientSecret;
    private BigDecimal totalCost;

    public PaymentIntentResponse() {
    }

    public PaymentIntentResponse(String paymentIntentId, String clientSecret, BigDecimal totalCost) {
        this.paymentIntentId = paymentIntentId;
        this.clientSecret = clientSecret;
        this.totalCost = totalCost;
    }

    public String getPaymentIntentId() {
        return paymentIntentId;
    }

    public void setPaymentIntentId(String paymentIntentId) {
        this.paymentIntentId = paymentIntentId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }
}
