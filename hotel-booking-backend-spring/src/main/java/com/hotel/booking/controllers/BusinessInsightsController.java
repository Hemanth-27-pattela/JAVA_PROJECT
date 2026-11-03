package com.hotel.booking.controllers;

import com.hotel.booking.dtos.AnalyticsSnapshotDto;
import com.hotel.booking.dtos.businessinsights.BusinessInsightsDashboardDto;
import com.hotel.booking.dtos.businessinsights.BusinessInsightsForecastDto;
import com.hotel.booking.dtos.businessinsights.SystemStatsDto;
import com.hotel.booking.entities.enums.UserRole;
import com.hotel.booking.security.UserPrincipal;
import com.hotel.booking.services.BusinessInsightsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/business-insights")
@PreAuthorize("hasRole('HOTEL_OWNER') or hasRole('ADMIN')")
public class BusinessInsightsController {

    private final BusinessInsightsService businessInsightsService;

    public BusinessInsightsController(BusinessInsightsService businessInsightsService) {
        this.businessInsightsService = businessInsightsService;
    }

    /**
     * Dashboard data: scoped to owner's hotels for HOTEL_OWNER, platform-wide for ADMIN.
     * GET /api/business-insights/dashboard
     * GET /api/business-insights/dashboard/public (alias requiring auth)
     */
    @GetMapping({"/dashboard", "/dashboard/public"})
    public ResponseEntity<BusinessInsightsDashboardDto> getDashboard(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        boolean isAdmin = principal != null && principal.getRole() == UserRole.ADMIN;
        String userId = principal != null ? principal.getId() : null;
        return ResponseEntity.ok(businessInsightsService.getDashboardData(userId, isAdmin));
    }

    /**
     * Forecasting data: scoped to owner's hotels for HOTEL_OWNER, platform-wide for ADMIN.
     * GET /api/business-insights/forecast
     * GET /api/business-insights/forecast/public (alias requiring auth)
     */
    @GetMapping({"/forecast", "/forecast/public"})
    public ResponseEntity<BusinessInsightsForecastDto> getForecast(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        boolean isAdmin = principal != null && principal.getRole() == UserRole.ADMIN;
        String userId = principal != null ? principal.getId() : null;
        return ResponseEntity.ok(businessInsightsService.getForecastData(userId, isAdmin));
    }

    /**
     * System & operational stats:
     * HOTEL_OWNER: database & booking counts scoped to owned properties, system telemetry omitted.
     * ADMIN: platform-wide figures with coarse JVM memory & uptime telemetry.
     * GET /api/business-insights/system-stats
     * GET /api/business-insights/system-stats/public (alias requiring auth)
     */
    @GetMapping({"/system-stats", "/system-stats/public"})
    public ResponseEntity<SystemStatsDto> getSystemStats(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        boolean isAdmin = principal != null && principal.getRole() == UserRole.ADMIN;
        String userId = principal != null ? principal.getId() : null;
        return ResponseEntity.ok(businessInsightsService.getSystemStats(userId, isAdmin));
    }

    /**
     * Admin: list business insights point-in-time rollups (sorted newest first).
     * GET /api/business-insights/rollups
     */
    @GetMapping("/rollups")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AnalyticsSnapshotDto>> getRollups(
            @RequestParam(required = false, defaultValue = "20") Integer limit
    ) {
        return ResponseEntity.ok(businessInsightsService.getRollups(limit));
    }

    /**
     * Admin: capture a live snapshot into the analytics rollups collection.
     * POST /api/business-insights/rollups
     */
    @PostMapping("/rollups")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnalyticsSnapshotDto> createRollup() {
        AnalyticsSnapshotDto created = businessInsightsService.createRollup();
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
