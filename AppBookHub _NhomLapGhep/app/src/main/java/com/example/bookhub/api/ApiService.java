package com.example.bookhub.api;

import com.example.bookhub.model.Book;
import com.example.bookhub.model.Event;
import com.example.bookhub.model.RegistrationRequest;
import com.example.bookhub.model.CheckStatusResponse;
import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {

    // Gọi API Đăng nhập
    // LoginRequest và LoginResponse là 2 class ta sẽ tạo ở bước sau
    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    // Gọi API Đăng ký
    @POST("api/auth/register")
    Call<Void> register(@Body RegisterRequest request);

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
}