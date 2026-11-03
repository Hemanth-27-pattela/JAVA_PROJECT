package com.hotel.desktop.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.desktop.config.AppConfig;
import com.hotel.desktop.util.JsonUtils;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;

/**
 * HTTP Client wrapper consuming Spring Boot REST APIs using Java standard HttpClient.
 * Manages JSON marshaling, timeouts, authorization headers, and error payload parsing.
 */
public class ApiClient {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);

    private final String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final SessionManager sessionManager;
    private final Duration requestTimeout;

    public ApiClient() {
        this(AppConfig.getApiBaseUrl(),
             HttpClient.newBuilder().connectTimeout(CONNECT_TIMEOUT).build(),
             JsonUtils.getMapper(),
             SessionManager.getInstance());
    }

    public ApiClient(String baseUrl) {
        this(baseUrl,
             HttpClient.newBuilder().connectTimeout(CONNECT_TIMEOUT).build(),
             JsonUtils.getMapper(),
             SessionManager.getInstance());
    }

    public ApiClient(String baseUrl, HttpClient httpClient, ObjectMapper objectMapper, SessionManager sessionManager) {
        this(baseUrl, httpClient, objectMapper, sessionManager, REQUEST_TIMEOUT);
    }

    public ApiClient(String baseUrl, HttpClient httpClient, ObjectMapper objectMapper, SessionManager sessionManager, Duration requestTimeout) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.sessionManager = sessionManager;
        this.requestTimeout = requestTimeout != null ? requestTimeout : REQUEST_TIMEOUT;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Executes an HTTP GET request.
     */
    public <T> T get(String endpoint, Class<T> responseType, boolean authenticated) throws ApiException {
        HttpRequest.Builder builder = createRequestBuilder(endpoint, authenticated)
                .GET();

        return sendRequest(builder.build(), responseType);
    }

    /**
     * Executes an HTTP POST request with a JSON body.
     */
    public <T> T post(String endpoint, Object body, Class<T> responseType, boolean authenticated) throws ApiException {
        try {
            String jsonPayload = body != null ? objectMapper.writeValueAsString(body) : "";
            HttpRequest.Builder builder = createRequestBuilder(endpoint, authenticated)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload));

            return sendRequest(builder.build(), responseType);
        } catch (IOException e) {
            throw new ApiException(0, "Failed to serialize request payload: " + e.getMessage(), e);
        }
    }

    private HttpRequest.Builder createRequestBuilder(String endpoint, boolean authenticated) {
        String path = endpoint.startsWith("/") ? endpoint : "/" + endpoint;
        URI uri = URI.create(baseUrl + path);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(this.requestTimeout)
                .header("Accept", "application/json");

        if (authenticated) {
            String token = sessionManager.getToken();
            if (token != null && !token.isBlank()) {
                builder.header("Authorization", "Bearer " + token);
            }
        }

        return builder;
    }

    private <T> T sendRequest(HttpRequest request, Class<T> responseType) throws ApiException {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int statusCode = response.statusCode();
            String responseBody = response.body();

            if (statusCode >= 200 && statusCode < 300) {
                if (responseType == Void.class || responseBody == null || responseBody.isBlank()) {
                    return null;
                }
                return objectMapper.readValue(responseBody, responseType);
            }

            // Extract error message from {"message": "..."} response format
            String errorMessage = extractErrorMessage(responseBody, statusCode);
            throw new ApiException(statusCode, errorMessage);

        } catch (ConnectException | HttpConnectTimeoutException e) {
            throw new ApiException(0, "Unable to connect to hotel server at " + baseUrl + ". Please ensure the backend is running.", e);
        } catch (HttpTimeoutException e) {
            throw new ApiException(0, "Request to hotel server timed out after " + REQUEST_TIMEOUT.toSeconds() + " seconds.", e);
        } catch (IOException e) {
            throw new ApiException(0, "Network communication error: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(0, "HTTP operation was interrupted", e);
        }
    }

    private String extractErrorMessage(String body, int statusCode) {
        if (body != null && !body.isBlank()) {
            try {
                JsonNode root = objectMapper.readTree(body);
                if (root.has("message") && !root.get("message").asText().isBlank()) {
                    return root.get("message").asText();
                }
                if (root.has("error") && !root.get("error").asText().isBlank()) {
                    return root.get("error").asText();
                }
            } catch (Exception ignored) {
                // Return default fallback if body is not valid JSON
            }
        }

        return switch (statusCode) {
            case 400 -> "Invalid request data. Please check your inputs.";
            case 401 -> "Invalid credentials or session expired.";
            case 403 -> "Access denied. Insufficient permissions.";
            case 404 -> "Resource not found.";
            case 409 -> "Resource already exists or state conflict.";
            case 500 -> "Internal server error. Please try again later.";
            default -> "Unexpected server error (HTTP " + statusCode + ").";
        };
    }
}

