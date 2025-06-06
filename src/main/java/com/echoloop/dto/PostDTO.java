package com.echoloop.dto;

import com.echoloop.model.Comment;
import com.echoloop.model.Post;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class PostDTO {
    private Long id;
    private String content;
    private String imageUrl;
    private LocalDateTime createdAt;
    private String username;
    private Long userId;
    private String userRole;

    private int likeCount;
    private int commentCount;
    private List<CommentDTO> recentComments;

    public PostDTO(Post post) {
        this.id = post.getId();
        this.content = post.getContent();
        this.imageUrl = post.getImageUrl();
        this.createdAt = post.getCreatedAt();
        this.username = post.getUser().getUsername();
        this.userId = post.getUser().getId();
        this.userRole = post.getUser().getRole();

        this.likeCount = post.getLikes() != null ? post.getLikes().size() : 0;
        this.commentCount = post.getComments() != null ? post.getComments().size() : 0;
        this.recentComments = post.getComments() != null
            ? post.getComments().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(CommentDTO::fromEntity)
                .collect(Collectors.toList())
            : List.of();
    }

    public static PostDTO fromEntity(Post post) {
        return new PostDTO(post);
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }

    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }

    public int getCommentCount() { return commentCount; }
    public void setCommentCount(int commentCount) { this.commentCount = commentCount; }

    public List<CommentDTO> getRecentComments() { return recentComments; }
    public void setRecentComments(List<CommentDTO> recentComments) { this.recentComments = recentComments; }
}
