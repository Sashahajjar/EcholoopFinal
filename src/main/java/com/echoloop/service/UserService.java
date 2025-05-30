package com.echoloop.service;

import com.echoloop.dto.UserDTO;
import com.echoloop.model.Event;
import com.echoloop.model.User;
import com.echoloop.repository.UserRepository;
import com.echoloop.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Set;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.HashSet;
import java.util.ArrayList;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    private UserDTO mapToDto(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRole(user.getRole());
        dto.setEvents(user.getEvents());
        dto.setGenres(user.getGenres());
        dto.setLocations(user.getLocations());
        dto.setExperience(user.getExperience());
        dto.setSkills(user.getSkills());
        dto.setAbout(user.getAbout());
        return dto;
    }

    public void followUser(Long userId, Long targetUserId) {
        User user = userRepository.findById(userId).orElseThrow();
        User target = userRepository.findById(targetUserId).orElseThrow();

        user.getFollowing().add(target);
        target.getFollowers().add(user);

        userRepository.save(user);
        userRepository.save(target);
    }

    public void unfollowUser(Long userId, Long targetUserId) {
        User user = userRepository.findById(userId).orElseThrow();
        User target = userRepository.findById(targetUserId).orElseThrow();

        user.getFollowing().remove(target);
        target.getFollowers().remove(user);

        userRepository.save(user);
        userRepository.save(target);
    }

    public Set<User> getFollowers(Long userId) {
        return userRepository.findById(userId).orElseThrow().getFollowers();
    }

    public Set<User> getFollowing(Long userId) {
        return userRepository.findById(userId).orElseThrow().getFollowing();
    }

    public List<User> getFoafCommunities(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        Set<User> following = user.getFollowing();
        Set<User> foafCommunities = new HashSet<>();

        // Get communities followed by people you follow
        for (User followed : following) {
            foafCommunities.addAll(
                followed.getFollowing().stream()
                    .filter(u -> "event community".equalsIgnoreCase(u.getRole()))
                    .collect(Collectors.toSet())
            );
        }

        // Remove communities you already follow
        foafCommunities.removeAll(following);
        // Remove yourself if you're a community
        foafCommunities.remove(user);

        return new ArrayList<>(foafCommunities);
    }

    public void markInterest(Long userId, Long eventId) {
        User user = userRepository.findById(userId).orElseThrow();
        Event event = eventRepository.findById(eventId).orElseThrow();
        user.getInterestedEvents().add(event);
        userRepository.save(user);
    }

    public UserDTO getDjProfile(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToDto(user);
    }

    public void updateDjProfile(Long id, UserDTO dto) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));

        user.setExperience(dto.getExperience());
        user.setSkills(dto.getSkills());
        user.setGenres(dto.getGenres());
        user.setLocations(dto.getLocations());

        userRepository.save(user);
    }

    public void updateCommunityProfile(Long id, UserDTO dto) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (!"event community".equalsIgnoreCase(user.getRole())) {
            throw new RuntimeException("Only event communities can update this profile type");
        }

        user.setLocations(dto.getLocations());
        user.setAbout(dto.getAbout());

        userRepository.save(user);
    }

    public List<Event> getUpcomingEventsForDj(Long djId) {
        User dj = userRepository.findById(djId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        return dj.getPerformedAtEvents().stream()
            .filter(e -> {
                try {
                    LocalDate eventDate = LocalDate.parse(e.getEventDate(), formatter);
                    return eventDate.isAfter(today);
                } catch (Exception ex) {
                    return false;
                }
            })
            .sorted((a, b) -> a.getEventDate().compareTo(b.getEventDate()))
            .collect(Collectors.toList());
    }
    public void updateUser(Long id, UserDTO dto) {
    User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    user.setLocations(dto.getLocations());
    user.setExperience(dto.getExperience());
    user.setSkills(dto.getSkills());
    user.setGenres(dto.getGenres());
    userRepository.save(user);
}
public void removeInterest(Long userId, Long eventId) {
    User user = userRepository.findById(userId).orElseThrow();
    Event event = eventRepository.findById(eventId).orElseThrow();
    user.getInterestedEvents().remove(event);
    userRepository.save(user);
}


}
