package com.hotel.booking.dtos.businessinsights;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SystemStatsDto {
    private DatabaseStatsDto database = new DatabaseStatsDto();
    private ApplicationStatsDto application = new ApplicationStatsDto();
    private SystemMetricsDto system;
    private String lastUpdated;

    public SystemStatsDto() {
    }

    public DatabaseStatsDto getDatabase() { return database; }
    public void setDatabase(DatabaseStatsDto database) { this.database = database; }

    public ApplicationStatsDto getApplication() { return application; }
    public void setApplication(ApplicationStatsDto application) { this.application = application; }

    public SystemMetricsDto getSystem() { return system; }
    public void setSystem(SystemMetricsDto system) { this.system = system; }

    public String getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(String lastUpdated) { this.lastUpdated = lastUpdated; }

    public static class DatabaseStatsDto {
        private Long totalHotels = 0L;
        private Long totalBookings = 0L;
        private Double totalRevenue = 0.0;

        public DatabaseStatsDto() {}

        public DatabaseStatsDto(Long totalHotels, Long totalBookings, Double totalRevenue) {
            this.totalHotels = totalHotels;
            this.totalBookings = totalBookings;
            this.totalRevenue = totalRevenue;
        }

        public Long getTotalHotels() { return totalHotels; }
        public void setTotalHotels(Long totalHotels) { this.totalHotels = totalHotels; }

        public Long getTotalBookings() { return totalBookings; }
        public void setTotalBookings(Long totalBookings) { this.totalBookings = totalBookings; }

        public Double getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(Double totalRevenue) { this.totalRevenue = totalRevenue; }
    }

    public static class ApplicationStatsDto {
        private Long avgResponseTime = 75L;
        private Long requestsPerMinute = 35L;
        private Double errorRate = 0.01;
        private String uptime = "99.9%";
        private Long todayBookings = 0L;
        private Long thisWeekBookings = 0L;

        public ApplicationStatsDto() {}

        public ApplicationStatsDto(Long avgResponseTime, Long requestsPerMinute, Double errorRate, String uptime, Long todayBookings, Long thisWeekBookings) {
            this.avgResponseTime = avgResponseTime;
            this.requestsPerMinute = requestsPerMinute;
            this.errorRate = errorRate;
            this.uptime = uptime;
            this.todayBookings = todayBookings;
            this.thisWeekBookings = thisWeekBookings;
        }

        public Long getAvgResponseTime() { return avgResponseTime; }
        public void setAvgResponseTime(Long avgResponseTime) { this.avgResponseTime = avgResponseTime; }

        public Long getRequestsPerMinute() { return requestsPerMinute; }
        public void setRequestsPerMinute(Long requestsPerMinute) { this.requestsPerMinute = requestsPerMinute; }

        public Double getErrorRate() { return errorRate; }
        public void setErrorRate(Double errorRate) { this.errorRate = errorRate; }

        public String getUptime() { return uptime; }
        public void setUptime(String uptime) { this.uptime = uptime; }

        public Long getTodayBookings() { return todayBookings; }
        public void setTodayBookings(Long todayBookings) { this.todayBookings = todayBookings; }

        public Long getThisWeekBookings() { return thisWeekBookings; }
        public void setThisWeekBookings(Long thisWeekBookings) { this.thisWeekBookings = thisWeekBookings; }
    }

    public static class SystemMetricsDto {
        private MemoryDto memory = new MemoryDto();
        private Long uptime = 0L;

        public SystemMetricsDto() {}

        public SystemMetricsDto(MemoryDto memory, Long uptime) {
            this.memory = memory;
            this.uptime = uptime;
        }

        public MemoryDto getMemory() { return memory; }
        public void setMemory(MemoryDto memory) { this.memory = memory; }

        public Long getUptime() { return uptime; }
        public void setUptime(Long uptime) { this.uptime = uptime; }
    }

    public static class MemoryDto {
        private Long used = 0L;
        private Long total = 0L;
        private Integer percentage = 0;

        public MemoryDto() {}

        public MemoryDto(Long used, Long total, Integer percentage) {
            this.used = used;
            this.total = total;
            this.percentage = percentage;
        }

        public Long getUsed() { return used; }
        public void setUsed(Long used) { this.used = used; }

        public Long getTotal() { return total; }
        public void setTotal(Long total) { this.total = total; }

        public Integer getPercentage() { return percentage; }
        public void setPercentage(Integer percentage) { this.percentage = percentage; }
    }
}

