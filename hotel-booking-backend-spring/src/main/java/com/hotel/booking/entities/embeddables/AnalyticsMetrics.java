package com.hotel.booking.entities.embeddables;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;

@Embeddable
public class AnalyticsMetrics {

    @Column(name = "total_bookings")
    private Long totalBookings = 0L;

    @Column(name = "total_revenue", precision = 14, scale = 2)
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    @Column(name = "total_users")
    private Long totalUsers = 0L;

    @Column(name = "total_hotels")
    private Long totalHotels = 0L;

    @Column(name = "average_booking_value", precision = 10, scale = 2)
    private BigDecimal averageBookingValue = BigDecimal.ZERO;

    @Column(name = "conversion_rate")
    private Double conversionRate = 0.0;

    @Column(name = "cancellation_rate")
    private Double cancellationRate = 0.0;

    @Column(name = "average_rating")
    private Double averageRating = 0.0;

    public AnalyticsMetrics() {
    }

    public AnalyticsMetrics(Long totalBookings, BigDecimal totalRevenue, Long totalUsers, Long totalHotels,
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

