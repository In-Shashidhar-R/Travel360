package com.cts.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;


@Data
@Builder
public class ComplianceReportDTO {

    private LocalDateTime windowStart;
    private LocalDateTime windowEnd;

    private long totalAuditEvents;

    private Map<String, Long> eventCountByAction;

    private Map<String, Long> eventCountByResourceType;

    private Map<String, Long> eventCountByUser;
}
