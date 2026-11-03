package com.hotel.booking.dtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class HotelDto {

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
    private Instant lastUpdated;

    private HotelLocationDto location = new HotelLocationDto();
    private HotelContactDto contact = new HotelContactDto();
    private HotelPoliciesDto policies = new HotelPoliciesDto();
    private HotelAmenitiesDto amenities = new HotelAmenitiesDto();

    private Integer totalBookings = 0;
    private BigDecimal totalRevenue = BigDecimal.ZERO;
    private Double averageRating = 0.0;
    private Integer reviewCount = 0;

    /** Owner dashboard fields */
    private Integer upcomingBookings = 0;
    private Integer completedBookings = 0;
    private Integer cancelledBookings = 0;
    private Double occupancyRate = 0.0;

    private Boolean isActive = true;
    private Boolean isFeatured = false;
    private Instant createdAt;
    private Instant updatedAt;

    public HotelDto() {
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getType() {
        return type;
    }

    public void setType(List<String> type) {
        this.type = type != null ? type : new ArrayList<>();
    }

    public Integer getAdultCount() {
        return adultCount;
    }

    public void setAdultCount(Integer adultCount) {
        this.adultCount = adultCount;
    }

    public Integer getChildCount() {
        return childCount;
    }

    public void setChildCount(Integer childCount) {
        this.childCount = childCount;
    }

    public List<String> getFacilities() {
        return facilities;
    }

    public void setFacilities(List<String> facilities) {
        this.facilities = facilities != null ? facilities : new ArrayList<>();
    }

    public BigDecimal getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(BigDecimal pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public Integer getStarRating() {
        return starRating;
    }

    public void setStarRating(Integer starRating) {
        this.starRating = starRating;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>();
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Instant lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public HotelLocationDto getLocation() {
        return location;
    }

    public void setLocation(HotelLocationDto location) {
        this.location = location != null ? location : new HotelLocationDto();
    }

    public HotelContactDto getContact() {
        return contact;
    }

    public void setContact(HotelContactDto contact) {
        this.contact = contact != null ? contact : new HotelContactDto();
    }

    public HotelPoliciesDto getPolicies() {
        return policies;
    }

    public void setPolicies(HotelPoliciesDto policies) {
        this.policies = policies != null ? policies : new HotelPoliciesDto();
    }

    public HotelAmenitiesDto getAmenities() {
        return amenities;
    }

    public void setAmenities(HotelAmenitiesDto amenities) {
        this.amenities = amenities != null ? amenities : new HotelAmenitiesDto();
    }

    public Integer getTotalBookings() {
        return totalBookings;
    }

    public void setTotalBookings(Integer totalBookings) {
        this.totalBookings = totalBookings;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
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

    public Integer getUpcomingBookings() {
        return upcomingBookings;
    }

    public void setUpcomingBookings(Integer upcomingBookings) {
        this.upcomingBookings = upcomingBookings;
    }

    public Integer getCompletedBookings() {
        return completedBookings;
    }

    public void setCompletedBookings(Integer completedBookings) {
        this.completedBookings = completedBookings;
    }

    public Integer getCancelledBookings() {
        return cancelledBookings;
    }

    public void setCancelledBookings(Integer cancelledBookings) {
        this.cancelledBookings = cancelledBookings;
    }

    public Double getOccupancyRate() {
        return occupancyRate;
    }

    public void setOccupancyRate(Double occupancyRate) {
        this.occupancyRate = occupancyRate;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public Boolean getIsFeatured() {
        return isFeatured;
    }

    public void setIsFeatured(Boolean featured) {
        isFeatured = featured;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static HotelDto fromEntity(com.hotel.booking.entities.Hotel hotel) {
        if (hotel == null) {
            return null;
        }
        HotelDto dto = new HotelDto();
        dto.setId(hotel.getId());
        dto.setUserId(hotel.getUser() != null ? hotel.getUser().getId() : hotel.getUserId());
        dto.setName(hotel.getName());
        dto.setCity(hotel.getCity());
        dto.setCountry(hotel.getCountry());
        dto.setDescription(hotel.getDescription());
        try {
            if (hotel.getType() != null) {
                dto.setType(new ArrayList<>(hotel.getType()));
            }
        } catch (Exception ignored) {}
        dto.setAdultCount(hotel.getAdultCount());
        dto.setChildCount(hotel.getChildCount());
        try {
            if (hotel.getFacilities() != null) {
                dto.setFacilities(new ArrayList<>(hotel.getFacilities()));
            }
        } catch (Exception ignored) {}
        dto.setPricePerNight(hotel.getPricePerNight());
        dto.setStarRating(hotel.getStarRating());
        try {
            if (hotel.getImageUrls() != null) {
                dto.setImageUrls(new ArrayList<>(hotel.getImageUrls()));
            }
        } catch (Exception ignored) {}
        dto.setLastUpdated(hotel.getLastUpdated());

        if (hotel.getLocation() != null) {
            AddressDto addressDto = new AddressDto();
            if (hotel.getLocation().getAddress() != null) {
                addressDto = new AddressDto(
                    hotel.getLocation().getAddress().getStreet(),
                    hotel.getLocation().getAddress().getCity(),
                    hotel.getLocation().getAddress().getState(),
                    hotel.getLocation().getAddress().getCountry(),
                    hotel.getLocation().getAddress().getZipCode()
                );
            }
            dto.setLocation(new HotelLocationDto(
                hotel.getLocation().getLatitude(),
                hotel.getLocation().getLongitude(),
                addressDto
            ));
        }

        if (hotel.getContact() != null) {
            dto.setContact(new HotelContactDto(
                hotel.getContact().getPhone(),
                hotel.getContact().getEmail(),
                hotel.getContact().getWebsite()
            ));
        }

        if (hotel.getPolicies() != null) {
            dto.setPolicies(new HotelPoliciesDto(
                hotel.getPolicies().getCheckInTime(),
                hotel.getPolicies().getCheckOutTime(),
                hotel.getPolicies().getCancellationPolicy(),
                hotel.getPolicies().getPetPolicy(),
                hotel.getPolicies().getSmokingPolicy()
            ));
        }

        if (hotel.getAmenities() != null) {
            HotelAmenitiesDto amenitiesDto = new HotelAmenitiesDto();
            amenitiesDto.setParking(hotel.getAmenities().getParking());
            amenitiesDto.setWifi(hotel.getAmenities().getWifi());
            amenitiesDto.setPool(hotel.getAmenities().getPool());
            amenitiesDto.setGym(hotel.getAmenities().getGym());
            amenitiesDto.setSpa(hotel.getAmenities().getSpa());
            amenitiesDto.setRestaurant(hotel.getAmenities().getRestaurant());
            amenitiesDto.setBar(hotel.getAmenities().getBar());
            amenitiesDto.setAirportShuttle(hotel.getAmenities().getAirportShuttle());
            amenitiesDto.setBusinessCenter(hotel.getAmenities().getBusinessCenter());
            dto.setAmenities(amenitiesDto);
        }

        dto.setTotalBookings(hotel.getTotalBookings());
        dto.setTotalRevenue(hotel.getTotalRevenue());
        dto.setAverageRating(hotel.getAverageRating());
        dto.setReviewCount(hotel.getReviewCount());
        dto.setOccupancyRate(hotel.getOccupancyRate());
        dto.setIsActive(hotel.getIsActive());
        dto.setIsFeatured(hotel.getIsFeatured());
        dto.setCreatedAt(hotel.getCreatedAt());
        dto.setUpdatedAt(hotel.getUpdatedAt());

        return dto;
    }
}

