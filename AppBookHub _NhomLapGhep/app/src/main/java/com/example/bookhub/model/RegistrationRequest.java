package com.example.bookhub.model;

public class RegistrationRequest {
    private int userId;
    private int eventId;

    public RegistrationRequest(int userId, int eventId) {
        this.userId = userId;
        this.eventId = eventId;
    }
}