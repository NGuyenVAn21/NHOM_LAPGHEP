package com.example.bookhub.model;

public class Event {
    private int id;
    private String title;
    private String description;
    private String startDate;
    private String endDate;
    private String imageUrl; // URL ảnh banner

    // Getter
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public String getImageUrl() { return imageUrl; }
}