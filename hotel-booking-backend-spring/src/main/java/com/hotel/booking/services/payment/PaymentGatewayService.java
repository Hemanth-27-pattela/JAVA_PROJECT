package com.hotel.booking.services.payment;

import java.math.BigDecimal;

public interface PaymentGatewayService {

    PaymentIntentResult createPaymentIntent(String hotelId, String userId, BigDecimal totalCost, String currency);

    PaymentVerificationResult verifyPayment(
            String paymentIntentId,
            String expectedHotelId,
            String expectedUserId,
            BigDecimal expectedAmount,
            String expectedCurrency
    );

    default RefundResult processRefund(String paymentIntentId, BigDecimal amount) {
        return processRefund(paymentIntentId, amount, null);
    }

    RefundResult processRefund(String paymentIntentId, BigDecimal amount, String idempotencyKey);

    boolean isSimulation();
}
