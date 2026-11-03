package com.hotel.booking.dtos.businessinsights;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PopularDestinationDto {

    @JsonProperty("_id")
    private String id;

    private Long count = 0L;
    private Double totalRevenue = 0.0;
    private Double avgPrice = 0.0;

    public PopularDestinationDto() {
    }

    public PopularDestinationDto(String id, Long count, Double totalRevenue, Double avgPrice) {
        this.id = id;
        this.count = count;
        this.totalRevenue = totalRevenue;
        this.avgPrice = avgPrice;
    }

    @JsonProperty("_id")
    public String getId() { return id; }

    @JsonProperty("_id")
    public void setId(String id) { this.id = id; }

    public Long getCount() { return count; }
    public void setCount(Long count) { this.count = count; }

    public Double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(Double totalRevenue) { this.totalRevenue = totalRevenue; }

    public Double getAvgPrice() { return avgPrice; }
    public void setAvgPrice(Double avgPrice) { this.avgPrice = avgPrice; }
}

