package com.hotel.desktop.config;

/**
 * Global desktop application configuration.
 */
public final class AppConfig {

    private AppConfig() {}

    public static final String DEFAULT_API_BASE_URL = "http://localhost:5001";
    public static final String API_BASE_URL_PROP = "hotel.api.base-url";

    public static String getApiBaseUrl() {
        return System.getProperty(API_BASE_URL_PROP, DEFAULT_API_BASE_URL);
    }
}

