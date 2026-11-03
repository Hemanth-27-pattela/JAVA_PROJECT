package com.hotel.booking.controllers;

import com.hotel.booking.dtos.HealthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.time.Instant;

@RestController
@Tag(name = "Health", description = "Liveness and readiness health checks")
public class HealthController {

    private final DataSource dataSource;
    private final long startTime;

    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
        this.startTime = System.currentTimeMillis();
    }

    @GetMapping("/")
    public String root() {
        return "<h1>Hotel Booking Backend Spring Boot API is running 🚀</h1>";
    }

    @GetMapping("/api/health")
    @Operation(summary = "Liveness / readiness probe (public)", description = "Minimal payload for load balancers and healthchecks")
    public ResponseEntity<HealthResponse> checkHealth() {
        boolean dbConnected = isDatabaseConnected();
        String dbStatus = dbConnected ? "connected" : "disconnected";

        HealthResponse response = new HealthResponse(
                dbConnected ? "healthy" : "unhealthy",
                Instant.now().toString(),
                new HealthResponse.DatabaseHealth(dbStatus)
        );

        if (!dbConnected) {
            response.setError("Health check failed: database not reachable");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/health/detailed")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Sanitized health metrics", description = "Performance and memory statistics")
    public ResponseEntity<HealthResponse> checkDetailedHealth() {
        boolean dbConnected = isDatabaseConnected();
        String dbStatus = dbConnected ? "connected" : "disconnected";

        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        long usedMb = usedMemory / (1024 * 1024);
        long totalMb = totalMemory / (1024 * 1024);
        int percentage = totalMb > 0 ? (int) Math.round(((double) usedMb / totalMb) * 100) : 0;

        long uptimeSeconds = (System.currentTimeMillis() - startTime) / 1000;

        HealthResponse.PerformanceHealth performance = new HealthResponse.PerformanceHealth(
                new HealthResponse.MemoryHealth(usedMb, totalMb, percentage),
                uptimeSeconds
        );

        HealthResponse response = new HealthResponse(
                dbConnected ? "healthy" : "unhealthy",
                Instant.now().toString(),
                new HealthResponse.DatabaseHealth(dbStatus),
                performance
        );

        if (!dbConnected) {
            response.setError("Detailed health check failed: database not reachable");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        }

        return ResponseEntity.ok(response);
    }

    private boolean isDatabaseConnected() {
        try (Connection connection = dataSource.getConnection()) {
            return connection != null && connection.isValid(2);
        } catch (Exception ex) {
            return false;
        }
    }
}

