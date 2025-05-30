package com.echoloop.dto;

import com.echoloop.model.Event;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public class EventDTO {
    private Long id;
    private String title;
    private String genre;
    private String location;
    private String eventDate;
    private String description;
    private LocalDateTime createdAt;
    private Set<UserDTO> performingDjs;
    private String communityName;
    private Long communityId;

    public EventDTO(Event event) {
        this.id = event.getId();
        this.title = event.getTitle();
        this.genre = event.getGenre();
        this.location = event.getLocation();
        this.eventDate = event.getEventDate();
        this.description = event.getDescription();
        this.createdAt = event.getCreatedAt();
        
        if (event.getCreatedBy() != null) {
            this.communityName = event.getCreatedBy().getUsername();
            this.communityId = event.getCreatedBy().getId();
        }

        if (event.getPerformingDjs() != null) {
            this.performingDjs = event.getPerformingDjs().stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toSet());
        }
    }

    public static EventDTO fromEntity(Event event) {
        return new EventDTO(event);
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getEventDate() { return eventDate; }
    public void setEventDate(String eventDate) { this.eventDate = eventDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Set<UserDTO> getPerformingDjs() { return performingDjs; }
    public void setPerformingDjs(Set<UserDTO> performingDjs) { this.performingDjs = performingDjs; }

    public String getCommunityName() { return communityName; }
    public void setCommunityName(String communityName) { this.communityName = communityName; }

    public Long getCommunityId() { return communityId; }
    public void setCommunityId(Long communityId) { this.communityId = communityId; }

}
