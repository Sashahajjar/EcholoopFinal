package com.echoloop.service;

import com.echoloop.dto.MessageRequest;
import com.echoloop.dto.MessageResponse;
import com.echoloop.dto.ConversationResponse;
import com.echoloop.model.Message;
import com.echoloop.model.User;
import com.echoloop.repository.MessageRepository;
import com.echoloop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM d, HH:mm");

    @Autowired
    public MessageService(MessageRepository messageRepository, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> getConversations(Long userId) {
        User currentUser = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Get all messages where the user is either sender or recipient
        List<Message> allMessages = messageRepository.findByRecipientOrSenderOrderBySentAtDesc(currentUser, currentUser);

        // Group messages by the other user in the conversation
        Map<Long, List<Message>> messagesByUser = new HashMap<>();
        for (Message message : allMessages) {
            Long otherUserId = message.getSender().getId().equals(userId) 
                ? message.getRecipient().getId() 
                : message.getSender().getId();
            
            messagesByUser.computeIfAbsent(otherUserId, k -> new ArrayList<>()).add(message);
        }

        // Create conversation responses
        return messagesByUser.entrySet().stream()
            .map(entry -> {
                Long otherUserId = entry.getKey();
                List<Message> messages = entry.getValue();
                User otherUser = userRepository.findById(otherUserId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

                ConversationResponse response = new ConversationResponse();
                response.setUserId(otherUserId);
                response.setUsername(otherUser.getUsername());
                response.setOnline(false); // You can implement online status tracking if needed
                
                // Get unread count
                long unreadCount = messages.stream()
                    .filter(m -> m.getRecipient().getId().equals(userId) && m.getReadAt() == null)
                    .count();
                response.setUnreadCount((int) unreadCount);

                // Get last message
                Message lastMessage = messages.get(0); // Messages are already sorted by sentAt desc
                response.setLastMessage(lastMessage.getContent());
                response.setLastMessageTime(lastMessage.getSentAt().format(TIME_FORMATTER));

                return response;
            })
            .sorted(Comparator.comparing(ConversationResponse::getLastMessageTime).reversed())
            .collect(Collectors.toList());
    }

    @Transactional
    public MessageResponse sendMessage(Long senderId, MessageRequest messageRequest) {
        User sender = userRepository.findById(senderId)
            .orElseThrow(() -> new RuntimeException("Sender not found"));
        User recipient = userRepository.findById(messageRequest.getRecipientId())
            .orElseThrow(() -> new RuntimeException("Recipient not found"));

        Message message = new Message();
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setContent(messageRequest.getContent());
        message.setSentAt(LocalDateTime.now());

        Message savedMessage = messageRepository.save(message);
        return convertToResponse(savedMessage);
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getConversation(Long userId, Long otherUserId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        User otherUser = userRepository.findById(otherUserId)
            .orElseThrow(() -> new RuntimeException("Other user not found"));

        List<Message> messages = messageRepository.findBySenderAndRecipientOrderBySentAtDesc(user, otherUser);
        messages.addAll(messageRepository.findBySenderAndRecipientOrderBySentAtDesc(otherUser, user));

        return messages.stream()
            .sorted((m1, m2) -> m2.getSentAt().compareTo(m1.getSentAt()))
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public List<MessageResponse> getUnreadMessages(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        List<Message> unreadMessages = messageRepository.findByRecipientAndReadAtIsNullOrderBySentAtAsc(user);
        unreadMessages.forEach(message -> message.setReadAt(LocalDateTime.now()));
        messageRepository.saveAll(unreadMessages);

        return unreadMessages.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public int getUnreadCount(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return messageRepository.countByRecipientAndReadAtIsNull(user);
    }

    @Transactional
    public void markAllMessagesAsRead(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Message> unreadMessages = messageRepository.findByRecipientAndReadAtIsNullOrderBySentAtAsc(user);
        LocalDateTime now = LocalDateTime.now();
        unreadMessages.forEach(message -> message.setReadAt(now));
        messageRepository.saveAll(unreadMessages);
    }

    @Transactional
    public void markConversationAsRead(Long userId, Long otherUserId) {
        // Get all unread messages where the current user is the recipient and the other user is the sender
        List<Message> unreadMessages = messageRepository.findByRecipientIdAndSenderIdAndReadAtIsNull(userId, otherUserId);
        
        // Mark all messages as read
        LocalDateTime now = LocalDateTime.now();
        unreadMessages.forEach(message -> message.setReadAt(now));
        messageRepository.saveAll(unreadMessages);
    }

    private MessageResponse convertToResponse(Message message) {
        MessageResponse response = new MessageResponse();
        response.setId(message.getId());
        response.setSenderId(message.getSender().getId());
        response.setSenderName(message.getSender().getUsername());
        response.setRecipientId(message.getRecipient().getId());
        response.setRecipientName(message.getRecipient().getUsername());
        response.setContent(message.getContent());
        response.setSentAt(message.getSentAt());
        response.setReadAt(message.getReadAt());
        return response;
    }
} 