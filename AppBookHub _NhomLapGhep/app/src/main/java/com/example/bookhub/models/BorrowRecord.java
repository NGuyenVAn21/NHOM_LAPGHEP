package com.example.bookhub.models;

public class BorrowRecord {
    private int recordId;
    private int bookId;
    private String title;
    private String author;
    private String coverUrl;
    private String borrowDate;
    private String dueDate;
    private String returnDate;
    private String status;       // Status gốc (Borrowing, Returned...)
    private String displayStatus; // Hiển thị (Đang mượn, Quá hạn...)
    private String statusColor;   // Mã màu (#FF0000...)

    // Getter
    public int getRecordId() { return recordId; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getCoverUrl() { return coverUrl; }
    public String getBorrowDate() { return borrowDate; }
    public String getDueDate() { return dueDate; }
    public String getReturnDate() { return returnDate; }
    public String getDisplayStatus() { return displayStatus; }
    public String getStatusColor() { return statusColor; }
    public String getStatus() { return status; }
}