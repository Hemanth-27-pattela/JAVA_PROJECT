package com.hotel.desktop.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Client-side model representing a Hotel property retrieved from Spring Boot API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Hotel {

    @JsonProperty("_id")
    private String id;

    private String userId;
    private String name;
    private String city;
    private String country;
    private String description;
    private List<String> type = new ArrayList<>();
    private Integer adultCount = 1;
    private Integer childCount = 0;
    private List<String> facilities = new ArrayList<>();
    private BigDecimal pricePerNight = BigDecimal.ZERO;
    private Integer starRating = 1;
    private List<String> imageUrls = new ArrayList<>();
    private Boolean isActive = true;
    private Boolean isFeatured = false;
    private Double averageRating = 0.0;
    private Integer reviewCount = 0;

    private HotelLocation location = new HotelLocation();
    private HotelContact contact = new HotelContact();
    private HotelPolicies policies = new HotelPolicies();

    public Hotel() {
    }

    public Hotel(String id, String name, String city, String country, BigDecimal pricePerNight, Integer starRating) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.country = country;
        this.pricePerNight = pricePerNight;
        this.starRating = starRating;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name != null ? name : "";
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

    public String getDescription() {
        return description != null ? description : "";
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getType() {
        return type != null ? type : new ArrayList<>();
    }

    public void setType(List<String> type) {
        this.type = type != null ? type : new ArrayList<>();
    }

    public Integer getAdultCount() {
        return adultCount != null ? adultCount : 1;
    }

    public void setAdultCount(Integer adultCount) {
        this.adultCount = adultCount;
    }

    public Integer getChildCount() {
        return childCount != null ? childCount : 0;
    }

    public void setChildCount(Integer childCount) {
        this.childCount = childCount;
    }

    public List<String> getFacilities() {
        return facilities != null ? facilities : new ArrayList<>();
    }

    public void setFacilities(List<String> facilities) {
        this.facilities = facilities != null ? facilities : new ArrayList<>();
    }

    public BigDecimal getPricePerNight() {
        return pricePerNight != null ? pricePerNight : BigDecimal.ZERO;
    }

    public void setPricePerNight(BigDecimal pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public Integer getStarRating() {
        return starRating != null ? starRating : 1;
    }

    public void setStarRating(Integer starRating) {
        this.starRating = starRating;
    }

    public List<String> getImageUrls() {
        return imageUrls != null ? imageUrls : new ArrayList<>();
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>();
    }

    public Boolean getIsActive() {
        return isActive != null ? isActive : true;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public Boolean getIsFeatured() {
        return isFeatured != null ? isFeatured : false;
    }

    public void setIsFeatured(Boolean featured) {
        isFeatured = featured;
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

    public HotelLocation getLocation() {
        return location != null ? location : new HotelLocation();
    }

    public void setLocation(HotelLocation location) {
        this.location = location;
    }

    public HotelContact getContact() {
        return contact != null ? contact : new HotelContact();
    }

    public void setContact(HotelContact contact) {
        this.contact = contact;
    }

    public HotelPolicies getPolicies() {
        return policies != null ? policies : new HotelPolicies();
    }

    public void setPolicies(HotelPolicies policies) {
        this.policies = policies;
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

    public String getFirstImageUrl() {
        if (imageUrls != null && !imageUrls.isEmpty()) {
            for (String url : imageUrls) {
                if (url != null && !url.trim().isEmpty()) {
                    return url.trim();
                }
            }
        }
        return null;
    }

    public String getTypesDisplay() {
        if (type == null || type.isEmpty()) return "Hotel";
        return String.join(", ", type);
    }

    public String getFacilitiesDisplay() {
        if (facilities == null || facilities.isEmpty()) return "Standard amenities";
        return String.join(" • ", facilities);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HotelLocation {
        private Double latitude;
        private Double longitude;
        private Address address = new Address();

        public Double getLatitude() { return latitude; }
        public void setLatitude(Double latitude) { this.latitude = latitude; }
        public Double getLongitude() { return longitude; }
        public void setLongitude(Double longitude) { this.longitude = longitude; }
        public Address getAddress() { return address != null ? address : new Address(); }
        public void setAddress(Address address) { this.address = address; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Address {
        private String street;
        private String postalCode;

        public String getStreet() { return street != null ? street : ""; }
        public void setStreet(String street) { this.street = street; }
        public String getPostalCode() { return postalCode != null ? postalCode : ""; }
        public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HotelContact {
        private String phone;
        private String email;
        private String website;

        public String getPhone() { return phone != null ? phone : ""; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getEmail() { return email != null ? email : ""; }
        public void setEmail(String email) { this.email = email; }
        public String getWebsite() { return website != null ? website : ""; }
        public void setWebsite(String website) { this.website = website; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HotelPolicies {
        private String checkInTime;
        private String checkOutTime;
        private String cancellationPolicy;
        private String petPolicy;
        private String smokingPolicy;

        public String getCheckInTime() { return checkInTime != null ? checkInTime : "15:00"; }
        public void setCheckInTime(String checkInTime) { this.checkInTime = checkInTime; }
        public String getCheckOutTime() { return checkOutTime != null ? checkOutTime : "11:00"; }
        public void setCheckOutTime(String checkOutTime) { this.checkOutTime = checkOutTime; }
        public String getCancellationPolicy() { return cancellationPolicy != null ? cancellationPolicy : "Free cancellation up to 24h before check-in"; }
        public void setCancellationPolicy(String cancellationPolicy) { this.cancellationPolicy = cancellationPolicy; }
        public String getPetPolicy() { return petPolicy != null ? petPolicy : "No pets allowed"; }
        public void setPetPolicy(String petPolicy) { this.petPolicy = petPolicy; }
        public String getSmokingPolicy() { return smokingPolicy != null ? smokingPolicy : "Non-smoking"; }
        public void setSmokingPolicy(String smokingPolicy) { this.smokingPolicy = smokingPolicy; }
    }
}
