package com.echoloop.controller;

import com.echoloop.dto.EventApplicationStatusDTO;
import com.echoloop.dto.SignupRequest;
import com.echoloop.model.Event;
import com.echoloop.model.User;
import com.echoloop.dto.UserDTO;
import com.echoloop.repository.EventRepository;
import com.echoloop.repository.UserRepository;
import com.echoloop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.UUID;
import java.nio.file.Path;
import jakarta.annotation.PostConstruct;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory!", e);
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        try {
            User newUser = new User(
                request.getUsername(),
                request.getPassword(),
                request.getRole()
            );
            userRepository.save(newUser);
            return ResponseEntity.ok("User registered successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error during registration");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        try {
            Optional<User> userOpt = userRepository.findByUsername(loginRequest.get("username"));

            if (userOpt.isPresent() && userOpt.get().getPassword().equals(loginRequest.get("password"))) {
                UserDTO dto = UserDTO.fromEntity(userOpt.get());
                return ResponseEntity.ok(dto);
            }

            return ResponseEntity.status(401).body("Invalid credentials");
        } catch (Exception e) {
            e.printStackTrace(); // Optional: helpful for debugging
            return ResponseEntity.internalServerError().body("Error during login");
        }
    }

    @PostMapping("/{id}/follow/{targetId}")
    public ResponseEntity<?> follow(@PathVariable Long id, @PathVariable Long targetId) {
        userService.followUser(id, targetId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/unfollow/{targetId}")
    public ResponseEntity<?> unfollow(@PathVariable Long id, @PathVariable Long targetId) {
        userService.unfollowUser(id, targetId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/followers")
    public ResponseEntity<Set<UserDTO>> getFollowers(@PathVariable Long id) {
        Set<UserDTO> followers = userService.getFollowers(id)
                .stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toSet());
        return ResponseEntity.ok(followers);
    }

    @GetMapping("/{id}/following")
    public ResponseEntity<Set<UserDTO>> getFollowing(@PathVariable Long id) {
        Set<UserDTO> following = userService.getFollowing(id)
                .stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toSet());
        return ResponseEntity.ok(following);
    }

    @GetMapping("/{id}/suggestions/communities")
    public ResponseEntity<List<UserDTO>> getFoafCommunitySuggestions(@PathVariable Long id) {
        List<User> suggestions = userService.getFoafCommunities(id);
        List<UserDTO> dtos = suggestions.stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, List<UserDTO>>> searchUsers(@RequestParam String query) {
        // Split query into words for better matching
        String[] searchTerms = query.toLowerCase().split("\\s+");
        
        // Get all users
        List<User> allUsers = userRepository.findAll();
        
        // Filter users based on search terms
        List<User> djs = allUsers.stream()
            .filter(user -> "dj".equalsIgnoreCase(user.getRole()))
            .filter(user -> matchesSearchTerms(user, searchTerms))
            .collect(Collectors.toList());
        
        List<User> communities = allUsers.stream()
            .filter(user -> "event community".equalsIgnoreCase(user.getRole()))
            .filter(user -> matchesSearchTerms(user, searchTerms))
            .collect(Collectors.toList());

        Map<String, List<UserDTO>> result = new HashMap<>();
        result.put("djs", djs.stream().map(UserDTO::fromEntity).toList());
        result.put("communities", communities.stream().map(UserDTO::fromEntity).toList());

        return ResponseEntity.ok(result);
    }

    private boolean matchesSearchTerms(User user, String[] searchTerms) {
        String userText = (user.getUsername() + " " + 
                          (user.getGenres() != null ? user.getGenres() : "") + " " +
                          (user.getLocations() != null ? user.getLocations() : "")).toLowerCase();
        
        return Arrays.stream(searchTerms)
            .allMatch(term -> userText.contains(term));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
            .map(UserDTO::fromEntity)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/users/{id}/upcoming-events")
    public ResponseEntity<List<Event>> getUpcomingEvents(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUpcomingEventsForDj(id));
    }

    @GetMapping("/{djId}/my-applications")
    public ResponseEntity<List<EventApplicationStatusDTO>> getMyApplications(@PathVariable Long djId) {
        User dj = userRepository.findById(djId).orElseThrow();
        List<Event> allEvents = eventRepository.findAll();

        List<EventApplicationStatusDTO> result = new ArrayList<>();

        for (Event e : allEvents) {
            if (e.getPerformingDjs().contains(dj)) {
                result.add(new EventApplicationStatusDTO(e.getTitle(), "Accepted", e.getId()));
            } else if (e.getApplicants().contains(dj)) {
                result.add(new EventApplicationStatusDTO(e.getTitle(), "Pending", e.getId()));
            } else if (e.getRejectedApplicants().contains(dj)) {
                result.add(new EventApplicationStatusDTO(e.getTitle(), "Rejected", e.getId()));
            }
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/profile")
    public ResponseEntity<UserDTO> getDjProfile(@PathVariable Long id) {
        try {
            UserDTO profile = userService.getDjProfile(id);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/profile")
    public ResponseEntity<Void> updateDjProfile(@PathVariable Long id, @RequestBody UserDTO dto) {
        userService.updateDjProfile(id, dto);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/community-profile")
    public ResponseEntity<Void> updateCommunityProfile(@PathVariable Long id, @RequestBody UserDTO dto) {
        userService.updateCommunityProfile(id, dto);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserDTO userDto) {
        userService.updateUser(id, userDto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/by-username/{username}")
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username) {
        return userRepository.findByUsername(username)
            .map(UserDTO::fromEntity)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/profile-picture")
    public ResponseEntity<?> updateProfilePicture(@PathVariable Long id, @RequestParam MultipartFile image) {
        try {
            User user = userRepository.findById(id).orElseThrow();

            if (image != null && !image.isEmpty()) {
                String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
                Path uploadPath = Paths.get(uploadDir);
                Files.createDirectories(uploadPath);
                Files.copy(image.getInputStream(), uploadPath.resolve(fileName));
                user.setProfilePicture("/uploads/" + fileName);
                userRepository.save(user);
                return ResponseEntity.ok(user.getProfilePicture());
            }
            return ResponseEntity.badRequest().body("No image provided");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error uploading profile picture");
        }
    }

    @GetMapping("/security-demo")
    public Map<String, String> demonstrateSecurity() {
        String testPassword = "myPassword123";
        String hashedPassword = BCrypt.hashpw(testPassword, BCrypt.gensalt());
        
        Map<String, String> demo = new HashMap<>();
        demo.put("originalPassword", testPassword);
        demo.put("hashedPassword", hashedPassword);
        demo.put("passwordMatch", String.valueOf(BCrypt.checkpw(testPassword, hashedPassword)));
        
        return demo;
    }

    // Add this new endpoint for file upload security demonstration
    @PostMapping("/security-demo/upload")
    public Map<String, String> demonstrateFileUploadSecurity(@RequestParam MultipartFile file) {
        Map<String, String> result = new HashMap<>();
        
        // Check file size (5MB limit)
        if (file.getSize() > 5_000_000) {
            result.put("status", "error");
            result.put("reason", "File too large. Maximum size is 5MB");
            return result;
        }
        
        // Check file type
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            result.put("status", "error");
            result.put("reason", "Invalid file type. Only JPEG and PNG allowed");
            return result;
        }
        
        result.put("status", "success");
        result.put("fileName", file.getOriginalFilename());
        result.put("fileSize", String.valueOf(file.getSize()));
        result.put("fileType", contentType);
        
        return result;
    }

}
