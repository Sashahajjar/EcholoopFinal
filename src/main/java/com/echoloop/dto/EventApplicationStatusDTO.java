package com.echoloop.dto;

public class EventApplicationStatusDTO {
    public String eventTitle;
    public String status;

    public EventApplicationStatusDTO(String eventTitle, String status) {
        this.eventTitle = eventTitle;
        this.status = status;
    }
}
