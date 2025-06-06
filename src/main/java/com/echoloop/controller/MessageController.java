package com.echoloop.controller;

import com.echoloop.dto.MessageRequest;
import com.echoloop.dto.MessageResponse;
import com.echoloop.dto.ConversationResponse;
import com.echoloop.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    @Autowired
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationResponse>> getConversations(
            @RequestHeader("User-Id") Long userId) {
        List<ConversationResponse> conversations = messageService.getConversations(userId);
        return ResponseEntity.ok(conversations);
    }

    @PostMapping("/send")
    public ResponseEntity<MessageResponse> sendMessage(
            @RequestHeader("User-Id") Long senderId,
            @RequestBody MessageRequest messageRequest) {
        MessageResponse response = messageService.sendMessage(senderId, messageRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/conversation/{otherUserId}")
    public ResponseEntity<List<MessageResponse>> getConversation(
            @RequestHeader("User-Id") Long userId,
            @PathVariable Long otherUserId) {
        List<MessageResponse> messages = messageService.getConversation(userId, otherUserId);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/unread")
    public ResponseEntity<List<MessageResponse>> getUnreadMessages(
            @RequestHeader("User-Id") Long userId) {
        List<MessageResponse> unreadMessages = messageService.getUnreadMessages(userId);
        return ResponseEntity.ok(unreadMessages);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Integer>> getUnreadCount(
            @RequestHeader("User-Id") Long userId) {
        int unreadCount = messageService.getUnreadCount(userId);
        return ResponseEntity.ok(Map.of("unreadCount", unreadCount));
    }

    @PostMapping("/mark-all-read")
    public ResponseEntity<?> markAllMessagesAsRead(
            @RequestHeader("User-Id") Long userId) {
        messageService.markAllMessagesAsRead(userId);
        return ResponseEntity.ok().build();
    }
} 