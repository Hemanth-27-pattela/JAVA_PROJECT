package com.hotel.booking.services.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PaymentGatewayServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private HttpClient mockHttpClient;

    @BeforeEach
    void setUp() {
        mockHttpClient = mock(HttpClient.class);
    }

    // =========================================================================
    // Stripe Configuration & Startup Safety
    // =========================================================================

    @Test
    @DisplayName("StripePaymentGatewayService fails construction if API key is null or empty")
    void testStripeFailsOnEmptyApiKey() {
        IllegalStateException ex1 = assertThrows(IllegalStateException.class, () -> {
            new StripePaymentGatewayService(null, "https://api.stripe.com", mockHttpClient, objectMapper);
        });
        assertTrue(ex1.getMessage().contains("STRIPE_API_KEY is required"));

        IllegalStateException ex2 = assertThrows(IllegalStateException.class, () -> {
            new StripePaymentGatewayService("   ", "https://api.stripe.com", mockHttpClient, objectMapper);
        });
        assertTrue(ex2.getMessage().contains("STRIPE_API_KEY is required"));
    }

    @Test
    @DisplayName("StripePaymentGatewayService fails construction if publishable key pk_ is supplied")
    void testStripeFailsOnPublishableKey() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            new StripePaymentGatewayService("pk_test_12345", "https://api.stripe.com", mockHttpClient, objectMapper);
        });
        assertTrue(ex.getMessage().contains("publishable key"));
    }

    // =========================================================================
    // Stripe Payment Intent Creation & Monetary Precision (£125.50 and £99.99)
    // =========================================================================

    @Test
    @DisplayName("StripePaymentGatewayService converts £125.50 to 12550 pence correctly in request and response")
    @SuppressWarnings("unchecked")
    void testStripeMonetaryPrecision12550() throws Exception {
        String jsonResponse = """
                {
                    "id": "pi_stripe_12550",
                    "client_secret": "pi_stripe_12550_secret_abc",
                    "amount": 12550,
                    "currency": "gbp",
                    "status": "requires_payment_method"
                }
                """;

        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(jsonResponse);

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        when(mockHttpClient.send(requestCaptor.capture(), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        StripePaymentGatewayService service = new StripePaymentGatewayService(
                "sk_test_mock_secret",
                "https://api.stripe.com",
                mockHttpClient,
                objectMapper
        );

        PaymentIntentResult result = service.createPaymentIntent("hotel_1", "user_1", new BigDecimal("125.50"), "gbp");
        assertNotNull(result);
        assertEquals("pi_stripe_12550", result.getId());
        assertEquals(new BigDecimal("125.50"), result.getAmount());
        assertEquals("gbp", result.getCurrency());
        assertFalse(result.isSimulated());

        // Verify request was sent as POST with proper headers
        HttpRequest sentRequest = requestCaptor.getValue();
        assertEquals("POST", sentRequest.method());
        assertEquals("Bearer sk_test_mock_secret", sentRequest.headers().firstValue("Authorization").orElse(""));
        assertEquals("application/x-www-form-urlencoded", sentRequest.headers().firstValue("Content-Type").orElse(""));
    }

    @Test
    @DisplayName("StripePaymentGatewayService converts £99.99 to 9999 pence correctly in request and response")
    @SuppressWarnings("unchecked")
    void testStripeMonetaryPrecision9999() throws Exception {
        String jsonResponse = """
                {
                    "id": "pi_stripe_9999",
                    "client_secret": "pi_stripe_9999_secret_xyz",
                    "amount": 9999,
                    "currency": "gbp",
                    "status": "requires_payment_method"
                }
                """;

        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(jsonResponse);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        StripePaymentGatewayService service = new StripePaymentGatewayService(
                "sk_test_mock_secret",
                "https://api.stripe.com",
                mockHttpClient,
                objectMapper
        );

        PaymentIntentResult result = service.createPaymentIntent("hotel_1", "user_1", new BigDecimal("99.99"), "gbp");
        assertNotNull(result);
        assertEquals("pi_stripe_9999", result.getId());
        assertEquals(new BigDecimal("99.99"), result.getAmount());
        assertEquals("gbp", result.getCurrency());
    }

    @Test
    @DisplayName("StripePaymentGatewayService throws 502 on Stripe API error during intent creation")
    @SuppressWarnings("unchecked")
    void testStripeCreatePaymentIntentFailure() throws Exception {
        String errorJson = """
                {
                    "error": {
                        "message": "Invalid currency"
                    }
                }
                """;

        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(400);
        when(mockResponse.body()).thenReturn(errorJson);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        StripePaymentGatewayService service = new StripePaymentGatewayService(
                "sk_test_mock_secret",
                "https://api.stripe.com",
                mockHttpClient,
                objectMapper
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            service.createPaymentIntent("hotel_1", "user_1", new BigDecimal("450.00"), "xyz");
        });
        assertEquals(HttpStatus.BAD_GATEWAY, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Invalid currency"));
    }

    // =========================================================================
    // Stripe Payment Verification
    // =========================================================================

    @Test
    @DisplayName("StripePaymentGatewayService verifies succeeded payment intent with £125.50 (12550 pence)")
    @SuppressWarnings("unchecked")
    void testStripeVerifyPaymentSuccessWith12550() throws Exception {
        String jsonResponse = """
                {
                    "id": "pi_stripe_succeeded_12550",
                    "amount": 12550,
                    "currency": "gbp",
                    "status": "succeeded",
                    "metadata": {
                        "hotelId": "hotel_1",
                        "userId": "user_1"
                    }
                }
                """;

        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(jsonResponse);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        StripePaymentGatewayService service = new StripePaymentGatewayService(
                "sk_test_mock_secret",
                "https://api.stripe.com",
                mockHttpClient,
                objectMapper
        );

        PaymentVerificationResult result = service.verifyPayment(
                "pi_stripe_succeeded_12550",
                "hotel_1",
                "user_1",
                new BigDecimal("125.50"),
                "gbp"
        );

        assertTrue(result.isVerified());
        assertEquals("pi_stripe_succeeded_12550", result.getPaymentIntentId());
        assertEquals("succeeded", result.getStatus());
        assertEquals(new BigDecimal("125.50"), result.getAmount());
        assertFalse(result.isSimulated());
    }

    @Test
    @DisplayName("StripePaymentGatewayService verifies succeeded payment intent with £99.99 (9999 pence)")
    @SuppressWarnings("unchecked")
    void testStripeVerifyPaymentSuccessWith9999() throws Exception {
        String jsonResponse = """
                {
                    "id": "pi_stripe_succeeded_9999",
                    "amount": 9999,
                    "currency": "gbp",
                    "status": "succeeded",
                    "metadata": {
                        "hotelId": "hotel_1",
                        "userId": "user_1"
                    }
                }
                """;

        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(jsonResponse);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        StripePaymentGatewayService service = new StripePaymentGatewayService(
                "sk_test_mock_secret",
                "https://api.stripe.com",
                mockHttpClient,
                objectMapper
        );

        PaymentVerificationResult result = service.verifyPayment(
                "pi_stripe_succeeded_9999",
                "hotel_1",
                "user_1",
                new BigDecimal("99.99"),
                "gbp"
        );

        assertTrue(result.isVerified());
        assertEquals(new BigDecimal("99.99"), result.getAmount());
    }

    @Test
    @DisplayName("StripePaymentGatewayService rejects unverified intent when status is not succeeded")
    @SuppressWarnings("unchecked")
    void testStripeVerifyPaymentUnpaidStatus() throws Exception {
        String jsonResponse = """
                {
                    "id": "pi_stripe_pending",
                    "amount": 45000,
                    "currency": "gbp",
                    "status": "requires_payment_method",
                    "metadata": {
                        "hotelId": "hotel_1",
                        "userId": "user_1"
                    }
                }
                """;

        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(jsonResponse);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        StripePaymentGatewayService service = new StripePaymentGatewayService(
                "sk_test_mock_secret",
                "https://api.stripe.com",
                mockHttpClient,
                objectMapper
        );

        PaymentVerificationResult result = service.verifyPayment(
                "pi_stripe_pending",
                "hotel_1",
                "user_1",
                new BigDecimal("450.00"),
                "gbp"
        );

        assertFalse(result.isVerified());
        assertTrue(result.getFailureReason().contains("payment intent not succeeded. Status: requires_payment_method"));
    }

    @Test
    @DisplayName("StripePaymentGatewayService rejects intent when metadata does not match")
    @SuppressWarnings("unchecked")
    void testStripeVerifyPaymentMetadataMismatch() throws Exception {
        String jsonResponse = """
                {
                    "id": "pi_stripe_mismatch",
                    "amount": 45000,
                    "currency": "gbp",
                    "status": "succeeded",
                    "metadata": {
                        "hotelId": "other_hotel",
                        "userId": "other_user"
                    }
                }
                """;

        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(jsonResponse);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        StripePaymentGatewayService service = new StripePaymentGatewayService(
                "sk_test_mock_secret",
                "https://api.stripe.com",
                mockHttpClient,
                objectMapper
        );

        PaymentVerificationResult result = service.verifyPayment(
                "pi_stripe_mismatch",
                "hotel_1",
                "user_1",
                new BigDecimal("450.00"),
                "gbp"
        );

        assertFalse(result.isVerified());
        assertTrue(result.getFailureReason().contains("payment intent mismatch"));
    }

    @Test
    @DisplayName("StripePaymentGatewayService rejects intent when amount does not match")
    @SuppressWarnings("unchecked")
    void testStripeVerifyPaymentAmountMismatch() throws Exception {
        String jsonResponse = """
                {
                    "id": "pi_stripe_wrong_amount",
                    "amount": 20000,
                    "currency": "gbp",
                    "status": "succeeded",
                    "metadata": {
                        "hotelId": "hotel_1",
                        "userId": "user_1"
                    }
                }
                """;

        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(jsonResponse);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        StripePaymentGatewayService service = new StripePaymentGatewayService(
                "sk_test_mock_secret",
                "https://api.stripe.com",
                mockHttpClient,
                objectMapper
        );

        PaymentVerificationResult result = service.verifyPayment(
                "pi_stripe_wrong_amount",
                "hotel_1",
                "user_1",
                new BigDecimal("450.00"),
                "gbp"
        );

        assertFalse(result.isVerified());
        assertTrue(result.getFailureReason().contains("amount mismatch"));
    }

    @Test
    @DisplayName("StripePaymentGatewayService rejects intent not found (404)")
    @SuppressWarnings("unchecked")
    void testStripeVerifyPaymentNotFound() throws Exception {
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(404);
        when(mockResponse.body()).thenReturn("{\"error\": {\"code\": \"resource_missing\"}}");
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        StripePaymentGatewayService service = new StripePaymentGatewayService(
                "sk_test_mock_secret",
                "https://api.stripe.com",
                mockHttpClient,
                objectMapper
        );

        PaymentVerificationResult result = service.verifyPayment(
                "pi_non_existent",
                "hotel_1",
                "user_1",
                new BigDecimal("450.00"),
                "gbp"
        );

        assertFalse(result.isVerified());
        assertEquals("payment intent not found", result.getFailureReason());
    }

    // =========================================================================
    // Stripe Refund Processing & Idempotency Header
    // =========================================================================

    @Test
    @DisplayName("StripePaymentGatewayService passes Idempotency-Key header and refunds £125.50 (12550 pence)")
    @SuppressWarnings("unchecked")
    void testStripeProcessRefundIdempotencyHeaderAndPrecision() throws Exception {
        String jsonResponse = """
                {
                    "id": "re_stripe_refund_12550",
                    "amount": 12550,
                    "currency": "gbp",
                    "status": "succeeded"
                }
                """;

        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(jsonResponse);

        ArgumentCaptor<HttpRequest> reqCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        when(mockHttpClient.send(reqCaptor.capture(), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        StripePaymentGatewayService service = new StripePaymentGatewayService(
                "sk_test_mock_secret",
                "https://api.stripe.com",
                mockHttpClient,
                objectMapper
        );

        RefundResult result = service.processRefund(
                "pi_stripe_12550",
                new BigDecimal("125.50"),
                "idemp_booking_12550_key"
        );

        assertTrue(result.isSuccess());
        assertEquals("re_stripe_refund_12550", result.getRefundId());
        assertEquals(new BigDecimal("125.50"), result.getRefundAmount());
        assertNull(result.getRefundSkipped());
        assertFalse(result.isSimulated());

        // Verify Idempotency-Key header was attached
        HttpRequest request = reqCaptor.getValue();
        assertEquals("idemp_booking_12550_key", request.headers().firstValue("Idempotency-Key").orElse(""));
    }

    @Test
    @DisplayName("StripePaymentGatewayService returns failure when Stripe refund API fails")
    @SuppressWarnings("unchecked")
    void testStripeProcessRefundFailure() throws Exception {
        String errorJson = """
                {
                    "error": {
                        "message": "The payment intent has already been refunded"
                    }
                }
                """;

        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(400);
        when(mockResponse.body()).thenReturn(errorJson);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        StripePaymentGatewayService service = new StripePaymentGatewayService(
                "sk_test_mock_secret",
                "https://api.stripe.com",
                mockHttpClient,
                objectMapper
        );

        RefundResult result = service.processRefund("pi_stripe_already_refunded", new BigDecimal("450.00"), "idemp_test");
        assertFalse(result.isSuccess());
        assertTrue(result.getFailureReason().contains("already been refunded"));
    }

    @Test
    @DisplayName("StripePaymentGatewayService handles null or empty payment intent as skipped refund")
    void testStripeProcessRefundNoIntentOnRecord() {
        StripePaymentGatewayService service = new StripePaymentGatewayService(
                "sk_test_mock_secret",
                "https://api.stripe.com",
                mockHttpClient,
                objectMapper
        );

        RefundResult result = service.processRefund(null, new BigDecimal("450.00"), "idemp_test");
        assertTrue(result.isSuccess());
        assertNotNull(result.getRefundSkipped());
        assertTrue(result.getRefundSkipped().contains("no payment intent on file"));
    }

    // =========================================================================
    // Simulation Mode Verification & Idempotency Replay
    // =========================================================================

    @Test
    @DisplayName("SimulatedPaymentGatewayService handles £125.50 and £99.99 with exact precision")
    void testSimulatedPaymentPrecisionValues() {
        SimulatedPaymentGatewayService sim = new SimulatedPaymentGatewayService();

        PaymentIntentResult pi1 = sim.createPaymentIntent("h1", "u1", new BigDecimal("125.50"), "gbp");
        assertEquals(new BigDecimal("125.50"), pi1.getAmount());

        PaymentVerificationResult v1 = sim.verifyPayment(pi1.getId(), "h1", "u1", new BigDecimal("125.50"), "gbp");
        assertTrue(v1.isVerified());
        assertEquals(new BigDecimal("125.50"), v1.getAmount());

        PaymentIntentResult pi2 = sim.createPaymentIntent("h1", "u1", new BigDecimal("99.99"), "gbp");
        assertEquals(new BigDecimal("99.99"), pi2.getAmount());

        PaymentVerificationResult v2 = sim.verifyPayment(pi2.getId(), "h1", "u1", new BigDecimal("99.99"), "gbp");
        assertTrue(v2.isVerified());
        assertEquals(new BigDecimal("99.99"), v2.getAmount());
    }

    @Test
    @DisplayName("SimulatedPaymentGatewayService replays identical refund on duplicate idempotency key")
    void testSimulatedRefundIdempotencyReplay() {
        SimulatedPaymentGatewayService sim = new SimulatedPaymentGatewayService();
        String idempotencyKey = "idemp_replay_test_123";

        // First refund call
        RefundResult first = sim.processRefund("pi_sim_123", new BigDecimal("99.99"), idempotencyKey);
        assertTrue(first.isSuccess());
        assertEquals(new BigDecimal("99.99"), first.getRefundAmount());
        assertNotNull(first.getRefundId());

        // Second refund call with identical idempotency key
        RefundResult second = sim.processRefund("pi_sim_123", new BigDecimal("99.99"), idempotencyKey);
        assertTrue(second.isSuccess());
        // Must return the exact same refundId without re-generating
        assertEquals(first.getRefundId(), second.getRefundId());
        assertEquals(new BigDecimal("99.99"), second.getRefundAmount());
    }
}
