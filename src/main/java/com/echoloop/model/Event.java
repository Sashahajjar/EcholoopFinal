package com.echoloop.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String genre;
    private String location;
    private String eventDate;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @ManyToMany(mappedBy = "interestedEvents")
    private Set<User> interestedUsers = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "event_djs",
        joinColumns = @JoinColumn(name = "event_id"),
        inverseJoinColumns = @JoinColumn(name = "dj_id")
    )
    private Set<User> performingDjs = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToMany
    @JoinTable(
        name = "event_applications",
        joinColumns = @JoinColumn(name = "event_id"),
        inverseJoinColumns = @JoinColumn(name = "dj_id")
    )
    private Set<User> applicants = new HashSet<>();
    private String imageUrl;


    public Event() {}

    public Event(String title, String genre, String location, String eventDate) {
        this.title = title;
        this.genre = genre;
        this.location = location;
        this.eventDate = eventDate;
    }

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

    public Set<User> getInterestedUsers() { return interestedUsers; }
    public void setInterestedUsers(Set<User> interestedUsers) { this.interestedUsers = interestedUsers; }

    public Set<User> getPerformingDjs() { return performingDjs; }
    public void setPerformingDjs(Set<User> performingDjs) { this.performingDjs = performingDjs; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public Set<User> getApplicants() { return applicants; }
    public void setApplicants(Set<User> applicants) { this.applicants = applicants; }

    
}
