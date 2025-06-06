package com.echoloop.controller;

import com.echoloop.dto.EventCreationRequest;
import com.echoloop.dto.EventDTO;
import com.echoloop.dto.UserDTO;
import com.echoloop.model.Event;
import com.echoloop.model.User;
import com.echoloop.repository.EventRepository;
import com.echoloop.repository.UserRepository;
import com.echoloop.service.EventService;
import com.echoloop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*")
public class EventController {

    @Autowired private EventService eventService;
    @Autowired private UserService userService;
    @Autowired private EventRepository eventRepository;
    @Autowired private UserRepository userRepository;

    @PostMapping("/{userId}/interested/{eventId}")
    public ResponseEntity<?> markInterested(@PathVariable Long userId, @PathVariable Long eventId) {
        userService.markInterest(userId, eventId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/create")
    public ResponseEntity<?> createEvent(@RequestBody EventCreationRequest request,
                                         @RequestParam Long plannerId) {
        eventService.createEvent(request, plannerId);
        return ResponseEntity.ok("Event created successfully");
    }

    @PostMapping("/{eventId}/apply/{djId}")
    public ResponseEntity<?> applyToEvent(@PathVariable Long eventId, @PathVariable Long djId) {
        try {
            eventService.applyToEvent(djId, eventId);
            return ResponseEntity.ok("Application submitted.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error applying to event: " + e.getMessage());
        }
    }

    @GetMapping("/{userId}/suggestions/genre")
    public ResponseEntity<List<EventDTO>> getSuggestionsFromGenre(@PathVariable Long userId) {
        return ResponseEntity.ok(eventService.suggestByGenre(userId).stream().map(EventDTO::fromEntity).collect(Collectors.toList()));
    }

    @GetMapping("/{userId}/suggestions/following")
    public ResponseEntity<List<EventDTO>> getSuggestionsFromFollowing(@PathVariable Long userId) {
        return ResponseEntity.ok(eventService.suggestByFollowing(userId).stream().map(EventDTO::fromEntity).collect(Collectors.toList()));
    }

    @GetMapping("/opportunities")
    public List<EventDTO> getAllOpportunities() {
        return eventService.getAllOpportunities().stream().map(EventDTO::fromEntity).collect(Collectors.toList());
    }

    @GetMapping("/by-user/{userId}")
    public List<EventDTO> getByPlanner(@PathVariable Long userId) {
        return eventService.getEventsByPlanner(userId).stream().map(EventDTO::fromEntity).collect(Collectors.toList());
    }

    @GetMapping("/{eventId}/applicants")
    public ResponseEntity<List<UserDTO>> getApplicants(@PathVariable Long eventId, @RequestParam Long requesterId) {
        List<User> applicants = eventService.getApplicantsForEvent(eventId, requesterId);
        return ResponseEntity.ok(applicants.stream().map(UserDTO::fromEntity).collect(Collectors.toList()));
    }

    @PostMapping("/{eventId}/accept/{djId}")
    public ResponseEntity<?> acceptApplicant(@PathVariable Long eventId,
                                             @PathVariable Long djId,
                                             @RequestParam Long requesterId) {
        try {
            eventService.acceptApplicant(eventId, djId, requesterId);
            return ResponseEntity.ok("DJ accepted successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error accepting applicant: " + e.getMessage());
        }
    }

    @PostMapping("/{eventId}/reject/{djId}")
    public ResponseEntity<?> rejectApplicant(@PathVariable Long eventId,
                                             @PathVariable Long djId,
                                             @RequestParam Long requesterId) {
        eventService.rejectApplicant(eventId, djId, requesterId);
        return ResponseEntity.ok("DJ rejected");
    }

    @GetMapping("/{userId}/suggestions/performed")
    public ResponseEntity<List<EventDTO>> getSuggestionsFromPerformed(@PathVariable Long userId) {
        return ResponseEntity.ok(eventService.suggestByPerformedGenres(userId).stream().map(EventDTO::fromEntity).collect(Collectors.toList()));
    }

    @GetMapping("/{userId}/suggestions/foaf")
    public ResponseEntity<List<EventDTO>> getFoafSuggestions(@PathVariable Long userId) {
        return ResponseEntity.ok(eventService.suggestByFoaf(userId).stream().map(EventDTO::fromEntity).collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDTO> getEventById(@PathVariable Long id) {
        Event event = eventService.findById(id);
        return event == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(new EventDTO(event));
    }

    @GetMapping("/feed")
    public List<EventDTO> getFeedEvents() {
        return eventRepository.findAll().stream()
                .filter(e -> e.getCreatedAt() != null)
                .sorted(Comparator.comparing(Event::getCreatedAt).reversed())
                .map(EventDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping("/feed/following/{userId}")
    public List<EventDTO> getFeedEventsFromFollowing(@PathVariable Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        Set<User> usersToFetch = new HashSet<>(user.getFollowing());
        usersToFetch.add(user);
        return eventRepository.findAll().stream()
                .filter(e -> e.getCreatedAt() != null)
                .filter(e -> usersToFetch.contains(e.getCreatedBy()))
                .sorted(Comparator.comparing(Event::getCreatedAt).reversed())
                .map(EventDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping("/search")
    public ResponseEntity<List<EventDTO>> searchEvents(@RequestParam String query) {
        String q = query.toLowerCase();
        return ResponseEntity.ok(eventRepository.findAll().stream()
                .filter(e -> e.getTitle().toLowerCase().contains(q) || e.getGenre().toLowerCase().contains(q) || e.getLocation().toLowerCase().contains(q))
                .map(EventDTO::fromEntity)
                .collect(Collectors.toList()));
    }

    @GetMapping("/users/{id}/profile")
    public ResponseEntity<UserDTO> getDjProfile(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getDjProfile(id));
    }

    @PutMapping("/users/{id}/profile")
    public ResponseEntity<Void> updateDjProfile(@PathVariable Long id, @RequestBody UserDTO updatedProfile) {
        userService.updateDjProfile(id, updatedProfile);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{eventId}/has-applied/{djId}")
    public ResponseEntity<Boolean> hasApplied(@PathVariable Long eventId, @PathVariable Long djId) {
        Event event = eventRepository.findById(eventId).orElseThrow();
        User dj = userRepository.findById(djId).orElseThrow();
        boolean hasApplied = event.getApplicants().contains(dj) || event.getPerformingDjs().contains(dj);
        return ResponseEntity.ok(hasApplied);
    }

    @GetMapping("/{eventId}/applications/{userId}")
    public ResponseEntity<?> checkDjApplication(@PathVariable Long eventId, @PathVariable Long userId) {
        Event event = eventService.findById(eventId);
        User user = userRepository.findById(userId).orElse(null);
        if (event == null || user == null) return ResponseEntity.notFound().build();
        boolean hasApplied = event.getApplicants().stream().anyMatch(dj -> dj.getId().equals(userId));
        return hasApplied ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/check-applications/{userId}")
    public ResponseEntity<Set<Long>> checkUserApplications(@PathVariable Long userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) return ResponseEntity.notFound().build();

            Set<Long> eventIds = eventRepository.findAll().stream()
                .filter(event -> event.getApplicants().contains(user) || event.getPerformingDjs().contains(user))
                .map(Event::getId)
                .collect(Collectors.toSet());

            return ResponseEntity.ok(eventIds);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/check-interests/{userId}")
    public ResponseEntity<Set<Long>> checkUserInterests(@PathVariable Long userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) return ResponseEntity.notFound().build();
            Set<Long> eventIds = user.getInterestedEvents().stream().map(Event::getId).collect(Collectors.toSet());
            return ResponseEntity.ok(eventIds);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEvent(@PathVariable Long id, @RequestBody EventCreationRequest request) {
        try {
            Event event = eventRepository.findById(id).orElseThrow();
            User creator = event.getCreatedBy();
            if (creator == null || !"event community".equalsIgnoreCase(creator.getRole())) {
                return ResponseEntity.status(403).body("Only event communities can update events");
            }

            event.setTitle(request.getTitle());
            event.setGenre(request.getGenre());
            event.setLocation(request.getLocation());
            event.setEventDate(request.getEventDate());
            event.setDescription(request.getDescription());

            eventRepository.save(event);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error updating event: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable Long id) {
        try {
            Event event = eventRepository.findById(id).orElseThrow();
            User creator = event.getCreatedBy();
            if (creator == null || !"event community".equalsIgnoreCase(creator.getRole())) {
                return ResponseEntity.status(403).body("Only event communities can delete events");
            }

            eventRepository.delete(event);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error deleting event: " + e.getMessage());
        }
    }

    @GetMapping("/interesting/{userId}")
    public ResponseEntity<List<EventDTO>> getInterestedEvents(@PathVariable Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return ResponseEntity.ok(user.getInterestedEvents().stream().map(EventDTO::fromEntity).collect(Collectors.toList()));
    }

    @DeleteMapping("/{userId}/interested/{eventId}")
    public ResponseEntity<?> removeInterest(@PathVariable Long userId, @PathVariable Long eventId) {
        try {
            User user = userRepository.findById(userId).orElseThrow();
            Event event = eventRepository.findById(eventId).orElseThrow();
            user.getInterestedEvents().remove(event);
            userRepository.save(user);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error removing interest: " + e.getMessage());
        }
    }
}
