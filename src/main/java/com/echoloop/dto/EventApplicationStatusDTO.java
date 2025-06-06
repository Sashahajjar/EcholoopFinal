package com.echoloop.dto;

public class EventApplicationStatusDTO {
    public String eventTitle;
    public String status;
    public Long eventId;

    public EventApplicationStatusDTO(String eventTitle, String status, Long eventId) {
        this.eventTitle = eventTitle;
        this.status = status;
        this.eventId = eventId;
    }
}
