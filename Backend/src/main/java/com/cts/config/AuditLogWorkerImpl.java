package com.cts.config;

import com.cts.entity.AuditLog;
import com.cts.entity.User;
import com.cts.enumeration.EventLevel;
import com.cts.repository.AuditLogRepository;
import com.cts.service.AuditLogWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogWorkerImpl implements AuditLogWorker {

    private final AuditLogRepository auditLogRepository;

    @Override
    public void logAsyncAction(User user, String action, String resourceType, Long resourceId, String details) {
        logAsyncAction(user, action, resourceType, resourceId, details, EventLevel.INFO);
    }

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAsyncAction(User user, String action, String resourceType, Long resourceId,
                               String details, EventLevel level) {
        AuditLog entry = AuditLog.builder()
                .user(user)
                .action(action)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .details(details)
                .eventLevel(level != null ? level : EventLevel.INFO)
                .timestamp(LocalDateTime.now())
                .build();
        auditLogRepository.save(entry);
        log.debug("Async audit persisted [{}]: {} on {} #{}", entry.getEventLevel(), action, resourceType, resourceId);
    }
}
