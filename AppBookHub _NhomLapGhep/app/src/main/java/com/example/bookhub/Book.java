package com.example.bookhub;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;

public class Book implements Parcelable {
    @SerializedName("id") private int id;
    @SerializedName("title") private String title;
    @SerializedName("author") private String author;
    @SerializedName("rating") private float rating;
    @SerializedName("pages") private int pages;
    @SerializedName("status") private String status;
    @SerializedName("category") private String category;
    @SerializedName("publisher") private String publisher;
    @SerializedName("year") private int year;
    @SerializedName("description") private String description;
    @SerializedName("price") private String price;
    @SerializedName("reviews") private int reviews;
    @SerializedName("imageUrl") private String imageUrl;
    @SerializedName("stock") private int stock;

    // Constructor rỗng cho Gson
    public Book() {}

    // Parcelable
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
        imageUrl = in.readString();
        stock = in.readInt();
    }

    public static final Creator<Book> CREATOR = new Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel in) { return new Book(in); }
        @Override
        public Book[] newArray(int size) { return new Book[size]; }
    };

    @Override
    public int describeContents() { return 0; }

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
        dest.writeString(imageUrl);
        dest.writeInt(stock);
    }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public float getRating() { return rating; }
    public int getPages() { return pages; }
    public String getStatus() { return status; }
    public String getCategory() { return category != null ? category : "Tổng hợp"; }
    public String getPublisher() { return publisher != null ? publisher : "Chưa có"; }
    public int getYear() { return year; }
    public String getDescription() { return description; }
    public String getPrice() { return price; }
    public int getReviews() { return reviews; }
    public String getImageUrl() { return imageUrl; }
    public int getStock() { return stock; }
}