package com.hotel.booking.dtos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyticsBreakdownDto {

    private Map<String, Long> byStatus = new HashMap<>();
    private Map<String, Long> byPaymentStatus = new HashMap<>();
    private List<DestinationBreakdownDto> byDestination = new ArrayList<>();
    private List<HotelTypeBreakdownDto> byHotelType = new ArrayList<>();

    public AnalyticsBreakdownDto() {
        byStatus.put("pending", 0L);
        byStatus.put("confirmed", 0L);
        byStatus.put("cancelled", 0L);
        byStatus.put("completed", 0L);
        byStatus.put("refunded", 0L);

        byPaymentStatus.put("pending", 0L);
        byPaymentStatus.put("paid", 0L);
        byPaymentStatus.put("failed", 0L);
        byPaymentStatus.put("refunded", 0L);
    }

    public Map<String, Long> getByStatus() {
        return byStatus;
    }

    public void setByStatus(Map<String, Long> byStatus) {
        this.byStatus = byStatus;
    }

    public Map<String, Long> getByPaymentStatus() {
        return byPaymentStatus;
    }

    public void setByPaymentStatus(Map<String, Long> byPaymentStatus) {
        this.byPaymentStatus = byPaymentStatus;
    }

    public List<DestinationBreakdownDto> getByDestination() {
        return byDestination;
    }

    public void setByDestination(List<DestinationBreakdownDto> byDestination) {
        this.byDestination = byDestination;
    }

    public List<HotelTypeBreakdownDto> getByHotelType() {
        return byHotelType;
    }

    public void setByHotelType(List<HotelTypeBreakdownDto> byHotelType) {
        this.byHotelType = byHotelType;
    }

    public static class DestinationBreakdownDto {
        private String city;
        private Long bookings = 0L;
        private Double revenue = 0.0;

        public DestinationBreakdownDto() {
        }

        public DestinationBreakdownDto(String city, Long bookings, Double revenue) {
            this.city = city;
            this.bookings = bookings;
            this.revenue = revenue;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public Long getBookings() {
            return bookings;
        }

        public void setBookings(Long bookings) {
            this.bookings = bookings;
        }

        public Double getRevenue() {
            return revenue;
        }

        public void setRevenue(Double revenue) {
            this.revenue = revenue;
        }
    }

    public static class HotelTypeBreakdownDto {
        private String type;
        private Long bookings = 0L;
        private Double revenue = 0.0;

        public HotelTypeBreakdownDto() {
        }

        public HotelTypeBreakdownDto(String type, Long bookings, Double revenue) {
            this.type = type;
            this.bookings = bookings;
            this.revenue = revenue;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Long getBookings() {
            return bookings;
        }

        public void setBookings(Long bookings) {
            this.bookings = bookings;
        }

        public Double getRevenue() {
            return revenue;
        }

        public void setRevenue(Double revenue) {
            this.revenue = revenue;
        }
    }
}

