package com.example.bookhub.models;

public class Event {
    private int id; // Đã có biến này
    private String title;
    private String description;
    private String startDate;
    private String endDate;
    private String imageUrl;
    public int getId() { return id; }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public String getImageUrl() { return imageUrl; }
}