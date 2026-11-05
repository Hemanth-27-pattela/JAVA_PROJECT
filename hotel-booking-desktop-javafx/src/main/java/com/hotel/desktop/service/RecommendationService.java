package com.hotel.desktop.service;

import com.hotel.desktop.model.RecommendationDto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Client service interacting with Spring Boot recommendation endpoints.
 * Consumes personalized, popular, top-rated, and similar hotel recommendations.
 */
public class RecommendationService {

    private final ApiClient apiClient;

    public RecommendationService() {
        this(new ApiClient());
    }

    public RecommendationService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Retrieves personalized recommendations for the currently logged-in guest.
     * Authenticated endpoint requiring a valid guest JWT Bearer token.
     *
     * @param limit maximum number of recommendations to retrieve (default: 6)
     * @return list of personalized RecommendationDto objects
     * @throws ApiException on communication or authentication error
     */
    public List<RecommendationDto> getPersonalizedRecommendations(int limit) throws ApiException {
        int targetLimit = limit > 0 ? limit : 6;
        String endpoint = "/api/recommendations/personalized?limit=" + targetLimit;
        RecommendationDto[] array = apiClient.get(endpoint, RecommendationDto[].class, true);
        if (array == null || array.length == 0) {
            return Collections.emptyList();
        }
        return new ArrayList<>(Arrays.asList(array));
    }

    /**
     * Retrieves trending and popular hotels ranked by verified booking frequency.
     * Public endpoint.
     *
     * @param limit maximum number of hotels to retrieve (default: 6)
     * @return list of popular RecommendationDto objects
     * @throws ApiException on communication error
     */
    public List<RecommendationDto> getPopularRecommendations(int limit) throws ApiException {
        int targetLimit = limit > 0 ? limit : 6;
        String endpoint = "/api/recommendations/popular?limit=" + targetLimit;
        RecommendationDto[] array = apiClient.get(endpoint, RecommendationDto[].class, false);
        if (array == null || array.length == 0) {
            return Collections.emptyList();
        }
        return new ArrayList<>(Arrays.asList(array));
    }

    /**
     * Retrieves top-rated hotels ranked by Bayesian-weighted guest reviews.
     * Public endpoint.
     *
     * @param limit maximum number of hotels to retrieve (default: 6)
     * @return list of top-rated RecommendationDto objects
     * @throws ApiException on communication error
     */
    public List<RecommendationDto> getTopRatedRecommendations(int limit) throws ApiException {
        int targetLimit = limit > 0 ? limit : 6;
        String endpoint = "/api/recommendations/top-rated?limit=" + targetLimit;
        RecommendationDto[] array = apiClient.get(endpoint, RecommendationDto[].class, false);
        if (array == null || array.length == 0) {
            return Collections.emptyList();
        }
        return new ArrayList<>(Arrays.asList(array));
    }

    /**
     * Retrieves similar hotels matching the content features of a specific hotel.
     * Public endpoint.
     *
     * @param hotelId ID of reference hotel
     * @param limit   maximum number of similar hotels to retrieve (default: 4)
     * @return list of similar RecommendationDto objects
     * @throws ApiException on communication error
     */
    public List<RecommendationDto> getSimilarRecommendations(String hotelId, int limit) throws ApiException {
        if (hotelId == null || hotelId.trim().isEmpty()) {
            throw new ApiException(400, "Hotel ID cannot be empty");
        }
        int targetLimit = limit > 0 ? limit : 4;
        String endpoint = "/api/recommendations/similar/" + hotelId.trim() + "?limit=" + targetLimit;
        RecommendationDto[] array = apiClient.get(endpoint, RecommendationDto[].class, false);
        if (array == null || array.length == 0) {
            return Collections.emptyList();
        }
        return new ArrayList<>(Arrays.asList(array));
    }
}

