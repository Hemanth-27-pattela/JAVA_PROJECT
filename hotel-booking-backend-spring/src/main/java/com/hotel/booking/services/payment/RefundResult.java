package com.hotel.booking.services.payment;

import java.math.BigDecimal;

public class RefundResult {

    private final boolean success;
    private final String refundId;
    private final BigDecimal refundAmount;
    private final String refundSkipped;
    private final String failureReason;
    private final boolean simulated;

    private RefundResult(
            boolean success,
            String refundId,
            BigDecimal refundAmount,
            String refundSkipped,
            String failureReason,
            boolean simulated
    ) {
        this.success = success;
        this.refundId = refundId;
        this.refundAmount = refundAmount;
        this.refundSkipped = refundSkipped;
        this.failureReason = failureReason;
        this.simulated = simulated;
    }

    public static RefundResult success(String refundId, BigDecimal refundAmount, String refundSkipped, boolean simulated) {
        return new RefundResult(true, refundId, refundAmount, refundSkipped, null, simulated);
    }

    public static RefundResult skipped(String reason, boolean simulated) {
        return new RefundResult(true, null, BigDecimal.ZERO, reason, null, simulated);
    }

    public static RefundResult failed(String failureReason, boolean simulated) {
        return new RefundResult(false, null, BigDecimal.ZERO, null, failureReason, simulated);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getRefundId() {
        return refundId;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public String getRefundSkipped() {
        return refundSkipped;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public boolean isSimulated() {
        return simulated;
    }
}

