package com.clouderp.controller;

import com.clouderp.dto.NotificationDto;
import com.clouderp.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<List<NotificationDto.Response>> getNotifications(
            @RequestParam(value = "unreadOnly", required = false) Boolean unreadOnly,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "search", required = false) String search) {
        return ResponseEntity.ok(notificationService.getNotifications(unreadOnly, type, search));
    }

    @GetMapping("/unread-count")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<NotificationDto.UnreadCountResponse> getUnreadCount() {
        return ResponseEntity.ok(notificationService.getUnreadCount());
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<NotificationDto.Response> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    @PutMapping("/read-all")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<Map<String, Object>> markAllAsRead() {
        int count = notificationService.markAllAsRead();
        return ResponseEntity.ok(Map.of("message", "All notifications marked as read", "updated_count", count));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, String>> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok(Map.of("message", "Notification deleted successfully"));
    }

    @DeleteMapping("/clear-read")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, Object>> clearReadNotifications() {
        int count = notificationService.clearReadNotifications();
        return ResponseEntity.ok(Map.of("message", "Cleared read notifications", "deleted_count", count));
    }

    @PostMapping("/scan")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<NotificationDto.ScanResponse> scanSystemAlerts() {
        return ResponseEntity.ok(notificationService.scanAndGenerateSystemAlerts(null));
    }
}
