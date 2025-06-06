package com.echoloop.service;

import com.echoloop.dto.EventCreationRequest;
import com.echoloop.model.Event;
import com.echoloop.model.NotificationType;
import com.echoloop.model.User;
import com.echoloop.repository.EventRepository;
import com.echoloop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EventService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private NotificationService notificationService;

    public List<Event> suggestByFollowing(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        Set<User> following = user.getFollowing();

        Set<Event> suggested = new HashSet<>();
        for (User followed : following) {
            suggested.addAll(followed.getInterestedEvents());
        }

        suggested.removeAll(user.getInterestedEvents());
        return new ArrayList<>(suggested);
    }

    public List<Event> suggestByGenre(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();

        Set<String> genres = user.getInterestedEvents().stream()
                .map(Event::getGenre)
                .collect(Collectors.toSet());

        List<Event> all = eventRepository.findByGenreIn(new ArrayList<>(genres));
        all.removeAll(user.getInterestedEvents());
        return all;
    }

    public List<Event> getEventsByPlanner(Long plannerId) {
        return eventRepository.findByCreatedById(plannerId);
    }

    public void createEvent(EventCreationRequest req, Long plannerId) {
        User planner = userRepository.findById(plannerId).orElseThrow();

        if (!planner.getRole().equalsIgnoreCase("event community")) {
            throw new RuntimeException("Only event communities can create events");
        }

        Event event = new Event();
        event.setTitle(req.getTitle());
        event.setGenre(req.getGenre());
        event.setLocation(req.getLocation());
        event.setEventDate(req.getEventDate());
        event.setDescription(req.getDescription()); // ✅ description properly set
        event.setCreatedBy(planner);


        if (req.getDjIds() != null) {
            List<User> djs = userRepository.findAllById(req.getDjIds());
            event.setPerformingDjs(new HashSet<>(djs));
        }

        eventRepository.save(event);
    }

    public List<Event> getAllOpportunities() {
        return eventRepository.findAll().stream()
                .filter(e -> e.getCreatedBy() != null &&
                             e.getCreatedBy().getRole().equalsIgnoreCase("event community"))
                .collect(Collectors.toList());
    }

    public void applyToEvent(Long djId, Long eventId) {
        User dj = userRepository.findById(djId).orElseThrow(() -> new RuntimeException("DJ not found"));
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new RuntimeException("Event not found"));

        event.getApplicants().add(dj);
        eventRepository.save(event);

        // Notify event owner about the new application
        if (event.getCreatedBy() != null) {
            notificationService.createNotification(
                event.getCreatedBy().getId(),
                NotificationType.EVENT_APPLICATION,
                dj.getUsername() + " applied to your event: " + event.getTitle(),
                eventId
            );
        }
    }

    public List<User> getApplicantsForEvent(Long eventId, Long requesterId) {
        Event event = eventRepository.findById(eventId).orElseThrow();
        if (event.getCreatedBy() == null || !event.getCreatedBy().getId().equals(requesterId)) {
            throw new RuntimeException("Unauthorized access to applicants");
        }
        return new ArrayList<>(event.getApplicants() != null ? event.getApplicants() : Collections.emptySet());
    }


    public void acceptApplicant(Long eventId, Long djId, Long requesterId) {
        Event event = eventRepository.findById(eventId).orElseThrow();
        if (!event.getCreatedBy().getId().equals(requesterId)) {
            throw new RuntimeException("Unauthorized action");
        }
        User dj = userRepository.findById(djId).orElseThrow();

        event.getApplicants().remove(dj);
        event.getPerformingDjs().add(dj);
        eventRepository.save(event);

        // Notify DJ about acceptance
        notificationService.createNotification(
            djId,
            NotificationType.APPLICATION_ACCEPTED,
            "Your application for " + event.getTitle() + " has been accepted!",
            eventId
        );
    }

    public void rejectApplicant(Long eventId, Long djId, Long requesterId) {
        Event event = eventRepository.findById(eventId).orElseThrow();
        if (!event.getCreatedBy().getId().equals(requesterId)) {
            throw new RuntimeException("Unauthorized action");
        }
        User dj = userRepository.findById(djId).orElseThrow();

        event.getApplicants().remove(dj);
        event.getRejectedApplicants().add(dj);
        eventRepository.save(event);

        // Notify DJ about rejection
        notificationService.createNotification(
            djId,
            NotificationType.APPLICATION_REJECTED,
            "Your application for " + event.getTitle() + " has been declined",
            eventId
        );
    }

    public List<Event> suggestByPerformedGenres(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();

        Set<String> performedGenres = user.getPerformedAtEvents().stream()
                .map(Event::getGenre)
                .collect(Collectors.toSet());

        List<Event> suggestions = eventRepository.findByGenreIn(new ArrayList<>(performedGenres));
        suggestions.removeAll(user.getPerformedAtEvents());

        return suggestions;
    }

    public List<Event> suggestByFoaf(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();

        Set<Event> foafEvents = new HashSet<>();

        for (User followed : user.getFollowing()) {
            foafEvents.addAll(followed.getPerformedAtEvents());
            foafEvents.addAll(followed.getInterestedEvents());
        }

        foafEvents.removeAll(user.getPerformedAtEvents());
        foafEvents.removeAll(user.getInterestedEvents());

        return new ArrayList<>(foafEvents);
    }

    public Event findById(Long id) {
        return eventRepository.findById(id).orElse(null);
    }
}
