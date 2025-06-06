package com.echoloop.controller;

import com.echoloop.model.Notification;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    @MessageMapping("/notification")
    @SendToUser("/queue/notifications")
    public Notification sendNotification(Notification notification) {
        return notification;
    }

    @MessageMapping("/broadcast")
    @SendTo("/topic/notifications")
    public Notification broadcastNotification(Notification notification) {
        return notification;
    }
} 