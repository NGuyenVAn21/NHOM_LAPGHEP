package com.example.bookhub.models; // Kiểm tra xem package của bạn là model hay models nhé

import com.google.gson.annotations.SerializedName;

public class ProfileStatsResponse {

    @SerializedName("totalBorrowed")
    private int totalBorrowed;

    @SerializedName("totalRead")
    private int totalRead;

    @SerializedName("readingDays")
    private int readingDays;

    // --- Constructor ---
    public ProfileStatsResponse(int totalBorrowed, int totalRead, int readingDays) {
        this.totalBorrowed = totalBorrowed;
        this.totalRead = totalRead;
        this.readingDays = readingDays;
    }

    // --- Getter Methods ---
    public int getTotalBorrowed() {
        return totalBorrowed;
    }

    public int getTotalRead() {
        return totalRead;
    }

    public int getReadingDays() {
        return readingDays;
    }
}