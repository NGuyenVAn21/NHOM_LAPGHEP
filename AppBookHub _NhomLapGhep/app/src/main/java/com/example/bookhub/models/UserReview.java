package com.example.bookhub.models;

import com.google.gson.annotations.SerializedName;

public class UserReview {
    @SerializedName("bookTitle")
    private String bookTitle;

    @SerializedName("rating")
    private int rating;

    @SerializedName("comment")
    private String comment;

    @SerializedName("date")
    private String date;

    @SerializedName("image")
    private String image;

    // Getters
    public String getBookTitle() { return bookTitle; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public String getDate() { return date; }
    public String getImage() { return image; }
}