package com.hotel.desktop.service;

import com.hotel.desktop.model.Hotel;
import com.hotel.desktop.model.HotelSearchParams;
import com.hotel.desktop.model.HotelSearchResponse;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Client service providing hotel catalog discovery, filtering, and detail retrieval.
 */
public class HotelService {

    private final ApiClient apiClient;

    public HotelService() {
        this(new ApiClient());
    }

    public HotelService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Searches hotels using multi-criteria query parameters (destination, dates, price, stars, facilities, etc.)
     */
    public HotelSearchResponse searchHotels(HotelSearchParams params) throws ApiException {
        String query = params != null ? params.toQueryString() : "";
        String endpoint = "/api/hotels/search" + query;
        return apiClient.get(endpoint, HotelSearchResponse.class, true);
    }

    /**
     * Retrieves full hotel details by ID.
     */
    public Hotel getHotelById(String id) throws ApiException {
        if (id == null || id.trim().isEmpty()) {
            throw new ApiException(400, "Hotel ID cannot be empty");
        }
        String endpoint = "/api/hotels/" + id.trim();
        return apiClient.get(endpoint, Hotel.class, true);
    }

    /**
     * Retrieves all active hotels.
     */
    public List<Hotel> getAllHotels() throws ApiException {
        Hotel[] hotels = apiClient.get("/api/hotels", Hotel[].class, true);
        if (hotels == null || hotels.length == 0) {
            return Collections.emptyList();
        }
        return Arrays.asList(hotels);
    }
}
