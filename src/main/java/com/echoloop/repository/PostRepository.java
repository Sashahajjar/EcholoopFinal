package com.echoloop.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.echoloop.model.Post;
import com.echoloop.model.User;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
  List<Post> findAllByOrderByCreatedAtDesc();
  List<Post> findByUserIdOrderByCreatedAtDesc(Long userId);
  List<Post> findByUserInOrderByCreatedAtDesc(Set<User> users);  // ✅ Add this line


}

