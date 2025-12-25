package com.example.bookhub.models;
public class ChangePasswordRequest {
    private int userId;
    private String oldPassword;
    private String newPassword;
    public ChangePasswordRequest(int userId, String oldPassword, String newPassword) {
        this.userId = userId;
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }
}