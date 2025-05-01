package com.echoloop.model;

import jakarta.persistence.*;

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

    public User() {}

    public User(String username, String password, String role, String events, String locations, String genres) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.events = events;
        this.locations = locations;
        this.genres = genres;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public String getEvents() {
        return events;
    }

    public String getLocations() {
        return locations;
    }

    public String getGenres() {
        return genres;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setEvents(String events) {
        this.events = events;
    }

    public void setLocations(String locations) {
        this.locations = locations;
    }

    public void setGenres(String genres) {
        this.genres = genres;
    }
}
