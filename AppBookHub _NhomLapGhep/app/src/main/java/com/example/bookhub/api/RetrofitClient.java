package com.example.bookhub.api;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    // LƯU Ý: Kiểm tra lại Port của server bạn.
    // HEAD dùng 5280, Code mới dùng 5177. Mình đang để 5177 theo code mới.
    private static final String BASE_URL = "http://10.0.2.2:5280/";

    private static Retrofit retrofit;
    private static ApiService apiService;

    // Cấu hình Retrofit Client
    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {

            // 1. Cấu hình Logging để xem Log API trong Logcat (Rất quan trọng khi debug)
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            // 2. Cấu hình Timeout để tránh app bị treo khi mạng lag
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .connectTimeout(30, TimeUnit.SECONDS) // Thời gian chờ kết nối
                    .readTimeout(30, TimeUnit.SECONDS)    // Thời gian chờ đọc dữ liệu
                    .writeTimeout(30, TimeUnit.SECONDS)   // Thời gian chờ gửi dữ liệu
                    .build();

            // 3. Khởi tạo Retrofit
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient) // Gán OkHttp đã cấu hình vào
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    // Singleton lấy ApiService
    public static ApiService getApiService() {
        if (apiService == null) {
            apiService = getRetrofitInstance().create(ApiService.class);
        }
        return apiService;
    }
}