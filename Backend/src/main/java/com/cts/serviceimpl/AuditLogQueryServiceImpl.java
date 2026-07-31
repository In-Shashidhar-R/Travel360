package com.cts.serviceimpl;

import com.cts.dto.AuditLogResponseDTO;
import com.cts.dto.ComplianceReportDTO;
import com.cts.dto.PageResponse;
import com.cts.entity.AuditLog;
import com.cts.enumeration.EventLevel;
import com.cts.repository.AuditLogRepository;
import com.cts.service.AuditLogQueryService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditLogQueryServiceImpl implements AuditLogQueryService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponseDTO> getLogs(Long userId, String action, String resourceType, 
                                                     Long resourceId, EventLevel eventLevel, LocalDate date, Pageable pageable) {
        
        Specification<AuditLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (userId != null) {
                predicates.add(cb.equal(root.get("user").get("userId"), userId));
            }
            if (action != null && !action.trim().isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("action")), action.toLowerCase().trim()));
            }
            if (resourceType != null && !resourceType.trim().isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("resourceType")), resourceType.toLowerCase().trim()));
            }
            if (resourceId != null) {
                predicates.add(cb.equal(root.get("resourceId"), resourceId));
            }
            if (eventLevel != null) {
                predicates.add(cb.equal(root.get("eventLevel"), eventLevel));
            }
            if (date != null) {
                LocalDateTime startOfDay = date.atStartOfDay();
                LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
                predicates.add(cb.between(root.get("timestamp"), startOfDay, endOfDay));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return PageResponse.from(auditLogRepository.findAll(spec, pageable).map(this::toDto));
    }

    @Override
    @Transactional(readOnly = true)
    public ComplianceReportDTO buildComplianceReport(LocalDate from, LocalDate to) {
        if (from == null) from = LocalDate.now().minusDays(30);
        if (to   == null) to   = LocalDate.now();
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("Compliance report 'from' date must be before or equal to 'to' date");
        }

        LocalDateTime startDateTime = from.atStartOfDay();
        LocalDateTime endDateTime = to.atTime(LocalTime.MAX);

        List<AuditLog> events = auditLogRepository.findByTimestampBetween(startDateTime, endDateTime);

        Map<String, Long> byAction = events.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getAction() == null ? "UNKNOWN" : e.getAction(),
                        LinkedHashMap::new,
                        Collectors.counting()));

        Map<String, Long> byResource = events.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getResourceType() == null ? "UNKNOWN" : e.getResourceType(),
                        LinkedHashMap::new,
                        Collectors.counting()));

        Map<String, Long> byUser = events.stream()
                .collect(Collectors.groupingBy(
                        e -> (e.getUser() == null) ? "SYSTEM" : ("USER#" + e.getUser().getUserId()),
                        LinkedHashMap::new,
                        Collectors.counting()));

        return ComplianceReportDTO.builder()
                .windowStart(startDateTime)
                .windowEnd(endDateTime)
                .totalAuditEvents(events.size())
                .eventCountByAction(byAction)
                .eventCountByResourceType(byResource)
                .eventCountByUser(byUser)
                .build();
    }

    private AuditLogResponseDTO toDto(AuditLog entry) {
        return AuditLogResponseDTO.builder()
                .auditId(entry.getAuditId())
                .userId(entry.getUser() != null ? entry.getUser().getUserId() : null)
                .userName(entry.getUser() != null ? entry.getUser().getName() : "SYSTEM")
                .action(entry.getAction())
                .resourceType(entry.getResourceType())
                .resourceId(entry.getResourceId())
                .details(entry.getDetails())
                .timestamp(entry.getTimestamp())
                .eventLevel(entry.getEventLevel() != null ? entry.getEventLevel().name() : "INFO")
                .build();
    }
}