package com.example.bookhub.models;

public class ActionRequest {
    private int userId;
    private int recordId;

    public ActionRequest(int userId, int recordId) {
        this.userId = userId;
        this.recordId = recordId;
    }
}