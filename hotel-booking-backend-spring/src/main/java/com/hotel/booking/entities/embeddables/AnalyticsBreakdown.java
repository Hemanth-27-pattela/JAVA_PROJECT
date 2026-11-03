package com.hotel.booking.entities.embeddables;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyticsBreakdown implements Serializable {

    private Map<String, Long> byStatus = new HashMap<>();
    private Map<String, Long> byPaymentStatus = new HashMap<>();
    private List<DestinationBreakdown> byDestination = new ArrayList<>();
    private List<HotelTypeBreakdown> byHotelType = new ArrayList<>();

    public AnalyticsBreakdown() {
        // Default zeroed counters
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

    public List<DestinationBreakdown> getByDestination() {
        return byDestination;
    }

    public void setByDestination(List<DestinationBreakdown> byDestination) {
        this.byDestination = byDestination;
    }

    public List<HotelTypeBreakdown> getByHotelType() {
        return byHotelType;
    }

    public void setByHotelType(List<HotelTypeBreakdown> byHotelType) {
        this.byHotelType = byHotelType;
    }

    public static class DestinationBreakdown implements Serializable {
        private String city;
        private Long bookings = 0L;
        private Double revenue = 0.0;

        public DestinationBreakdown() {
        }

        public DestinationBreakdown(String city, Long bookings, Double revenue) {
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

    public static class HotelTypeBreakdown implements Serializable {
        private String type;
        private Long bookings = 0L;
        private Double revenue = 0.0;

        public HotelTypeBreakdown() {
        }

        public HotelTypeBreakdown(String type, Long bookings, Double revenue) {
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

