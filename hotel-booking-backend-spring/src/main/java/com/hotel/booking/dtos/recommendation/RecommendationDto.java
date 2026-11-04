package com.hotel.booking.dtos.recommendation;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hotel.booking.entities.Hotel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Data transfer object representing a scored and explained hotel recommendation.
 */
public class RecommendationDto {

    @JsonProperty("_id")
    private String id;

    private String name;
    private String city;
    private String country;
    private BigDecimal pricePerNight = BigDecimal.ZERO;
    private Double averageRating = 0.0;
    private Integer reviewCount = 0;
    private Integer starRating = 1;
    private String imageUrl;
    private List<String> imageUrls = new ArrayList<>();
    private List<String> type = new ArrayList<>();
    private List<String> facilities = new ArrayList<>();

    private Double score = 0.0;
    private RecommendationCategory category;
    private RecommendationExplanationDto explanation;

    public RecommendationDto() {
    }

    public static RecommendationDto fromHotel(
            Hotel hotel,
            double rawScore,
            RecommendationCategory category,
            RecommendationExplanationDto explanation
    ) {
        RecommendationDto dto = new RecommendationDto();
        if (hotel != null) {
            dto.setId(hotel.getId());
            dto.setName(hotel.getName());
            dto.setCity(hotel.getCity());
            dto.setCountry(hotel.getCountry());
            dto.setPricePerNight(hotel.getPricePerNight() != null ? hotel.getPricePerNight() : BigDecimal.ZERO);
            dto.setAverageRating(hotel.getAverageRating() != null ? hotel.getAverageRating() : 0.0);
            dto.setReviewCount(hotel.getReviewCount() != null ? hotel.getReviewCount() : 0);
            dto.setStarRating(hotel.getStarRating() != null ? hotel.getStarRating() : 1);

            if (hotel.getImageUrls() != null && !hotel.getImageUrls().isEmpty()) {
                dto.setImageUrl(hotel.getImageUrls().get(0));
                dto.setImageUrls(new ArrayList<>(hotel.getImageUrls()));
            }

            if (hotel.getType() != null) {
                dto.setType(new ArrayList<>(hotel.getType()));
            }

            if (hotel.getFacilities() != null) {
                dto.setFacilities(new ArrayList<>(hotel.getFacilities()));
            }
        }

        double normalizedScore = Math.max(0.0, Math.min(1.0, rawScore));
        dto.setScore(BigDecimal.valueOf(normalizedScore).setScale(4, RoundingMode.HALF_UP).doubleValue());
        dto.setCategory(category);
        dto.setExplanation(explanation);
        return dto;
    }

    @JsonProperty("_id")
    public String getId() {
        return id;
    }

    @JsonProperty("_id")
    @JsonAlias("id")
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public BigDecimal getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(BigDecimal pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Integer getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(Integer reviewCount) {
        this.reviewCount = reviewCount;
    }

    public Integer getStarRating() {
        return starRating;
    }

    public void setStarRating(Integer starRating) {
        this.starRating = starRating;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>();
    }

    public List<String> getType() {
        return type;
    }

    public void setType(List<String> type) {
        this.type = type != null ? type : new ArrayList<>();
    }

    public List<String> getFacilities() {
        return facilities;
    }

    public void setFacilities(List<String> facilities) {
        this.facilities = facilities != null ? facilities : new ArrayList<>();
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public RecommendationCategory getCategory() {
        return category;
    }

    public void setCategory(RecommendationCategory category) {
        this.category = category;
    }

    public RecommendationExplanationDto getExplanation() {
        return explanation;
    }

    public void setExplanation(RecommendationExplanationDto explanation) {
        this.explanation = explanation;
    }
}

