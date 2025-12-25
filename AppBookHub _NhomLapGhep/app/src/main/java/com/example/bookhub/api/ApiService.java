package com.example.bookhub.api;

import com.example.bookhub.models.Book;
import com.example.bookhub.models.Event;
import com.example.bookhub.models.RegisterResponse;
import com.example.bookhub.models.RegistrationRequest;
import com.example.bookhub.models.CheckStatusResponse;
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

    // Gọi API Đăng nhập
    // LoginRequest và LoginResponse là 2 class ta sẽ tạo ở bước sau
    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    // Gọi API Đăng ký
    @POST("auth/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    // Gọi API Lấy sách
    @GET("api/books")
    Call<List<Book>> getAllBooks();

    @GET("api/events")
    Call<List<Event>> getAllEvents();

    @POST("api/events/register")
    Call<ResponseBody> registerEvent(@Body RegistrationRequest request);

    @GET("api/events/check-status")
    Call<CheckStatusResponse> checkRegistrationStatus(@retrofit2.http.Query("userId") int userId,
                                                      @retrofit2.http.Query("eventId") int eventId);

    @GET("api/stats/active-readers")
    Call<List<UserRank>> getActiveReaders();

    @GET("api/books/new")
    Call<List<Book>> getNewBooks();

    @GET("api/books/popular")
    Call<List<Book>> getPopularBooks();

    @GET("api/stats/user-summary")
    Call<UserStatsResponse> getUserStats(@Query("userId") int userId);
}