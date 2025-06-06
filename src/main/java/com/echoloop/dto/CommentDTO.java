package com.echoloop.dto;

import com.echoloop.model.Comment;
import java.time.LocalDateTime;

public class CommentDTO {
    private Long id;
    private String content;
    private String username;
    private Long userId;
    private String userRole;
    private LocalDateTime createdAt;

    public CommentDTO(Comment comment) {
        this.id = comment.getId();
        this.content = comment.getContent();
        this.username = comment.getUser().getUsername();
        this.userId = comment.getUser().getId();
        this.userRole = comment.getUser().getRole();
        this.createdAt = comment.getCreatedAt();
    }

    public static CommentDTO fromEntity(Comment comment) {
        return new CommentDTO(comment);
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
