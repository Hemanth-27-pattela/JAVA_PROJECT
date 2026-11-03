package com.hotel.booking.dtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public class AnalyticsSnapshotDto {

    @JsonProperty("_id")
    private String id;

    private Instant date;
    private AnalyticsMetricsDto metrics = new AnalyticsMetricsDto();
    private AnalyticsBreakdownDto breakdown = new AnalyticsBreakdownDto();
    private Instant createdAt;
    private Instant updatedAt;

    public AnalyticsSnapshotDto() {
    }

    public AnalyticsSnapshotDto(Instant date, AnalyticsMetricsDto metrics, AnalyticsBreakdownDto breakdown) {
        this.date = date;
        this.metrics = metrics != null ? metrics : new AnalyticsMetricsDto();
        this.breakdown = breakdown != null ? breakdown : new AnalyticsBreakdownDto();
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

    public Instant getDate() {
        return date;
    }

    public void setDate(Instant date) {
        this.date = date;
    }

    public AnalyticsMetricsDto getMetrics() {
        return metrics;
    }

    public void setMetrics(AnalyticsMetricsDto metrics) {
        this.metrics = metrics != null ? metrics : new AnalyticsMetricsDto();
    }

    public AnalyticsBreakdownDto getBreakdown() {
        return breakdown;
    }

    public void setBreakdown(AnalyticsBreakdownDto breakdown) {
        this.breakdown = breakdown != null ? breakdown : new AnalyticsBreakdownDto();
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

    public static AnalyticsSnapshotDto fromEntity(com.hotel.booking.entities.AnalyticsSnapshot entity) {
        if (entity == null) {
            return null;
        }
        AnalyticsSnapshotDto dto = new AnalyticsSnapshotDto();
        dto.setId(entity.getId());
        dto.setDate(entity.getDate());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        if (entity.getMetrics() != null) {
            AnalyticsMetricsDto mDto = new AnalyticsMetricsDto(
                    entity.getMetrics().getTotalBookings(),
                    entity.getMetrics().getTotalRevenue(),
                    entity.getMetrics().getTotalUsers(),
                    entity.getMetrics().getTotalHotels(),
                    entity.getMetrics().getAverageBookingValue(),
                    entity.getMetrics().getConversionRate(),
                    entity.getMetrics().getCancellationRate(),
                    entity.getMetrics().getAverageRating()
            );
            dto.setMetrics(mDto);
        }

        if (entity.getBreakdown() != null) {
            AnalyticsBreakdownDto bDto = new AnalyticsBreakdownDto();
            bDto.setByStatus(entity.getBreakdown().getByStatus());
            bDto.setByPaymentStatus(entity.getBreakdown().getByPaymentStatus());

            if (entity.getBreakdown().getByDestination() != null) {
                java.util.List<AnalyticsBreakdownDto.DestinationBreakdownDto> destList = new java.util.ArrayList<>();
                for (com.hotel.booking.entities.embeddables.AnalyticsBreakdown.DestinationBreakdown d : entity.getBreakdown().getByDestination()) {
                    destList.add(new AnalyticsBreakdownDto.DestinationBreakdownDto(d.getCity(), d.getBookings(), d.getRevenue()));
                }
                bDto.setByDestination(destList);
            }

            if (entity.getBreakdown().getByHotelType() != null) {
                java.util.List<AnalyticsBreakdownDto.HotelTypeBreakdownDto> typeList = new java.util.ArrayList<>();
                for (com.hotel.booking.entities.embeddables.AnalyticsBreakdown.HotelTypeBreakdown t : entity.getBreakdown().getByHotelType()) {
                    typeList.add(new AnalyticsBreakdownDto.HotelTypeBreakdownDto(t.getType(), t.getBookings(), t.getRevenue()));
                }
                bDto.setByHotelType(typeList);
            }
            dto.setBreakdown(bDto);
        }

        return dto;
    }
}

