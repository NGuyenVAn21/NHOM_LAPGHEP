package com.example.bookhub;

import android.os.Parcel;
import android.os.Parcelable;

public class Book implements Parcelable {
    private int id;
    private String title;
    private String author;
    private float rating;
    private int pages;
    private String status;
    private String category;
    private String publisher;
    private int year;
    private String description;
    private String price;
    private int reviews;

    public Book(int id, String title, String author, float rating, int pages,
                String status, String category, String publisher, int year, String description) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.rating = rating;
        this.pages = pages;
        this.status = status;
        this.category = category;
        this.publisher = publisher;
        this.year = year;
        this.description = description;
        this.price = "200.000 VND";
        this.reviews = 2548;
    }

    protected Book(Parcel in) {
        id = in.readInt();
        title = in.readString();
        author = in.readString();
        rating = in.readFloat();
        pages = in.readInt();
        status = in.readString();
        category = in.readString();
        publisher = in.readString();
        year = in.readInt();
        description = in.readString();
        price = in.readString();
        reviews = in.readInt();
    }

    public static final Creator<Book> CREATOR = new Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public float getRating() { return rating; }
    public int getPages() { return pages; }
    public String getStatus() { return status; }
    public String getCategory() { return category; }
    public String getPublisher() { return publisher; }
    public int getYear() { return year; }
    public String getDescription() { return description; }
    public String getPrice() { return price; }
    public int getReviews() { return reviews; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(author);
        dest.writeFloat(rating);
        dest.writeInt(pages);
        dest.writeString(status);
        dest.writeString(category);
        dest.writeString(publisher);
        dest.writeInt(year);
        dest.writeString(description);
        dest.writeString(price);
        dest.writeInt(reviews);
    }

}