package com.hotel.booking.services.payment;

import java.math.BigDecimal;

public class PaymentVerificationResult {

    private final boolean verified;
    private final String paymentIntentId;
    private final BigDecimal amount;
    private final String currency;
    private final String status;
    private final String failureReason;
    private final boolean simulated;

    private PaymentVerificationResult(
            boolean verified,
            String paymentIntentId,
            BigDecimal amount,
            String currency,
            String status,
            String failureReason,
            boolean simulated
    ) {
        this.verified = verified;
        this.paymentIntentId = paymentIntentId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.failureReason = failureReason;
        this.simulated = simulated;
    }

    public static PaymentVerificationResult success(String paymentIntentId, BigDecimal amount, String currency, boolean simulated) {
        return new PaymentVerificationResult(true, paymentIntentId, amount, currency, "succeeded", null, simulated);
    }

    public static PaymentVerificationResult failed(String failureReason, boolean simulated) {
        return new PaymentVerificationResult(false, null, null, null, null, failureReason, simulated);
    }

    public boolean isVerified() {
        return verified;
    }

    public String getPaymentIntentId() {
        return paymentIntentId;
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

    public String getFailureReason() {
        return failureReason;
    }

    public boolean isSimulated() {
        return simulated;
    }
}

