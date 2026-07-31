package com.cts.controller;

import com.cts.dto.AnalyticsDashboardDTO;
import com.cts.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "08a. Analytics & Reporting")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE_OFFICER','CORPORATE_TRAVEL_MANAGER','COMPLIANCE_OFFICER')")
    @Operation(summary = "End-to-end dashboard: users, bookings, invoices, revenue, refunds, breakdown by type")
    public ResponseEntity<AnalyticsDashboardDTO> dashboard() {
        return ResponseEntity.ok(analyticsService.getDashboard());
    }
}
