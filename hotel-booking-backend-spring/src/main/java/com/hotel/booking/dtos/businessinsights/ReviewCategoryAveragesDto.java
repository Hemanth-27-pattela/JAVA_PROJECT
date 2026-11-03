package com.hotel.booking.dtos.businessinsights;

public class ReviewCategoryAveragesDto {
    private Double cleanliness = 0.0;
    private Double service = 0.0;
    private Double location = 0.0;
    private Double value = 0.0;
    private Double amenities = 0.0;

    public ReviewCategoryAveragesDto() {
    }

    public ReviewCategoryAveragesDto(Double cleanliness, Double service, Double location, Double value, Double amenities) {
        this.cleanliness = cleanliness;
        this.service = service;
        this.location = location;
        this.value = value;
        this.amenities = amenities;
    }

    public Double getCleanliness() { return cleanliness; }
    public void setCleanliness(Double cleanliness) { this.cleanliness = cleanliness; }

    public Double getService() { return service; }
    public void setService(Double service) { this.service = service; }

    public Double getLocation() { return location; }
    public void setLocation(Double location) { this.location = location; }

    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }

    public Double getAmenities() { return amenities; }
    public void setAmenities(Double amenities) { this.amenities = amenities; }
}

