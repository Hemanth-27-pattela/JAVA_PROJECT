package com.hotel.booking.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class HealthResponse {

    private String status;
    private String timestamp;
    private DatabaseHealth database;
    private PerformanceHealth performance;
    private String error;

    public HealthResponse() {}

    public HealthResponse(String status, String timestamp, DatabaseHealth database) {
        this.status = status;
        this.timestamp = timestamp;
        this.database = database;
    }

    public HealthResponse(String status, String timestamp, DatabaseHealth database, PerformanceHealth performance) {
        this.status = status;
        this.timestamp = timestamp;
        this.database = database;
        this.performance = performance;
    }

    public static class DatabaseHealth {
        private String status;

        public DatabaseHealth() {}

        public DatabaseHealth(String status) {
            this.status = status;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    public static class PerformanceHealth {
        private MemoryHealth memory;
        private long uptime;

        public PerformanceHealth() {}

        public PerformanceHealth(MemoryHealth memory, long uptime) {
            this.memory = memory;
            this.uptime = uptime;
        }

        public MemoryHealth getMemory() {
            return memory;
        }

        public void setMemory(MemoryHealth memory) {
            this.memory = memory;
        }

        public long getUptime() {
            return uptime;
        }

        public void setUptime(long uptime) {
            this.uptime = uptime;
        }
    }

    public static class MemoryHealth {
        private long usedMb;
        private long totalMb;
        private int percentage;

        public MemoryHealth() {}

        public MemoryHealth(long usedMb, long totalMb, int percentage) {
            this.usedMb = usedMb;
            this.totalMb = totalMb;
            this.percentage = percentage;
        }

        public long getUsedMb() {
            return usedMb;
        }

        public void setUsedMb(long usedMb) {
            this.usedMb = usedMb;
        }

        public long getTotalMb() {
            return totalMb;
        }

        public void setTotalMb(long totalMb) {
            this.totalMb = totalMb;
        }

        public int getPercentage() {
            return percentage;
        }

        public void setPercentage(int percentage) {
            this.percentage = percentage;
        }
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public DatabaseHealth getDatabase() {
        return database;
    }

    public void setDatabase(DatabaseHealth database) {
        this.database = database;
    }

    public PerformanceHealth getPerformance() {
        return performance;
    }

    public void setPerformance(PerformanceHealth performance) {
        this.performance = performance;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}

