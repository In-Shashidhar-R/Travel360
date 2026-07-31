package com.cts.service;

import com.cts.dto.AuditLogResponseDTO;
import com.cts.dto.ComplianceReportDTO;
import com.cts.dto.PageResponse;
import com.cts.enumeration.EventLevel;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface AuditLogQueryService {
    
    PageResponse<AuditLogResponseDTO> getLogs(Long userId, String action, String resourceType, 
                                             Long resourceId, EventLevel eventLevel, LocalDate date, Pageable pageable);

    ComplianceReportDTO buildComplianceReport(LocalDate from, LocalDate to);
}