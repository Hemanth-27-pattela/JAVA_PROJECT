package com.hotel.booking.dtos.businessinsights;

public class TrendSummaryDto {
    private String bookingTrend = "stable";
    private String revenueTrend = "stable";

    public TrendSummaryDto() {
    }

    public TrendSummaryDto(String bookingTrend, String revenueTrend) {
        this.bookingTrend = bookingTrend;
        this.revenueTrend = revenueTrend;
    }

    public String getBookingTrend() { return bookingTrend; }
    public void setBookingTrend(String bookingTrend) { this.bookingTrend = bookingTrend; }

    public String getRevenueTrend() { return revenueTrend; }
    public void setRevenueTrend(String revenueTrend) { this.revenueTrend = revenueTrend; }
}

