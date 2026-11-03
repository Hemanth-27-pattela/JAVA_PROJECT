package com.hotel.booking.services.payment;

import java.math.BigDecimal;

public class PaymentIntentResult {

    private final String id;
    private final String clientSecret;
    private final BigDecimal amount;
    private final String currency;
    private final String status;
    private final boolean simulated;

    public PaymentIntentResult(String id, String clientSecret, BigDecimal amount, String currency, String status, boolean simulated) {
        this.id = id;
        this.clientSecret = clientSecret;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.simulated = simulated;
    }

    public String getId() {
        return id;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getStatus() {
        return status;
    }

    public boolean isSimulated() {
        return simulated;
    }
}

