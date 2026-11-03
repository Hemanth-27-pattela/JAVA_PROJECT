package com.hotel.booking.dtos.businessinsights;

public class OverviewMetricsDto {
    private Long totalHotels = 0L;
    private Long totalUsers = 0L;
    private Long totalBookings = 0L;
    private Long recentBookings = 0L;
    private Double totalRevenue = 0.0;
    private Double recentRevenue = 0.0;
    private Double revenueGrowth = 0.0;
    private Long cancelledBookings = 0L;
    private Long confirmedBookings = 0L;
    private Long pendingBookings = 0L;
    private Long refundedBookings = 0L;
    private Double totalRefundAmount = 0.0;
    private Double cancellationRate = 0.0;
    private Long totalReviews = 0L;
    private Double avgReviewRating = 0.0;
    private Double avgLos = 0.0;
    private Double adr = 0.0;
    private Double avgPartySize = 0.0;
    private Long verifiedReviewCount = 0L;

    public OverviewMetricsDto() {
    }

    public Long getTotalHotels() { return totalHotels; }
    public void setTotalHotels(Long totalHotels) { this.totalHotels = totalHotels; }

    public Long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(Long totalUsers) { this.totalUsers = totalUsers; }

    public Long getTotalBookings() { return totalBookings; }
    public void setTotalBookings(Long totalBookings) { this.totalBookings = totalBookings; }

    public Long getRecentBookings() { return recentBookings; }
    public void setRecentBookings(Long recentBookings) { this.recentBookings = recentBookings; }

    public Double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(Double totalRevenue) { this.totalRevenue = totalRevenue; }

    public Double getRecentRevenue() { return recentRevenue; }
    public void setRecentRevenue(Double recentRevenue) { this.recentRevenue = recentRevenue; }

    public Double getRevenueGrowth() { return revenueGrowth; }
    public void setRevenueGrowth(Double revenueGrowth) { this.revenueGrowth = revenueGrowth; }

    public Long getCancelledBookings() { return cancelledBookings; }
    public void setCancelledBookings(Long cancelledBookings) { this.cancelledBookings = cancelledBookings; }

    public Long getConfirmedBookings() { return confirmedBookings; }
    public void setConfirmedBookings(Long confirmedBookings) { this.confirmedBookings = confirmedBookings; }

    public Long getPendingBookings() { return pendingBookings; }
    public void setPendingBookings(Long pendingBookings) { this.pendingBookings = pendingBookings; }

    public Long getRefundedBookings() { return refundedBookings; }
    public void setRefundedBookings(Long refundedBookings) { this.refundedBookings = refundedBookings; }

    public Double getTotalRefundAmount() { return totalRefundAmount; }
    public void setTotalRefundAmount(Double totalRefundAmount) { this.totalRefundAmount = totalRefundAmount; }

    public Double getCancellationRate() { return cancellationRate; }
    public void setCancellationRate(Double cancellationRate) { this.cancellationRate = cancellationRate; }

    public Long getTotalReviews() { return totalReviews; }
    public void setTotalReviews(Long totalReviews) { this.totalReviews = totalReviews; }

    public Double getAvgReviewRating() { return avgReviewRating; }
    public void setAvgReviewRating(Double avgReviewRating) { this.avgReviewRating = avgReviewRating; }

    public Double getAvgLos() { return avgLos; }
    public void setAvgLos(Double avgLos) { this.avgLos = avgLos; }

    public Double getAdr() { return adr; }
    public void setAdr(Double adr) { this.adr = adr; }

    public Double getAvgPartySize() { return avgPartySize; }
    public void setAvgPartySize(Double avgPartySize) { this.avgPartySize = avgPartySize; }

    public Long getVerifiedReviewCount() { return verifiedReviewCount; }
    public void setVerifiedReviewCount(Long verifiedReviewCount) { this.verifiedReviewCount = verifiedReviewCount; }
}

