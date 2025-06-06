package com.echoloop.controller;

import com.echoloop.model.Notification;
import com.echoloop.model.NotificationDTO;
import com.echoloop.model.NotificationType;
import com.echoloop.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    public static class NotificationRequest {
        private String type;
        private String message;
        private Long relatedEntityId;  // Added field
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public Long getRelatedEntityId() { return relatedEntityId; }
        public void setRelatedEntityId(Long relatedEntityId) { this.relatedEntityId = relatedEntityId; }
    }

    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getUserNotifications(@RequestParam Long userId) {
        List<NotificationDTO> dtos = notificationService.getUserNotifications(userId)
            .stream()
            .map(NotificationDTO::fromEntity)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDTO>> getUnreadNotifications(@RequestParam Long userId) {
        List<NotificationDTO> dtos = notificationService.getUnreadNotifications(userId)
            .stream()
            .map(NotificationDTO::fromEntity)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/unread/count")
    public ResponseEntity<Long> getUnreadCount(@RequestParam Long userId) {
        return ResponseEntity.ok(notificationService.getUnreadCount(userId));
    }

    @PostMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@RequestParam Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/test")
    public ResponseEntity<NotificationDTO> sendTestNotification(
        @RequestParam Long userId,
        @RequestBody NotificationRequest request
    ) {
        try {
            if (request == null || request.getType() == null || request.getMessage() == null) {
                throw new IllegalArgumentException("Type and message are required");
            }

            NotificationType notificationType = NotificationType.valueOf(request.getType());
            
            Notification notification = notificationService.createNotification(
                userId,
                notificationType,
                request.getMessage(),
                request.getRelatedEntityId()  // Use the relatedEntityId from request
            );
            
            return ResponseEntity.ok(NotificationDTO.fromEntity(notification));
        } catch (Exception e) {
            System.err.println("Error creating notification: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
} 