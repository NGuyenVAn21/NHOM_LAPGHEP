package com.example.bookhub.model;
import com.google.gson.annotations.SerializedName;

public class UserRank {
    @SerializedName("id") private int id;
    @SerializedName("name") private String name;
    @SerializedName("avatar") private String avatar;
    @SerializedName("borrowCount") private int borrowCount;

    public String getName() { return name; }
    public String getAvatar() { return avatar; }
    public int getBorrowCount() { return borrowCount; }
}