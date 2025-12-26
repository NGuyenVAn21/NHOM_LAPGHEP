package com.example.bookhub.api;

import com.example.bookhub.models.ActionRequest;
import com.example.bookhub.models.ActionResponse;
import com.example.bookhub.models.Book;
import com.example.bookhub.models.BorrowRecord;
import com.example.bookhub.models.BorrowRequest;
import com.example.bookhub.models.ChangePasswordRequest;
import com.example.bookhub.models.CheckStatusResponse;
import com.example.bookhub.models.Event;
import com.example.bookhub.models.ProfileStatsResponse;
import com.example.bookhub.models.RegisterResponse;
import com.example.bookhub.models.RegistrationRequest;
import com.example.bookhub.models.UpdateProfileRequest;
import com.example.bookhub.models.UserDetail;
import com.example.bookhub.models.UserRank;
import com.example.bookhub.models.UserStatsResponse;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // --- AUTH (ĐĂNG NHẬP / ĐĂNG KÝ) ---
    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("api/auth/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    // --- BOOKS (SÁCH) ---
    @GET("api/books")
    Call<List<Book>> getAllBooks();

    @GET("api/books/new")
    Call<List<Book>> getNewBooks();

    @GET("api/books/popular")
    Call<List<Book>> getPopularBooks();

    // Lấy chi tiết sách theo ID
    @GET("api/books/detail")
    Call<Book> getBookDetail(@Query("id") int bookId);

    // --- EVENTS (SỰ KIỆN) ---
    @GET("api/events")
    Call<List<Event>> getAllEvents();

    @POST("api/events/register")
    Call<ResponseBody> registerEvent(@Body RegistrationRequest request);

    @GET("api/events/check-status")
    Call<CheckStatusResponse> checkRegistrationStatus(@Query("userId") int userId, @Query("eventId") int eventId);

    // --- STATS (THỐNG KÊ) ---
    @GET("api/stats/active-readers")
    Call<List<UserRank>> getActiveReaders();

    @GET("api/stats/user-summary")
    Call<UserStatsResponse> getUserStats(@Query("userId") int userId);

    @GET("api/stats/profile-stats")
    Call<ProfileStatsResponse> getProfileStats(@Query("userId") int userId);

    // --- BORROW (MƯỢN / TRẢ / ĐẶT TRƯỚC SÁCH) ---

    // 1. Lấy danh sách lịch sử/hiện tại
    @GET("api/borrow/current")
    Call<List<BorrowRecord>> getCurrentBorrows(@Query("userId") int userId);

    @GET("api/borrow/history")
    Call<List<BorrowRecord>> getHistory(@Query("userId") int userId);

    @GET("api/borrow/reservations")
    Call<List<BorrowRecord>> getReservations(@Query("userId") int userId);

    // 2. Các hành động (Tạo mới, Trả, Gia hạn, Hủy)
    @POST("api/borrow/create")
    Call<ActionResponse> borrowBook(@Body BorrowRequest request);

    @POST("api/borrow/return")
    Call<ActionResponse> returnBook(@Body ActionRequest request);

    @POST("api/borrow/extend")
    Call<ActionResponse> extendBook(@Body ActionRequest request);

    @POST("api/borrow/cancel")
    Call<ActionResponse> cancelReservation(@Body ActionRequest request);

    @POST("api/borrow/reserve")
    Call<ActionResponse> reserveBook(@Body BorrowRequest request);

    // --- USER PROFILE (NGƯỜI DÙNG) ---
    @GET("api/users/{id}")
    Call<UserDetail> getUserProfile(@Path("id") int id);

    // Cập nhật thông tin
    @PUT("api/users/{id}")
    Call<ResponseBody> updateProfile(@Path("id") int id, @Body UpdateProfileRequest request);

    // Đổi mật khẩu
    @POST("api/users/change-password")
    Call<ResponseBody> changePassword(@Body ChangePasswordRequest request);

}