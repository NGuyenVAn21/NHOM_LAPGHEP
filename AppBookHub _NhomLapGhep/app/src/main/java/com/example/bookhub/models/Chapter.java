package com.example.bookhub.models;

import com.google.gson.annotations.SerializedName;

public class Chapter {
    @SerializedName("chapterId")
    private int chapterId;

    @SerializedName("chapterNum")
    private int chapterNum;

    @SerializedName("title")
    private String title;

    @SerializedName("content")
    private String content;

    // Getters
    public int getChapterId() { return chapterId; }
    public int getChapterNum() { return chapterNum; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
}