package com.hotel.booking.services.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SimulatedPaymentGatewayService implements PaymentGatewayService {

    private static final Logger log = LoggerFactory.getLogger(SimulatedPaymentGatewayService.class);

    public record SimulatedIntent(
            String id,
            String clientSecret,
            String hotelId,
            String userId,
            BigDecimal amount,
            String currency,
            String status
    ) {}

    private final Map<String, SimulatedIntent> intents = new ConcurrentHashMap<>();
    private final Map<String, RefundResult> refundIdempotencyStore = new ConcurrentHashMap<>();

    @Override
    public PaymentIntentResult createPaymentIntent(String hotelId, String userId, BigDecimal totalCost, String currency) {
        String piId = "pi_sim_" + UUID.randomUUID().toString().replace("-", "").substring(0, 24);
        String clientSecret = piId + "_secret_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        BigDecimal amount = totalCost.setScale(2, RoundingMode.HALF_UP);
        String cur = currency != null ? currency.toLowerCase() : "gbp";

        // In simulation mode, created intents default to "succeeded" to simulate successful client payment
        SimulatedIntent intent = new SimulatedIntent(piId, clientSecret, hotelId, userId, amount, cur, "succeeded");
        intents.put(piId, intent);

        log.info("[SIMULATION] Created simulated PaymentIntent: id={}, amount={}, currency={}, hotelId={}, userId={}",
                piId, amount, cur, hotelId, userId);

        return new PaymentIntentResult(piId, clientSecret, amount, cur, "succeeded", true);
    }

    @Override
    public PaymentVerificationResult verifyPayment(
            String paymentIntentId,
            String expectedHotelId,
            String expectedUserId,
            BigDecimal expectedAmount,
            String expectedCurrency
    ) {
        if (paymentIntentId == null || paymentIntentId.isBlank()) {
            return PaymentVerificationResult.failed("Payment intent ID is required", true);
        }

        SimulatedIntent intent = intents.get(paymentIntentId.trim());
        if (intent == null) {
            log.warn("[SIMULATION] Payment intent not found or forged: {}", paymentIntentId);
            return PaymentVerificationResult.failed("payment intent not found", true);
        }

        if (!intent.hotelId().equals(expectedHotelId) || !intent.userId().equals(expectedUserId)) {
            log.warn("[SIMULATION] Payment intent mismatch for intent {}: expected hotel={}, user={}, actual hotel={}, user={}",
                    paymentIntentId, expectedHotelId, expectedUserId, intent.hotelId(), intent.userId());
            return PaymentVerificationResult.failed("payment intent mismatch", true);
        }

        if (!"succeeded".equalsIgnoreCase(intent.status())) {
            log.warn("[SIMULATION] Payment intent not succeeded: status={}", intent.status());
            return PaymentVerificationResult.failed("payment intent not succeeded. Status: " + intent.status(), true);
        }

        if (expectedAmount != null && intent.amount().compareTo(expectedAmount.setScale(2, RoundingMode.HALF_UP)) != 0) {
            log.warn("[SIMULATION] Payment intent amount mismatch: expected {}, actual {}", expectedAmount, intent.amount());
            return PaymentVerificationResult.failed(
                    "payment intent amount mismatch: expected " + expectedAmount + " but was " + intent.amount(),
                    true
            );
        }

        if (expectedCurrency != null && !intent.currency().equalsIgnoreCase(expectedCurrency)) {
            log.warn("[SIMULATION] Payment intent currency mismatch: expected {}, actual {}", expectedCurrency, intent.currency());
            return PaymentVerificationResult.failed(
                    "payment intent currency mismatch: expected " + expectedCurrency + " but was " + intent.currency(),
                    true
            );
        }

        log.info("[SIMULATION] Payment intent verified successfully: {}", paymentIntentId);
        return PaymentVerificationResult.success(intent.id(), intent.amount(), intent.currency(), true);
    }

    @Override
    public RefundResult processRefund(String paymentIntentId, BigDecimal amount) {
        return processRefund(paymentIntentId, amount, null);
    }

    @Override
    public RefundResult processRefund(String paymentIntentId, BigDecimal amount, String idempotencyKey) {
        if (paymentIntentId == null || paymentIntentId.isBlank()) {
            return RefundResult.skipped("Cancelled without Stripe refund (no payment intent on file)", true);
        }

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            RefundResult existing = refundIdempotencyStore.get(idempotencyKey.trim());
            if (existing != null) {
                log.info("[SIMULATION] Idempotent refund replay for key {}: returning existing refundId={}",
                        idempotencyKey, existing.getRefundId());
                return existing;
            }
        }

        String refundId = "re_sim_" + UUID.randomUUID().toString().replace("-", "").substring(0, 24);
        BigDecimal refundAmount = amount != null ? amount.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        log.info("[SIMULATION] Processed simulated refund {} for intent {} amount {}", refundId, paymentIntentId, refundAmount);

        RefundResult result = RefundResult.success(
                refundId,
                refundAmount,
                "Simulated refund (test/dev mode - no real funds transferred)",
                true
        );

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            refundIdempotencyStore.put(idempotencyKey.trim(), result);
        }

        return result;
    }

    @Override
    public boolean isSimulation() {
        return true;
    }

    // Testing and simulation helper to seed intents
    public void registerSimulatedIntent(String id, String hotelId, String userId, BigDecimal amount, String currency, String status) {
        String clientSecret = id + "_secret_sim";
        BigDecimal amt = amount != null ? amount.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        String cur = currency != null ? currency.toLowerCase() : "gbp";
        intents.put(id, new SimulatedIntent(id, clientSecret, hotelId, userId, amt, cur, status));
    }

    public void clear() {
        intents.clear();
        refundIdempotencyStore.clear();
    }
}
