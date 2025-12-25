package com.example.bookhub.api;

<<<<<<< HEAD
=======
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
>>>>>>> c2cac68d4af52a97d36a6f7803a2b9c2807cb0c6
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
<<<<<<< HEAD
    // QUAN TRỌNG: Dùng IP 10.0.2.2 để máy ảo trỏ về localhost của máy tính
    private static final String BASE_URL = "http://10.0.2.2:5280/";
    private static Retrofit retrofit = null;

    public static ApiService getApiService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
=======

    // Thay đổi BASE_URL này theo API server của bạn
    private static final String BASE_URL = "http://10.0.2.2:5177/";


    private static Retrofit retrofit;
    private static ApiService apiService;

    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {

            // Logging interceptor để debug
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            // OkHttp client với timeout
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static ApiService getApiService() {
        if (apiService == null) {
            apiService = getRetrofitInstance().create(ApiService.class);
        }
        return apiService;
>>>>>>> c2cac68d4af52a97d36a6f7803a2b9c2807cb0c6
    }
}