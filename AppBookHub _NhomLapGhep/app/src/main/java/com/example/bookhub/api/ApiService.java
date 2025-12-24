package com.example.bookhub.api;

import com.example.bookhub.Book;
import com.example.bookhub.Chapter;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.POST;

public interface ApiService {

    // Gọi API Đăng nhập
    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    // Gọi API Đăng ký
    @POST("api/auth/register")
    Call<Void> register(@Body RegisterRequest request);

    // Gọi API Lấy tất cả sách
    @GET("api/books")
    Call<List<Book>> getAllBooks();

    // API Lấy chi tiết sách theo ID
    @GET("api/books/{id}")
    Call<Book> getBookById(@Path("id") int bookId);

    // API Tìm kiếm sách
    @GET("api/books/search")
    Call<List<Book>> searchBooks(@Query("keyword") String keyword);

    // API Lấy danh sách chương của sách
    @GET("api/reading/chapters/{bookId}")
    Call<List<Chapter>> getChapters(@Path("bookId") int bookId);

    // API Lấy nội dung chương
    @GET("api/reading/chapter/{chapterId}")
    Call<Chapter> getChapterContent(@Path("chapterId") int chapterId);
}