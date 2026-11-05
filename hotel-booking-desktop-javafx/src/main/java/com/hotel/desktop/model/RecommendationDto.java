package com.hotel.desktop.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Client-side Data Transfer Object representing a hotel recommendation payload from the Spring Boot API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
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
    private RecommendationCategory category = RecommendationCategory.PERSONALIZED;
    private RecommendationExplanation explanation = new RecommendationExplanation();

    public RecommendationDto() {
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
        return name != null ? name : "Hotel";
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city != null ? city : "";
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country != null ? country : "";
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public BigDecimal getPricePerNight() {
        return pricePerNight != null ? pricePerNight : BigDecimal.ZERO;
    }

    public void setPricePerNight(BigDecimal pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public Double getAverageRating() {
        return averageRating != null ? averageRating : 0.0;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Integer getReviewCount() {
        return reviewCount != null ? reviewCount : 0;
    }

    public void setReviewCount(Integer reviewCount) {
        this.reviewCount = reviewCount;
    }

    public Integer getStarRating() {
        return starRating != null ? starRating : 1;
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
        return imageUrls != null ? imageUrls : Collections.emptyList();
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls != null ? new ArrayList<>(imageUrls) : new ArrayList<>();
    }

    public List<String> getType() {
        return type != null ? type : Collections.emptyList();
    }

    public void setType(List<String> type) {
        this.type = type != null ? new ArrayList<>(type) : new ArrayList<>();
    }

    public List<String> getFacilities() {
        return facilities != null ? facilities : Collections.emptyList();
    }

    public void setFacilities(List<String> facilities) {
        this.facilities = facilities != null ? new ArrayList<>(facilities) : new ArrayList<>();
    }

    public Double getScore() {
        return score != null ? score : 0.0;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public RecommendationCategory getCategory() {
        return category != null ? category : RecommendationCategory.PERSONALIZED;
    }

    public void setCategory(RecommendationCategory category) {
        this.category = category;
    }

    public RecommendationExplanation getExplanation() {
        return explanation != null ? explanation : new RecommendationExplanation();
    }

    public void setExplanation(RecommendationExplanation explanation) {
        this.explanation = explanation;
    }

    // === UI Presentation Helpers ===

    public String getLocationDisplay() {
        String c = getCity().trim();
        String k = getCountry().trim();
        if (!c.isEmpty() && !k.isEmpty()) {
            return c + ", " + k;
        } else if (!c.isEmpty()) {
            return c;
        } else {
            return k;
        }
    }

    public String getFormattedPrice() {
        if (pricePerNight == null) {
            return "£0";
        }
        return "£" + pricePerNight.stripTrailingZeros().toPlainString();
    }

    public String getStarRatingDisplay() {
        int stars = Math.max(1, Math.min(5, getStarRating()));
        return "★".repeat(stars);
    }

    public String getRatingDisplay() {
        double r = getAverageRating();
        if (r <= 0.0) {
            return "Unrated";
        }
        return String.format(java.util.Locale.US, "★ %.1f / 5.0", r);
    }

    public String getReviewCountDisplay() {
        int count = getReviewCount();
        if (count == 1) {
            return "(1 review)";
        }
        return "(" + count + " reviews)";
    }

    public String getScorePercentageDisplay() {
        double s = getScore();
        int pct = (int) Math.round(s * 100.0);
        return pct + "% Match";
    }

    public String getFirstImageUrl() {
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            return imageUrl.trim();
        }
        if (imageUrls != null && !imageUrls.isEmpty()) {
            for (String url : imageUrls) {
                if (url != null && !url.trim().isEmpty()) {
                    return url.trim();
                }
            }
        }
        return null;
    }

    /**
     * Converts this RecommendationDto to a Hotel instance for seamless navigation into HotelDetailsController.
     */
    public Hotel toHotel() {
        Hotel h = new Hotel();
        h.setId(this.id);
        h.setName(this.getName());
        h.setCity(this.getCity());
        h.setCountry(this.getCountry());
        h.setPricePerNight(this.getPricePerNight());
        h.setStarRating(this.getStarRating());
        h.setAverageRating(this.getAverageRating());
        h.setReviewCount(this.getReviewCount());
        h.setType(new ArrayList<>(this.getType()));
        h.setFacilities(new ArrayList<>(this.getFacilities()));

        List<String> imgs = new ArrayList<>();
        if (this.imageUrl != null && !this.imageUrl.isBlank()) {
            imgs.add(this.imageUrl);
        }
        if (this.imageUrls != null) {
            for (String u : this.imageUrls) {
                if (!imgs.contains(u)) {
                    imgs.add(u);
                }
            }
        }
        h.setImageUrls(imgs);
        h.setIsActive(true);
        h.setDescription("Curated stay featuring top-tier hospitality in " + getLocationDisplay() + ".");

        Hotel.HotelPolicies policies = new Hotel.HotelPolicies();
        policies.setCheckInTime("15:00");
        policies.setCheckOutTime("11:00");
        policies.setCancellationPolicy("Free cancellation up to 24h before check-in");
        h.setPolicies(policies);

        return h;
    }
}

