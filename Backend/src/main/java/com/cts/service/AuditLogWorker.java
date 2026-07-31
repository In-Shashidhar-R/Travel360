package com.cts.service;

import com.cts.entity.User;
import com.cts.enumeration.EventLevel;


public interface AuditLogWorker {

    void logAsyncAction(User user, String action, String resourceType, Long resourceId, String details);

    void logAsyncAction(User user, String action, String resourceType, Long resourceId,
                        String details, EventLevel level);
}
