package com.hotel.booking.services.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class StripePaymentGatewayService implements PaymentGatewayService {

    private static final Logger log = LoggerFactory.getLogger(StripePaymentGatewayService.class);

    private final String apiKey;
    private final String apiUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public StripePaymentGatewayService(String apiKey, String apiUrl, HttpClient httpClient, ObjectMapper objectMapper) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException("STRIPE_API_KEY is required when payment mode is stripe. Please set STRIPE_API_KEY environment variable.");
        }
        String cleanKey = apiKey.trim();
        if (cleanKey.startsWith("pk_")) {
            throw new IllegalStateException("STRIPE_API_KEY appears to be a publishable key (pk_...). A secret key (sk_... or rk_...) is required for backend operations.");
        }
        this.apiKey = cleanKey;
        this.apiUrl = (apiUrl != null && !apiUrl.isBlank()) ? apiUrl.replaceAll("/+$", "") : "https://api.stripe.com";
        this.httpClient = httpClient != null ? httpClient : HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
    }

    @Override
    public PaymentIntentResult createPaymentIntent(String hotelId, String userId, BigDecimal totalCost, String currency) {
        String cur = currency != null ? currency.toLowerCase() : "gbp";
        long amountInCents = totalCost.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).longValue();

        String formBody = "amount=" + amountInCents
                + "&currency=" + URLEncoder.encode(cur, StandardCharsets.UTF_8)
                + "&metadata[hotelId]=" + URLEncoder.encode(hotelId, StandardCharsets.UTF_8)
                + "&metadata[userId]=" + URLEncoder.encode(userId, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl + "/v1/payment_intents"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(formBody))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode root = objectMapper.readTree(response.body());

            if (response.statusCode() >= 400) {
                String errorMsg = root.path("error").path("message").asText("Stripe error creating payment intent");
                log.error("Stripe create PaymentIntent failed with status {}: {}", response.statusCode(), errorMsg);
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Stripe API error: " + errorMsg);
            }

            String id = root.path("id").asText();
            String clientSecret = root.path("client_secret").asText();
            String status = root.path("status").asText();
            BigDecimal amount = BigDecimal.valueOf(root.path("amount").asLong())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            return new PaymentIntentResult(id, clientSecret, amount, cur, status, false);
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.error("Network failure calling Stripe create PaymentIntent: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Stripe API network failure: " + e.getMessage());
        }
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
            return PaymentVerificationResult.failed("Payment intent ID is required", false);
        }

        String encodedId = URLEncoder.encode(paymentIntentId.trim(), StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl + "/v1/payment_intents/" + encodedId))
                .header("Authorization", "Bearer " + apiKey)
                .header("Accept", "application/json")
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode root = objectMapper.readTree(response.body());

            if (response.statusCode() == 404) {
                return PaymentVerificationResult.failed("payment intent not found", false);
            }

            if (response.statusCode() >= 400) {
                String code = root.path("error").path("code").asText();
                String errorMsg = root.path("error").path("message").asText("Stripe error retrieving payment intent");
                if ("resource_missing".equals(code) || errorMsg.toLowerCase().contains("no such payment_intent")) {
                    return PaymentVerificationResult.failed("payment intent not found", false);
                }
                log.error("Stripe retrieve PaymentIntent error status {}: {}", response.statusCode(), errorMsg);
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Stripe API error: " + errorMsg);
            }

            String id = root.path("id").asText();
            String status = root.path("status").asText();
            String metaHotelId = root.path("metadata").path("hotelId").asText();
            String metaUserId = root.path("metadata").path("userId").asText();
            long amountInCents = root.path("amount").asLong();
            String currency = root.path("currency").asText();

            // 1. Metadata check
            if (!expectedHotelId.equals(metaHotelId) || !expectedUserId.equals(metaUserId)) {
                log.warn("Stripe metadata mismatch for {}: expected hotel={}, user={}, actual hotel={}, user={}",
                        paymentIntentId, expectedHotelId, expectedUserId, metaHotelId, metaUserId);
                return PaymentVerificationResult.failed("payment intent mismatch", false);
            }

            // 2. Status check
            if (!"succeeded".equalsIgnoreCase(status)) {
                log.warn("Stripe PaymentIntent {} not succeeded, status: {}", paymentIntentId, status);
                return PaymentVerificationResult.failed("payment intent not succeeded. Status: " + status, false);
            }

            // 3. Amount check
            if (expectedAmount != null) {
                long expectedCents = expectedAmount.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).longValue();
                if (amountInCents != expectedCents) {
                    BigDecimal actualAmt = BigDecimal.valueOf(amountInCents).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    log.warn("Stripe amount mismatch for {}: expected {}, actual {}", paymentIntentId, expectedAmount, actualAmt);
                    return PaymentVerificationResult.failed(
                            "payment intent amount mismatch: expected " + expectedAmount + " but was " + actualAmt,
                            false
                    );
                }
            }

            // 4. Currency check
            if (expectedCurrency != null && !expectedCurrency.equalsIgnoreCase(currency)) {
                log.warn("Stripe currency mismatch for {}: expected {}, actual {}", paymentIntentId, expectedCurrency, currency);
                return PaymentVerificationResult.failed(
                        "payment intent currency mismatch: expected " + expectedCurrency + " but was " + currency,
                        false
                );
            }

            BigDecimal actualAmount = BigDecimal.valueOf(amountInCents).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            log.info("Stripe PaymentIntent {} verified successfully for hotel {} and user {}", id, expectedHotelId, expectedUserId);
            return PaymentVerificationResult.success(id, actualAmount, currency, false);

        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.error("Network failure calling Stripe retrieve PaymentIntent: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Stripe API network failure: " + e.getMessage());
        }
    }

    @Override
    public RefundResult processRefund(String paymentIntentId, BigDecimal amount) {
        return processRefund(paymentIntentId, amount, null);
    }

    @Override
    public RefundResult processRefund(String paymentIntentId, BigDecimal amount, String idempotencyKey) {
        if (paymentIntentId == null || paymentIntentId.isBlank()) {
            return RefundResult.skipped("Cancelled without Stripe refund (no payment intent on file)", false);
        }

        StringBuilder formBody = new StringBuilder("payment_intent=")
                .append(URLEncoder.encode(paymentIntentId.trim(), StandardCharsets.UTF_8));

        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            long amountInPence = amount.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).longValue();
            formBody.append("&amount=").append(amountInPence);
        }

        HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl + "/v1/refunds"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "application/json");

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            reqBuilder.header("Idempotency-Key", idempotencyKey.trim());
        }

        HttpRequest request = reqBuilder.POST(HttpRequest.BodyPublishers.ofString(formBody.toString())).build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode root = objectMapper.readTree(response.body());

            if (response.statusCode() >= 400) {
                String errorMsg = root.path("error").path("message").asText("Stripe refund failed");
                log.error("Stripe refund failed with status {}: {}", response.statusCode(), errorMsg);
                return RefundResult.failed(errorMsg, false);
            }

            String refundId = root.path("id").asText();
            long refundAmountCents = root.path("amount").asLong();
            BigDecimal refundAmount = BigDecimal.valueOf(refundAmountCents).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            log.info("Stripe refund created successfully: id={}, amount={}, paymentIntent={}", refundId, refundAmount, paymentIntentId);
            return RefundResult.success(refundId, refundAmount, null, false);

        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.error("Network failure calling Stripe refund: {}", e.getMessage());
            return RefundResult.failed("Stripe refund network error: " + e.getMessage(), false);
        }
    }

    @Override
    public boolean isSimulation() {
        return false;
    }
}
