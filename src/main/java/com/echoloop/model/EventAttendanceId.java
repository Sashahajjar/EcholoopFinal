//package com.echoloop.model;
//
//import java.io.Serializable;
//import java.util.Objects;
//
//public class EventAttendanceId implements Serializable {
//    private Long userId;
//    private Long eventId;
//
//    public EventAttendanceId() {}
//
//    public EventAttendanceId(Long userId, Long eventId) {
//        this.userId = userId;
//        this.eventId = eventId;
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (!(o instanceof EventAttendanceId that)) return false;
//        return Objects.equals(userId, that.userId) &&
//               Objects.equals(eventId, that.eventId);
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(userId, eventId);
//    }
//}
