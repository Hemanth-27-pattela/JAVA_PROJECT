package com.hotel.booking.dtos;

import java.math.BigDecimal;

public class AnalyticsMetricsDto {
    private Long totalBookings = 0L;
    private BigDecimal totalRevenue = BigDecimal.ZERO;
    private Long totalUsers = 0L;
    private Long totalHotels = 0L;
    private BigDecimal averageBookingValue = BigDecimal.ZERO;
    private Double conversionRate = 0.0;
    private Double cancellationRate = 0.0;
    private Double averageRating = 0.0;

    public AnalyticsMetricsDto() {
    }

    public AnalyticsMetricsDto(Long totalBookings, BigDecimal totalRevenue, Long totalUsers, Long totalHotels,
                               BigDecimal averageBookingValue, Double conversionRate, Double cancellationRate, Double averageRating) {
        this.totalBookings = totalBookings;
        this.totalRevenue = totalRevenue;
        this.totalUsers = totalUsers;
        this.totalHotels = totalHotels;
        this.averageBookingValue = averageBookingValue;
        this.conversionRate = conversionRate;
        this.cancellationRate = cancellationRate;
        this.averageRating = averageRating;
    }

    public Long getTotalBookings() {
        return totalBookings;
    }

    public void setTotalBookings(Long totalBookings) {
        this.totalBookings = totalBookings;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(Long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public Long getTotalHotels() {
        return totalHotels;
    }

    public void setTotalHotels(Long totalHotels) {
        this.totalHotels = totalHotels;
    }

    public BigDecimal getAverageBookingValue() {
        return averageBookingValue;
    }

    public void setAverageBookingValue(BigDecimal averageBookingValue) {
        this.averageBookingValue = averageBookingValue;
    }

    public Double getConversionRate() {
        return conversionRate;
    }

    public void setConversionRate(Double conversionRate) {
        this.conversionRate = conversionRate;
    }

    public Double getCancellationRate() {
        return cancellationRate;
    }

    public void setCancellationRate(Double cancellationRate) {
        this.cancellationRate = cancellationRate;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }
}

