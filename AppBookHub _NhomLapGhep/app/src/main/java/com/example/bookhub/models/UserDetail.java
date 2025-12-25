package com.example.bookhub.models;
import com.google.gson.annotations.SerializedName;

public class UserDetail {
    private int id;
    @SerializedName("fullName") private String fullName;
    @SerializedName("email") private String email;
    @SerializedName("phone") private String phone;
    @SerializedName("username") private String username;

    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getUsername() { return username; }
}