package com.hotel.desktop.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.desktop.model.LoginRequest;
import com.hotel.desktop.model.LoginResponse;
import com.hotel.desktop.model.MessageResponse;
import com.hotel.desktop.model.User;
import com.hotel.desktop.model.UserRole;
import com.hotel.desktop.util.JsonUtils;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class ApiClientTest {

    private HttpServer server;
    private String serverBaseUrl;
    private SessionManager sessionManager;
    private ObjectMapper objectMapper;
    private HttpClient realHttpClient;
    private ApiClient apiClient;

    @BeforeEach
    void setUp() throws IOException {
        // Start lightweight in-memory HTTP server on an ephemeral port
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        int port = server.getAddress().getPort();
        serverBaseUrl = "http://127.0.0.1:" + port;

        sessionManager = SessionManager.getInstance();
        sessionManager.clearSession();
        objectMapper = JsonUtils.getMapper();

        realHttpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();

        apiClient = new ApiClient(serverBaseUrl, realHttpClient, objectMapper, sessionManager);
        server.start();
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
        if (sessionManager != null) {
            sessionManager.clearSession();
        }
    }

    @Test
    @DisplayName("GET request returns 200 OK and deserializes JSON response")
    void testGetSuccess() throws Exception {
        server.createContext("/api/test", exchange -> {
            byte[] responseBytes = "{\"message\":\"Operation OK\"}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        });

        MessageResponse response = apiClient.get("/api/test", MessageResponse.class, false);

        assertNotNull(response);
        assertEquals("Operation OK", response.getMessage());
    }

    @Test
    @DisplayName("POST request sends serialized JSON payload and deserializes response")
    void testPostSuccess() throws Exception {
        server.createContext("/api/auth/login", exchange -> {
            assertEquals("POST", exchange.getRequestMethod());
            byte[] responseBytes = "{\"token\":\"jwt123\",\"userId\":\"42\",\"message\":\"Success\"}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        });

        LoginRequest request = new LoginRequest("user@hotel.com", "12345678");
        LoginResponse response = apiClient.post("/api/auth/login", request, LoginResponse.class, false);

        assertNotNull(response);
        assertEquals("jwt123", response.getToken());
        assertEquals("42", response.getUserId());
        assertEquals("Success", response.getMessage());
    }

    @Test
    @DisplayName("POST request verifies Content-Type and request body JSON serialization")
    void testJsonRequestSerialization() throws Exception {
        AtomicReference<String> receivedContentType = new AtomicReference<>();
        AtomicReference<String> receivedBody = new AtomicReference<>();

        server.createContext("/api/echo", exchange -> {
            receivedContentType.set(exchange.getRequestHeaders().getFirst("Content-Type"));
            byte[] bytes = exchange.getRequestBody().readAllBytes();
            receivedBody.set(new String(bytes, StandardCharsets.UTF_8));

            byte[] responseBytes = "{\"message\":\"Received\"}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        });

        LoginRequest request = new LoginRequest("test@hotel.com", "password123");
        MessageResponse response = apiClient.post("/api/echo", request, MessageResponse.class, false);

        assertNotNull(response);
        assertEquals("application/json", receivedContentType.get());
        assertTrue(receivedBody.get().contains("\"email\":\"test@hotel.com\""));
        assertTrue(receivedBody.get().contains("\"password\":\"password123\""));
    }

    @Test
    @DisplayName("Authenticated request attaches Authorization Bearer header")
    void testAuthenticatedRequestIncludesBearerHeader() throws Exception {
        sessionManager.setSession("jwt-sample-token-xyz", new User("1", "admin@hotel.com", "Admin", "User", UserRole.ADMIN));

        AtomicReference<String> receivedAuthHeader = new AtomicReference<>();

        server.createContext("/api/users/me", exchange -> {
            receivedAuthHeader.set(exchange.getRequestHeaders().getFirst("Authorization"));
            byte[] responseBytes = "{\"message\":\"Protected data\"}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        });

        MessageResponse response = apiClient.get("/api/users/me", MessageResponse.class, true);

        assertNotNull(response);
        assertEquals("Bearer jwt-sample-token-xyz", receivedAuthHeader.get());
    }

    @Test
    @DisplayName("Unauthenticated request does not include Authorization header")
    void testUnauthenticatedRequestOmitsBearerHeader() throws Exception {
        sessionManager.setSession("jwt-sample-token-xyz", new User("1", "admin@hotel.com", "Admin", "User", UserRole.ADMIN));

        AtomicReference<String> receivedAuthHeader = new AtomicReference<>();

        server.createContext("/api/public", exchange -> {
            receivedAuthHeader.set(exchange.getRequestHeaders().getFirst("Authorization"));
            byte[] responseBytes = "{\"message\":\"Public data\"}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        });

        MessageResponse response = apiClient.get("/api/public", MessageResponse.class, false);

        assertNotNull(response);
        assertNull(receivedAuthHeader.get(), "Unauthenticated request must not include Authorization header");
    }

    @Test
    @DisplayName("HTTP 400 Bad Request extracts error message from backend JSON")
    void testBadRequestExtractsErrorMessage() {
        server.createContext("/api/auth/login", exchange -> {
            byte[] responseBytes = "{\"message\":\"Invalid Credentials\"}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(400, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        });

        ApiException ex = assertThrows(ApiException.class, () ->
                apiClient.post("/api/auth/login", null, LoginResponse.class, false)
        );

        assertEquals(400, ex.getStatusCode());
        assertEquals("Invalid Credentials", ex.getMessage());
    }

    @Test
    @DisplayName("HTTP 401 Unauthorized extracts error message")
    void testUnauthorizedExtractsErrorMessage() {
        server.createContext("/api/secure", exchange -> {
            byte[] responseBytes = "{\"message\":\"Token expired or invalid\"}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(401, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        });

        ApiException ex = assertThrows(ApiException.class, () ->
                apiClient.get("/api/secure", MessageResponse.class, true)
        );

        assertEquals(401, ex.getStatusCode());
        assertEquals("Token expired or invalid", ex.getMessage());
    }

    @Test
    @DisplayName("HTTP 500 Internal Server Error without custom JSON returns friendly fallback")
    void testServerErrorFallbackMessage() {
        server.createContext("/api/error", exchange -> {
            exchange.sendResponseHeaders(500, -1);
        });

        ApiException ex = assertThrows(ApiException.class, () ->
                apiClient.get("/api/error", MessageResponse.class, false)
        );

        assertEquals(500, ex.getStatusCode());
        assertTrue(ex.getMessage().contains("Internal server error"));
    }

    @Test
    @DisplayName("Connection failure translates to friendly server offline message with status 0")
    void testConnectionFailureHandling() {
        // Point ApiClient to a port with no listening server
        int nonExistentPort = 59999;
        ApiClient offlineClient = new ApiClient("http://127.0.0.1:" + nonExistentPort,
                realHttpClient, objectMapper, sessionManager);

        ApiException ex = assertThrows(ApiException.class, () ->
                offlineClient.get("/api/health", MessageResponse.class, false)
        );

        assertEquals(0, ex.getStatusCode());
        assertTrue(ex.isNetworkError());
        assertTrue(ex.getMessage().contains("Unable to connect to hotel server"));
    }

    @Test
    @DisplayName("Request timeout translates to timeout message with status 0")
    void testRequestTimeoutHandling() {
        // Endpoint that deliberately delays beyond client timeout
        server.createContext("/api/slow", exchange -> {
            try {
                Thread.sleep(600);
            } catch (InterruptedException ignored) {}
            exchange.sendResponseHeaders(200, -1);
        });

        // Client with short 150ms timeout
        ApiClient timeoutClient = new ApiClient(serverBaseUrl, realHttpClient, objectMapper, sessionManager,
                Duration.ofMillis(150));

        ApiException ex = assertThrows(ApiException.class, () ->
                timeoutClient.get("/api/slow", MessageResponse.class, false)
        );

        assertEquals(0, ex.getStatusCode());
        assertTrue(ex.isNetworkError());
        assertTrue(ex.getMessage().contains("timed out"));
    }
}
