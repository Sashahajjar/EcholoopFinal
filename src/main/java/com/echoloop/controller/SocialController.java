package com.echoloop.controller;

import com.echoloop.model.*;
import com.echoloop.repository.*;
import com.echoloop.service.NotificationService;
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

    @Autowired
    private NotificationService notificationService;

    // Like/Unlike a post
    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<?> togglePostLike(@PathVariable Long postId, @RequestParam Long userId) {
        try {
            System.out.println("\n=== POST LIKE FLOW START ===");
            System.out.println("1. Received request:");
            System.out.println("   - Post ID from path: " + postId);
            System.out.println("   - User ID from param: " + userId);
            
            User user = userRepository.findById(userId).orElseThrow(() -> 
                new RuntimeException("User not found: " + userId));
            Post post = postRepository.findById(postId).orElseThrow(() -> 
                new RuntimeException("Post not found: " + postId));
            
            System.out.println("\n2. Retrieved entities:");
            System.out.println("   - Post from DB - ID: " + post.getId());
            System.out.println("   - Post from DB - Owner ID: " + post.getUser().getId());
            System.out.println("   - User from DB - ID: " + user.getId());
            
            var existingLike = likeRepository.findByUserAndPost(user, post);
            if (existingLike.isPresent()) {
                likeRepository.delete(existingLike.get());
                return ResponseEntity.ok(Map.of(
                    "liked", false,
                    "count", likeRepository.countByPost(post)
                ));
            } else {
                Like newLike = new Like(user, post);
                likeRepository.save(newLike);
                
                // Create notification for post owner if it's not a self-like
                if (!post.getUser().getId().equals(userId)) {
                    Long postIdForNotification = post.getId();
                    System.out.println("\n3. Preparing notification:");
                    System.out.println("   - Post ID to be used: " + postIdForNotification);
                    System.out.println("   - Post ID direct access: " + post.getId());
                    System.out.println("   - Post reference valid: " + (post != null));
                    System.out.println("   - Target owner ID: " + post.getUser().getId());
                    
                    Notification notification = notificationService.createNotification(
                        post.getUser().getId(),
                        NotificationType.POST_LIKE,
                        user.getUsername() + " liked your post",
                        postIdForNotification  // Explicitly using saved post ID
                    );
                    
                    System.out.println("\n4. Notification created:");
                    System.out.println("   - Notification ID: " + notification.getId());
                    System.out.println("   - Stored related entity ID: " + notification.getRelatedEntityId());
                }
                
                return ResponseEntity.ok(Map.of(
                    "liked", true,
                    "count", likeRepository.countByPost(post)
                ));
            }
        } catch (Exception e) {
            System.err.println("\n=== ERROR IN POST LIKE FLOW ===");
            System.err.println("Error details:");
            System.err.println("  - Post ID: " + postId);
            System.err.println("  - User ID: " + userId);
            System.err.println("  - Error message: " + e.getMessage());
            e.printStackTrace();
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
                
                // Create notification for event owner
                if (event.getCreatedBy() != null && !event.getCreatedBy().getId().equals(userId)) {
                    notificationService.createNotification(
                        event.getCreatedBy().getId(),
                        NotificationType.EVENT_LIKE,
                        user.getUsername() + " liked your event: " + event.getTitle(),
                        eventId
                    );
                }
                
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
            
            // Create notification for post owner
            if (!post.getUser().getId().equals(userId)) {
                notificationService.createNotification(
                    post.getUser().getId(),
                    NotificationType.POST_COMMENT,
                    user.getUsername() + " commented on your post",
                    postId
                );
            }
            
            return ResponseEntity.ok(Map.of(
                "id", comment.getId(),
                "content", comment.getContent(),
                "username", user.getUsername(),
                "userId", user.getId(),
                "userRole", user.getRole(),
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
            
            // Create notification for event owner
            if (event.getCreatedBy() != null && !event.getCreatedBy().getId().equals(userId)) {
                notificationService.createNotification(
                    event.getCreatedBy().getId(),
                    NotificationType.POST_COMMENT,
                    user.getUsername() + " commented on your event: " + event.getTitle(),
                    eventId
                );
            }
            
            return ResponseEntity.ok(Map.of(
                "id", comment.getId(),
                "content", comment.getContent(),
                "username", user.getUsername(),
                "userId", user.getId(),
                "userRole", user.getRole(),
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