package com.example.bookhub.api;

import com.example.bookhub.models.Book;
import com.example.bookhub.models.BorrowRecord;
import com.example.bookhub.models.BorrowRequest;
import com.example.bookhub.models.ActionRequest; // Import cho các hàm trả/hủy
import com.example.bookhub.models.ActionResponse; // Import cho kết quả trả về
import com.example.bookhub.models.CheckStatusResponse;
import com.example.bookhub.models.Event;
import com.example.bookhub.models.RegisterResponse;
import com.example.bookhub.models.RegistrationRequest;
import com.example.bookhub.models.UserRank;
import com.example.bookhub.models.UserStatsResponse;

import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    // --- AUTH ---
    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("api/auth/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    // --- BOOKS ---
    @GET("api/books")
    Call<List<Book>> getAllBooks();

    @GET("api/books/new")
    Call<List<Book>> getNewBooks();

    @GET("api/books/popular")
    Call<List<Book>> getPopularBooks();

    // --- EVENTS ---
    @GET("api/events")
    Call<List<Event>> getAllEvents();

    @POST("api/events/register")
    Call<ResponseBody> registerEvent(@Body RegistrationRequest request);

    @GET("api/events/check-status")
    Call<CheckStatusResponse> checkRegistrationStatus(@Query("userId") int userId, @Query("eventId") int eventId);

    // --- STATS ---
    @GET("api/stats/active-readers")
    Call<List<UserRank>> getActiveReaders();

    @GET("api/stats/user-summary")
    Call<UserStatsResponse> getUserStats(@Query("userId") int userId);

    // --- BORROW (MƯỢN TRẢ SÁCH) ---

    // 1. Lấy danh sách
    @GET("api/borrow/current")
    Call<List<BorrowRecord>> getCurrentBorrows(@Query("userId") int userId);

    @GET("api/borrow/history")
    Call<List<BorrowRecord>> getHistory(@Query("userId") int userId);

    @GET("api/borrow/reservations")
    Call<List<BorrowRecord>> getReservations(@Query("userId") int userId);

    // 2. Các hành động (Nút bấm)
    // Lưu ý: Đã dùng ActionResponse và ActionRequest ngắn gọn nhờ import bên trên
    @POST("api/borrow/return")
    Call<ActionResponse> returnBook(@Body ActionRequest request);

    @POST("api/borrow/extend")
    Call<ActionResponse> extendBook(@Body ActionRequest request);

    @POST("api/borrow/cancel")
    Call<ActionResponse> cancelReservation(@Body ActionRequest request);

    // 3. API MƯỢN SÁCH
    @POST("api/borrow/create")
    Call<ActionResponse> borrowBook(@Body BorrowRequest request);
    // Lấy chi tiết sách theo ID
    @GET("api/books/detail")
    Call<Book> getBookDetail(@Query("id") int bookId);
}