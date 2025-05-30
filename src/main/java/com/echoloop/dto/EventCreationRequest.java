package com.echoloop.dto;

import java.util.List;

public class EventCreationRequest {
    private String title;
    private String genre;
    private String location;
    private String eventDate;
    private String description;
    private List<Long> djIds;

    public String getTitle() {
        return title;
    }

    public String getGenre() {
        return genre;
    }

    public String getLocation() {
        return location;
    }

    public String getEventDate() {
        return eventDate;
    }

    public String getDescription() {
        return description;
    }

    public List<Long> getDjIds() {
        return djIds;
    }
}
