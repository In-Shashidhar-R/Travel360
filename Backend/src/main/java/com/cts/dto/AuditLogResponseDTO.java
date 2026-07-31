package com.cts.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogResponseDTO {
    private Long auditId;
    private Long userId;
    private String userName;
    private String action;
    private String resourceType;
    private Long resourceId;
    private String details;
    private LocalDateTime timestamp;
    private String eventLevel;
}
