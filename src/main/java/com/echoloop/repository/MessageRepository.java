package com.echoloop.repository;

import com.echoloop.model.Message;
import com.echoloop.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findBySenderAndRecipientOrderBySentAtDesc(User sender, User recipient);
    List<Message> findByRecipientAndReadAtIsNullOrderBySentAtAsc(User recipient);
    List<Message> findByRecipientOrSenderOrderBySentAtDesc(User recipient, User sender);
    int countByRecipientAndReadAtIsNull(User recipient);
    
    // Add method to find unread messages between two users
    List<Message> findByRecipientIdAndSenderIdAndReadAtIsNull(Long recipientId, Long senderId);
} 