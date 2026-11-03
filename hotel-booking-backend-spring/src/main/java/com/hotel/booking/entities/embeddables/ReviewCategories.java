package com.hotel.booking.entities.embeddables;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Embeddable
public class ReviewCategories {

    @Min(1)
    @Max(5)
    @Column(name = "cat_cleanliness", nullable = false)
    private Integer cleanliness = 5;

    @Min(1)
    @Max(5)
    @Column(name = "cat_service", nullable = false)
    private Integer service = 5;

    @Min(1)
    @Max(5)
    @Column(name = "cat_location", nullable = false)
    private Integer location = 5;

    @Min(1)
    @Max(5)
    @Column(name = "cat_value", nullable = false)
    private Integer value = 5;

    @Min(1)
    @Max(5)
    @Column(name = "cat_amenities", nullable = false)
    private Integer amenities = 5;

    public ReviewCategories() {
    }

    public ReviewCategories(Integer cleanliness, Integer service, Integer location, Integer value, Integer amenities) {
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

