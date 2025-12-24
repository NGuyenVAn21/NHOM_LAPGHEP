package com.example.bookhub;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;

public class Chapter implements Parcelable {
    @SerializedName("chapterId")
    private int chapterId;

    @SerializedName("chapterNum")
    private int chapterNum;

    @SerializedName("title")
    private String title;

    @SerializedName("content")
    private String content;

    // Constructor
    public Chapter() {}

    public Chapter(int chapterId, int chapterNum, String title, String content) {
        this.chapterId = chapterId;
        this.chapterNum = chapterNum;
        this.title = title;
        this.content = content;
    }

    // Parcelable implementation
    protected Chapter(Parcel in) {
        chapterId = in.readInt();
        chapterNum = in.readInt();
        title = in.readString();
        content = in.readString();
    }

    public static final Creator<Chapter> CREATOR = new Creator<Chapter>() {
        @Override
        public Chapter createFromParcel(Parcel in) {
            return new Chapter(in);
        }

        @Override
        public Chapter[] newArray(int size) {
            return new Chapter[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(chapterId);
        dest.writeInt(chapterNum);
        dest.writeString(title);
        dest.writeString(content);
    }

    // Getters
    public int getChapterId() { return chapterId; }
    public int getChapterNum() { return chapterNum; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
}