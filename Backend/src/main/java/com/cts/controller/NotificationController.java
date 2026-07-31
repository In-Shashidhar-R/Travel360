package com.cts.controller;

import com.cts.dto.NotificationResponseDTO;
import com.cts.dto.PageResponse;
import com.cts.security.SecurityUtil;
import com.cts.service.NotificationService;
import com.cts.util.AppConstants;
import com.cts.util.PaginationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "08. Notification Center")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER', 'TRAVEL_AGENT')")
    @Operation(summary = "Fetches paginated notifications for a user, optionally only unread")
    public ResponseEntity<PageResponse<NotificationResponseDTO>> getUserNotifications(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        SecurityUtil.assertSelfOrAdmin(userId);
        Pageable pageable = PaginationUtil.buildPageable(page, size, sortBy, direction);
        return ResponseEntity.ok(notificationService.getUserNotifications(userId, unreadOnly, pageable));
    }

    @PutMapping("/{notificationId}/read")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER', 'TRAVEL_AGENT')")
    @Operation(summary = "Marks a single notification as read")
    public ResponseEntity<NotificationResponseDTO> markAsRead(
            @PathVariable Long notificationId,
            @RequestParam Long userId) {
        SecurityUtil.assertSelfOrAdmin(userId);
        return ResponseEntity.ok(notificationService.markAsRead(notificationId, userId));
    }
}
