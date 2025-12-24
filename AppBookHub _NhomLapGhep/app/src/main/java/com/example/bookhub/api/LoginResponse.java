package com.example.bookhub.api;

public class LoginResponse {
    private String status;
    private String message;
    private UserData user;

    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public UserData getUser() { return user; }

    public class UserData {
        private int id;
        private String fullName;
        private String email;

        public String getFullName() { return fullName; }
    }
}