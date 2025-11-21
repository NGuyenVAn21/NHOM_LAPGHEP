package com.example.bookhub.api;

import com.example.bookhub.models.LoginRequest;
import com.example.bookhub.models.LoginResponse;
import com.example.bookhub.models.RegisterRequest;
import com.example.bookhub.models.RegisterResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    // Endpoint đăng nhập
    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    // Endpoint đăng ký
    @POST("api/auth/register")
    Call<RegisterResponse> register(@Body RegisterRequest registerRequest);

    // Bạn có thể thêm các endpoint khác tại đây
    // Ví dụ:
    // @GET("api/books")
    // Call<List<Book>> getBooks();

    // @POST("api/auth/forgot-password")
    // Call<ForgotPasswordResponse> forgotPassword(@Body ForgotPasswordRequest request);
}
