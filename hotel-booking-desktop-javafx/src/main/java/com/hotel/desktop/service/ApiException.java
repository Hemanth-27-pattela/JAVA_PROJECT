package com.hotel.desktop.service;

/**
 * Exception representing HTTP client or server errors from the Spring Boot API.
 */
public class ApiException extends Exception {

    private final int statusCode;

    public ApiException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public ApiException(int statusCode, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public boolean isNetworkError() {
        return statusCode == 0;
    }

    public boolean isBadRequest() {
        return statusCode == 400;
    }

    public boolean isUnauthorized() {
        return statusCode == 401;
    }

    public boolean isForbidden() {
        return statusCode == 403;
    }

    public boolean isServerError() {
        return statusCode >= 500;
    }

    @Override
    public String toString() {
        return "ApiException{statusCode=" + statusCode + ", message='" + getMessage() + "'}";
    }
}

