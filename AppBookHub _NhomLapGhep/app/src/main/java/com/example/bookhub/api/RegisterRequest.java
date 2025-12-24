package com.example.bookhub.api;

public class RegisterRequest {
    private String fullName;
    private String email;
    private String username;
    private String password;

    public RegisterRequest(String fullName, String email, String username, String password) {
        this.fullName = fullName;
        this.email = email;
        this.username = username;
        this.password = password;
    }
}