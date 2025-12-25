package com.example.bookhub.models;

import com.google.gson.annotations.SerializedName;

public class BorrowRecord {
    @SerializedName("recordId")
    private int recordId;

    @SerializedName("bookId")
    private int bookId;

    @SerializedName("title")
    private String title;


    @SerializedName("author")
    private String author;
    // -------------------------

    @SerializedName("coverUrl")
    private String coverUrl;

    @SerializedName("borrowDate")
    private String borrowDate;

    @SerializedName("dueDate")
    private String dueDate;

    @SerializedName("returnDate")
    private String returnDate;

    @SerializedName("status")
    private String status;

    @SerializedName("displayStatus")
    private String displayStatus;

    @SerializedName("statusColor")
    private String statusColor;
    @SerializedName("price")
    private String price;

    public String getPrice() { return price; }

    // --- GETTERS ---
    public int getRecordId() { return recordId; }
    public int getBookId() { return bookId; }
    public String getTitle() { return title; }

    // --- BỔ SUNG NẾU THIẾU ---
    public String getAuthor() { return author; }
    // -------------------------

    public String getCoverUrl() { return coverUrl; }
    public String getBorrowDate() { return borrowDate; }
    public String getDueDate() { return dueDate; }
    public String getReturnDate() { return returnDate; }
    public String getStatus() { return status; }
    public String getDisplayStatus() { return displayStatus; }
    public String getStatusColor() { return statusColor; }
}