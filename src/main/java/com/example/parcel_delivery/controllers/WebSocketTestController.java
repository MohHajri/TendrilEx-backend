package com.example.parcel_delivery.controllers;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketTestController {

    @MessageMapping("/test")
    @SendTo("/topic/responses")
    public String testEndpoint(String message) {
        return "Received: " + message;
    }
}