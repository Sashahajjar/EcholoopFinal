package com.echoloop.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class NotificationDTO {
    @JsonProperty
    private Long id;
    
    @JsonProperty
    private Long userId;
    
    @JsonProperty
    private String type;
    
    @JsonProperty
    private String message;
    
    @JsonProperty
    private Long relatedEntityId;
    
    @JsonProperty
    private boolean read;
    
    @JsonProperty
    private LocalDateTime createdAt;
    
    // Static factory method
    public static NotificationDTO fromEntity(Notification entity) {
        NotificationDTO dto = new NotificationDTO();
        dto.id = entity.getId();
        dto.userId = entity.getUserId();
        dto.type = entity.getType();
        dto.message = entity.getMessage();
        dto.relatedEntityId = entity.getRelatedEntityId();
        dto.read = entity.isRead();
        dto.createdAt = entity.getCreatedAt();
        return dto;
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public Long getRelatedEntityId() { return relatedEntityId; }
    public void setRelatedEntityId(Long relatedEntityId) { this.relatedEntityId = relatedEntityId; }
    
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
} 