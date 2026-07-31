package com.cts.controller;

import com.cts.dto.AuditLogResponseDTO;
import com.cts.dto.ComplianceReportDTO;
import com.cts.dto.PageResponse;
import com.cts.enumeration.EventLevel;
import com.cts.service.AuditLogQueryService;
import com.cts.util.AppConstants;
import com.cts.util.PaginationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
@Tag(name = "09. Audit Trail (Administrative)")
public class AuditLogController {

    private final AuditLogQueryService auditLogQueryService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMPLIANCE_OFFICER')")
    @Operation(summary = "Retrieves the system-wide audit trail with dynamic structural filtering options")
    public ResponseEntity<PageResponse<AuditLogResponseDTO>> getLogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) Long resourceId,
            @RequestParam(required = false) EventLevel eventLevel,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        
        Pageable pageable = PaginationUtil.buildPageable(page, size, sortBy, direction);
        return ResponseEntity.ok(auditLogQueryService.getLogs(userId, action, resourceType, resourceId, eventLevel, date, pageable));
    }

    @GetMapping("/compliance-report")
    @PreAuthorize("hasAnyRole('ADMIN','COMPLIANCE_OFFICER')")
    @Operation(summary = "Aggregate compliance report: event counts grouped by action, resourceType, and user, for a date range")
    public ResponseEntity<ComplianceReportDTO> complianceReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(auditLogQueryService.buildComplianceReport(from, to));
    }
}