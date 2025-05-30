package com.echoloop.controller;

import com.echoloop.model.*;
import com.echoloop.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/social")
@CrossOrigin(origins = "*")
public class SocialController {

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private EventRepository eventRepository;

    // Like/Unlike a post
    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<?> togglePostLike(@PathVariable Long postId, @RequestParam Long userId) {
        try {
            User user = userRepository.findById(userId).orElseThrow();
            Post post = postRepository.findById(postId).orElseThrow();
            
            var existingLike = likeRepository.findByUserAndPost(user, post);
            if (existingLike.isPresent()) {
                likeRepository.delete(existingLike.get());
                return ResponseEntity.ok(Map.of(
                    "liked", false,
                    "count", likeRepository.countByPost(post)
                ));
            } else {
                likeRepository.save(new Like(user, post));
                return ResponseEntity.ok(Map.of(
                    "liked", true,
                    "count", likeRepository.countByPost(post)
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error toggling like: " + e.getMessage());
        }
    }

    // Like/Unlike an event
    @PostMapping("/events/{eventId}/like")
    public ResponseEntity<?> toggleEventLike(@PathVariable Long eventId, @RequestParam Long userId) {
        try {
            User user = userRepository.findById(userId).orElseThrow();
            Event event = eventRepository.findById(eventId).orElseThrow();
            
            var existingLike = likeRepository.findByUserAndEvent(user, event);
            if (existingLike.isPresent()) {
                likeRepository.delete(existingLike.get());
                return ResponseEntity.ok(Map.of(
                    "liked", false,
                    "count", likeRepository.countByEvent(event)
                ));
            } else {
                likeRepository.save(new Like(user, event));
                return ResponseEntity.ok(Map.of(
                    "liked", true,
                    "count", likeRepository.countByEvent(event)
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error toggling like: " + e.getMessage());
        }
    }

    // Add comment to post
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<?> addPostComment(@PathVariable Long postId, @RequestParam Long userId, @RequestBody Map<String, String> body) {
        try {
            User user = userRepository.findById(userId).orElseThrow();
            Post post = postRepository.findById(postId).orElseThrow();
            String content = body.get("content");
            
            Comment comment = new Comment(content, user, post);
            commentRepository.save(comment);
            
            return ResponseEntity.ok(Map.of(
                "id", comment.getId(),
                "content", comment.getContent(),
                "username", user.getUsername(),
                "createdAt", comment.getCreatedAt()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error adding comment: " + e.getMessage());
        }
    }

    // Add comment to event
    @PostMapping("/events/{eventId}/comments")
    public ResponseEntity<?> addEventComment(@PathVariable Long eventId, @RequestParam Long userId, @RequestBody Map<String, String> body) {
        try {
            User user = userRepository.findById(userId).orElseThrow();
            Event event = eventRepository.findById(eventId).orElseThrow();
            String content = body.get("content");
            
            Comment comment = new Comment(content, user, event);
            commentRepository.save(comment);
            
            return ResponseEntity.ok(Map.of(
                "id", comment.getId(),
                "content", comment.getContent(),
                "username", user.getUsername(),
                "createdAt", comment.getCreatedAt()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error adding comment: " + e.getMessage());
        }
    }

    // Get post comments
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<Comment>> getPostComments(@PathVariable Long postId) {
        try {
            Post post = postRepository.findById(postId).orElseThrow();
            return ResponseEntity.ok(commentRepository.findByPostOrderByCreatedAtDesc(post));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get event comments
    @GetMapping("/events/{eventId}/comments")
    public ResponseEntity<List<Comment>> getEventComments(@PathVariable Long eventId) {
        try {
            Event event = eventRepository.findById(eventId).orElseThrow();
            return ResponseEntity.ok(commentRepository.findByEventOrderByCreatedAtDesc(event));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Check if user has liked a post
    @GetMapping("/posts/{postId}/liked")
    public ResponseEntity<Boolean> hasLikedPost(@PathVariable Long postId, @RequestParam Long userId) {
        try {
            User user = userRepository.findById(userId).orElseThrow();
            Post post = postRepository.findById(postId).orElseThrow();
            return ResponseEntity.ok(likeRepository.findByUserAndPost(user, post).isPresent());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Check if user has liked an event
    @GetMapping("/events/{eventId}/liked")
    public ResponseEntity<Boolean> hasLikedEvent(@PathVariable Long eventId, @RequestParam Long userId) {
        try {
            User user = userRepository.findById(userId).orElseThrow();
            Event event = eventRepository.findById(eventId).orElseThrow();
            return ResponseEntity.ok(likeRepository.findByUserAndEvent(user, event).isPresent());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get like counts
    @GetMapping("/posts/{postId}/likes/count")
    public ResponseEntity<Long> getPostLikeCount(@PathVariable Long postId) {
        try {
            Post post = postRepository.findById(postId).orElseThrow();
            return ResponseEntity.ok(likeRepository.countByPost(post));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/events/{eventId}/likes/count")
    public ResponseEntity<Long> getEventLikeCount(@PathVariable Long eventId) {
        try {
            Event event = eventRepository.findById(eventId).orElseThrow();
            return ResponseEntity.ok(likeRepository.countByEvent(event));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 