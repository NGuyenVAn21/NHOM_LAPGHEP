package com.example.bookhub.models;

public class BorrowRequest {
    private int userId;
    private int bookId;

    public BorrowRequest(int userId, int bookId) {
        this.userId = userId;
        this.bookId = bookId;
    }
}