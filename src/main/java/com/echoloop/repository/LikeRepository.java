package com.echoloop.repository;

import com.echoloop.model.Like;
import com.echoloop.model.Post;
import com.echoloop.model.Event;
import com.echoloop.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    List<Like> findByPost(Post post);
    List<Like> findByEvent(Event event);
    Optional<Like> findByUserAndPost(User user, Post post);
    Optional<Like> findByUserAndEvent(User user, Event event);
    long countByPost(Post post);
    long countByEvent(Event event);
} 