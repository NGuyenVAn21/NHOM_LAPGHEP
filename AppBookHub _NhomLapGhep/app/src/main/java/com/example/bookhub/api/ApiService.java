package com.example.bookhub.api;

<<<<<<< HEAD
import com.example.bookhub.Book;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
=======
import com.example.bookhub.models.LoginRequest;
import com.example.bookhub.models.LoginResponse;
import com.example.bookhub.models.RegisterRequest;
import com.example.bookhub.models.RegisterResponse;

import retrofit2.Call;
import retrofit2.http.Body;
>>>>>>> c2cac68d4af52a97d36a6f7803a2b9c2807cb0c6
import retrofit2.http.POST;

public interface ApiService {

<<<<<<< HEAD
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
}
=======
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
>>>>>>> c2cac68d4af52a97d36a6f7803a2b9c2807cb0c6
