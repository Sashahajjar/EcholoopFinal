package com.echoloop.dto;

public class MessageRequest {
    private Long recipientId;
    private String content;

    // Default constructor
    public MessageRequest() {
    }

    // Getters and setters
    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
} 