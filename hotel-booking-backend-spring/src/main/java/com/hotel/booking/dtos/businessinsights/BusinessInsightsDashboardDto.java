package com.hotel.booking.dtos.businessinsights;

import java.util.ArrayList;
import java.util.List;

public class BusinessInsightsDashboardDto {
    private OverviewMetricsDto overview = new OverviewMetricsDto();
    private List<PopularDestinationDto> popularDestinations = new ArrayList<>();
    private List<DailyBookingDto> dailyBookings = new ArrayList<>();
    private List<HotelPerformanceDto> hotelPerformance = new ArrayList<>();
    private List<StatusCountDto> bookingStatusBreakdown = new ArrayList<>();
    private List<StatusCountDto> paymentStatusBreakdown = new ArrayList<>();
    private GuestMixDto guestMix = new GuestMixDto();
    private ReviewCategoryAveragesDto reviewCategoryAverages = new ReviewCategoryAveragesDto();
    private List<HotelsByStarDto> hotelsByStar = new ArrayList<>();
    private String lastUpdated;

    public BusinessInsightsDashboardDto() {
    }

    public OverviewMetricsDto getOverview() { return overview; }
    public void setOverview(OverviewMetricsDto overview) { this.overview = overview; }

    public List<PopularDestinationDto> getPopularDestinations() { return popularDestinations; }
    public void setPopularDestinations(List<PopularDestinationDto> popularDestinations) { this.popularDestinations = popularDestinations; }

    public List<DailyBookingDto> getDailyBookings() { return dailyBookings; }
    public void setDailyBookings(List<DailyBookingDto> dailyBookings) { this.dailyBookings = dailyBookings; }

    public List<HotelPerformanceDto> getHotelPerformance() { return hotelPerformance; }
    public void setHotelPerformance(List<HotelPerformanceDto> hotelPerformance) { this.hotelPerformance = hotelPerformance; }

    public List<StatusCountDto> getBookingStatusBreakdown() { return bookingStatusBreakdown; }
    public void setBookingStatusBreakdown(List<StatusCountDto> bookingStatusBreakdown) { this.bookingStatusBreakdown = bookingStatusBreakdown; }

    public List<StatusCountDto> getPaymentStatusBreakdown() { return paymentStatusBreakdown; }
    public void setPaymentStatusBreakdown(List<StatusCountDto> paymentStatusBreakdown) { this.paymentStatusBreakdown = paymentStatusBreakdown; }

    public GuestMixDto getGuestMix() { return guestMix; }
    public void setGuestMix(GuestMixDto guestMix) { this.guestMix = guestMix; }

    public ReviewCategoryAveragesDto getReviewCategoryAverages() { return reviewCategoryAverages; }
    public void setReviewCategoryAverages(ReviewCategoryAveragesDto reviewCategoryAverages) { this.reviewCategoryAverages = reviewCategoryAverages; }

    public List<HotelsByStarDto> getHotelsByStar() { return hotelsByStar; }
    public void setHotelsByStar(List<HotelsByStarDto> hotelsByStar) { this.hotelsByStar = hotelsByStar; }

    public String getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(String lastUpdated) { this.lastUpdated = lastUpdated; }
}

