package com.cts.service;

import com.cts.dto.NotificationResponseDTO;
import com.cts.dto.PageResponse;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    PageResponse<NotificationResponseDTO> getUserNotifications(Long userId, boolean unreadOnly, Pageable pageable);

    NotificationResponseDTO markAsRead(Long notificationId, Long userId);
}
