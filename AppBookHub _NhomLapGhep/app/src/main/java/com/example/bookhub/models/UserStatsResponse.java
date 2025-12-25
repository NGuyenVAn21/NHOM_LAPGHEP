package com.example.bookhub.models;

import com.google.gson.annotations.SerializedName;

public class UserStatsResponse {
    @SerializedName("borrowing")
    private int borrowing;

    @SerializedName("dueSoon")
    private int dueSoon;

    public int getBorrowing() { return borrowing; }
    public int getDueSoon() { return dueSoon; }
}
