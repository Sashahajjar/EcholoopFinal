package com.echoloop.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;
    private String role;

    @Column(columnDefinition = "TEXT")
    private String events;

    @Column(columnDefinition = "TEXT")
    private String locations;

    @Column(columnDefinition = "TEXT")
    private String genres;

    @Column(columnDefinition = "TEXT")
    private String experience;

    @Column(columnDefinition = "TEXT")
    private String skills;

    @Column(columnDefinition = "TEXT")
    private String about;
   
    private String profilePicture;
   

    @ManyToMany
    @JoinTable(
        name = "user_followers",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "follower_id")
    )
    private Set<User> followers = new HashSet<>();

    @ManyToMany(mappedBy = "followers")
    private Set<User> following = new HashSet<>();

    @ManyToMany(mappedBy = "performingDjs")
    private Set<Event> performedAtEvents = new HashSet<>();
    
    @ManyToMany(mappedBy = "applicants")
    private Set<Event> appliedEvents = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "user_interested_events",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "event_id")
    )
    private Set<Event> interestedEvents = new HashSet<>();

    public User() {}

    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public User(String username, String password, String role, String events, String locations, String genres) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.events = events;
        this.locations = locations;
        this.genres = genres;
    }

    // Getters and setters
    public Long getId() { return id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isEventCommunity() {
        return "event community".equalsIgnoreCase(this.role);
    }

    public String getEvents() { return events; }
    public void setEvents(String events) { this.events = events; }

    public String getLocations() { return locations; }
    public void setLocations(String locations) { this.locations = locations; }

    public String getGenres() { return genres; }
    public void setGenres(String genres) { this.genres = genres; }

    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }

    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }

    public String getAbout() { return about; }
    public void setAbout(String about) { this.about = about; }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public Set<User> getFollowers() { return followers; }
    public void setFollowers(Set<User> followers) { this.followers = followers; }

    public Set<User> getFollowing() { return following; }
    public void setFollowing(Set<User> following) { this.following = following; }

    public Set<Event> getPerformedAtEvents() { return performedAtEvents; }
    public void setPerformedAtEvents(Set<Event> performedAtEvents) { this.performedAtEvents = performedAtEvents; }

    public Set<Event> getAppliedEvents() { return appliedEvents; }
    public void setAppliedEvents(Set<Event> appliedEvents) { this.appliedEvents = appliedEvents; }

    public Set<Event> getInterestedEvents() { return interestedEvents; }
    public void setInterestedEvents(Set<Event> interestedEvents) { this.interestedEvents = interestedEvents; }
}
