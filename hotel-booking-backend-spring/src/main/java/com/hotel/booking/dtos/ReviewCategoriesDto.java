package com.hotel.booking.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ReviewCategoriesDto {

    @NotNull(message = "Cleanliness rating is required")
    @Min(value = 1, message = "Cleanliness rating must be between 1 and 5")
    @Max(value = 5, message = "Cleanliness rating must be between 1 and 5")
    private Integer cleanliness = 5;

    @NotNull(message = "Service rating is required")
    @Min(value = 1, message = "Service rating must be between 1 and 5")
    @Max(value = 5, message = "Service rating must be between 1 and 5")
    private Integer service = 5;

    @NotNull(message = "Location rating is required")
    @Min(value = 1, message = "Location rating must be between 1 and 5")
    @Max(value = 5, message = "Location rating must be between 1 and 5")
    private Integer location = 5;

    @NotNull(message = "Value rating is required")
    @Min(value = 1, message = "Value rating must be between 1 and 5")
    @Max(value = 5, message = "Value rating must be between 1 and 5")
    private Integer value = 5;

    @NotNull(message = "Amenities rating is required")
    @Min(value = 1, message = "Amenities rating must be between 1 and 5")
    @Max(value = 5, message = "Amenities rating must be between 1 and 5")
    private Integer amenities = 5;

    public ReviewCategoriesDto() {
    }

    public ReviewCategoriesDto(Integer cleanliness, Integer service, Integer location, Integer value, Integer amenities) {
        this.cleanliness = cleanliness;
        this.service = service;
        this.location = location;
        this.value = value;
        this.amenities = amenities;
    }

    public Integer getCleanliness() {
        return cleanliness;
    }

    public void setCleanliness(Integer cleanliness) {
        this.cleanliness = cleanliness;
    }

    public Integer getService() {
        return service;
    }

    public void setService(Integer service) {
        this.service = service;
    }

    public Integer getLocation() {
        return location;
    }

    public void setLocation(Integer location) {
        this.location = location;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public Integer getAmenities() {
        return amenities;
    }

    public void setAmenities(Integer amenities) {
        this.amenities = amenities;
    }
}
