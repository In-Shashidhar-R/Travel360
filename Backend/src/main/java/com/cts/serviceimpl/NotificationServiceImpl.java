package com.cts.serviceimpl;

import com.cts.dto.NotificationResponseDTO;
import com.cts.dto.PageResponse;
import com.cts.entity.Notification;
import com.cts.entity.User;
import com.cts.enumeration.Status;
import com.cts.exception.DataIsolationViolationException;
import com.cts.exception.ResourceNotFoundException;
import com.cts.repository.NotificationRepository;
import com.cts.repository.UserRepository;
import com.cts.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NotificationResponseDTO> getUserNotifications(Long userId, boolean unreadOnly, Pageable pageable) {
        User user = fetchUser(userId);
        Page<Notification> page = unreadOnly
                ? notificationRepository.findByUserAndStatus(user, Status.ACTIVE, pageable)
                : notificationRepository.findByUser(user, pageable);
        return PageResponse.from(page.map(this::toDto));
    }

    @Override
    @Transactional
    public NotificationResponseDTO markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + notificationId));
        if (!notification.getUser().getUserId().equals(userId)) {
            throw new DataIsolationViolationException("This notification does not belong to the requesting user.");
        }
        notification.setStatus(Status.INACTIVE);
        Notification saved = notificationRepository.save(notification);
        return toDto(saved);
    }

    private User fetchUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User account not found with ID: " + userId));
    }

    private NotificationResponseDTO toDto(Notification n) {
        return NotificationResponseDTO.builder()
                .notificationId(n.getNotificationId())
                .userId(n.getUser().getUserId())
                .message(n.getMessage())
                .category(n.getCategory() != null ? n.getCategory().name() : null)
                .status(n.getStatus() != null ? n.getStatus().name() : null)
                .createdDate(n.getCreatedDate())
                .build();
    }
}
