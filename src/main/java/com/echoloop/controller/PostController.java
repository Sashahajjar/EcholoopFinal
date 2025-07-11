package com.echoloop.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.HashSet;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.echoloop.dto.PostDTO;
import com.echoloop.model.Post;
import com.echoloop.model.User;
import com.echoloop.repository.PostRepository;
import com.echoloop.repository.UserRepository;

import jakarta.annotation.PostConstruct;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Autowired private PostRepository postRepo;
    @Autowired private UserRepository userRepo;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory!", e);
        }
    }

    @PostMapping
    public ResponseEntity<?> createPost(@RequestParam Long userId,
                                        @RequestParam String content,
                                        @RequestParam(required = false) MultipartFile image) throws IOException {
        User user = userRepo.findById(userId).orElseThrow();
        Post post = new Post();
        post.setUser(user);
        post.setContent(content);
        post.setCreatedAt(LocalDateTime.now());

        if (image != null && !image.isEmpty()) {
            String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
            Path uploadPath = Paths.get(uploadDir);
            Files.createDirectories(uploadPath);
            Files.copy(image.getInputStream(), uploadPath.resolve(fileName));
            post.setImageUrl("/uploads/" + fileName);
        }

        postRepo.save(post);
        return ResponseEntity.ok("Post saved");
    }

    @GetMapping("/feed")
    public List<PostDTO> getFeedPosts() {
        return postRepo.findAllByOrderByCreatedAtDesc().stream()
                .map(PostDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping
    public List<PostDTO> getAllPosts() {
        return postRepo.findAllByOrderByCreatedAtDesc().stream()
                .map(PostDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping("/user/{userId}")
    public List<PostDTO> getPostsByUser(@PathVariable Long userId) {
        return postRepo.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(PostDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping("/following/{userId}")
    public List<PostDTO> getPostsFromFollowing(@PathVariable Long userId) {
        User user = userRepo.findById(userId).orElseThrow();
        Set<User> following = user.getFollowing();
        Set<User> usersToFetch = new HashSet<>(following);
        usersToFetch.add(user); // Include the user's own posts
        return postRepo.findByUserInOrderByCreatedAtDesc(usersToFetch)
                .stream()
                .map(PostDTO::fromEntity)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDTO> getPostById(@PathVariable Long id) {
        return postRepo.findById(id)
                .map(PostDTO::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePost(@PathVariable Long id,
                                        @RequestParam Long userId,
                                        @RequestParam String content,
                                        @RequestParam(required = false) MultipartFile image) throws IOException {
        Post post = postRepo.findById(id).orElseThrow();

        // Verify ownership
        if (!post.getUser().getId().equals(userId)) {
            return ResponseEntity.status(403).body("You can only edit your own posts");
        }

        post.setContent(content);

        if (image != null && !image.isEmpty()) {
            String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
            Path uploadPath = Paths.get(uploadDir);
            Files.createDirectories(uploadPath);
            Files.copy(image.getInputStream(), uploadPath.resolve(fileName));
            post.setImageUrl("/uploads/" + fileName);
        }

        postRepo.save(post);
        return ResponseEntity.ok("Post updated");
    }
}
