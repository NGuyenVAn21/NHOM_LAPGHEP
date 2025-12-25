package com.example.bookhub.models;
public class UpdateProfileRequest {
    private String fullName;
    private String email;
    private String phone;
    public UpdateProfileRequest(String fullName, String email, String phone) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
    }
}