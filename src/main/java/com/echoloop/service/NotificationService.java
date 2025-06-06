package com.echoloop.service;

import com.echoloop.model.Notification;
import com.echoloop.model.NotificationDTO;
import com.echoloop.model.NotificationType;
import com.echoloop.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(userId, false);
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsRead(userId, false);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public Notification createNotification(Long userId, NotificationType type, String message, Long relatedEntityId) {
        try {
            System.out.println("\n=== NOTIFICATION CREATION START ===");
            
            // Input validation
            if (userId == null) throw new IllegalArgumentException("userId cannot be null");
            if (type == null) throw new IllegalArgumentException("type cannot be null");
            if (message == null) throw new IllegalArgumentException("message cannot be null");
            
            // For post-related notifications, relatedEntityId is required
            if ((type == NotificationType.POST_LIKE || type == NotificationType.POST_COMMENT) && relatedEntityId == null) {
                throw new IllegalArgumentException("relatedEntityId cannot be null for notification type: " + type);
            }
            
            System.out.println("1. Creating notification with parameters:");
            System.out.println("   - Type: " + type);
            System.out.println("   - User ID: " + userId);
            System.out.println("   - Related Entity ID: " + relatedEntityId);
            System.out.println("   - Message: " + message);
            
            // Create notification object
            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setType(type.name());
            notification.setMessage(message);
            notification.setRelatedEntityId(relatedEntityId);
            
            // Save and verify the notification
            System.out.println("\n2. Saving notification to database");
            notification = notificationRepository.save(notification);
            
            // Create DTO for WebSocket transmission
            NotificationDTO dto = NotificationDTO.fromEntity(notification);
            
            // Verify all fields
            System.out.println("\n3. Verifying notification DTO for WebSocket:");
            System.out.println("   - ID: " + dto.getId());
            System.out.println("   - Type: " + dto.getType());
            System.out.println("   - Related Entity ID: " + dto.getRelatedEntityId());
            
            if (dto.getRelatedEntityId() == null && (type == NotificationType.POST_LIKE || type == NotificationType.POST_COMMENT)) {
                System.err.println("\nWARNING: relatedEntityId is null in DTO!");
                System.err.println("Original value: " + relatedEntityId);
                throw new RuntimeException("Failed to persist relatedEntityId");
            }
            
            // Send real-time notification through WebSocket
            System.out.println("\n4. Sending WebSocket notification");
            messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/notifications",
                dto
            );
            
            return notification;
        } catch (Exception e) {
            System.err.println("\n=== ERROR IN NOTIFICATION CREATION ===");
            throw e;
        }
    }
} 