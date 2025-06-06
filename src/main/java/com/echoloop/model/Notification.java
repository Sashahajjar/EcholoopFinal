package com.echoloop.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String message;

    @Column(name = "related_entity_id", nullable = false)
    private Long relatedEntityId;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    @PreUpdate
    protected void validateRelatedEntityId() {
        if (type != null && 
            (type.equals(NotificationType.POST_LIKE.name()) || 
             type.equals(NotificationType.POST_COMMENT.name())) && 
            relatedEntityId == null) {
            throw new IllegalStateException("relatedEntityId cannot be null for notification type: " + type);
        }
    }
} 