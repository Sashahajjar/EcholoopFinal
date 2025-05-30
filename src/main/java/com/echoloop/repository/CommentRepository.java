package com.echoloop.repository;

import com.echoloop.model.Comment;
import com.echoloop.model.Post;
import com.echoloop.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostOrderByCreatedAtDesc(Post post);
    List<Comment> findByEventOrderByCreatedAtDesc(Event event);
    long countByPost(Post post);
    long countByEvent(Event event);
} 