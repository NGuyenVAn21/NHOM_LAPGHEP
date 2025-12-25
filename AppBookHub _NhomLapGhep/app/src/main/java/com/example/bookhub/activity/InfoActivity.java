package com.example.bookhub.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookhub.R;
import com.example.bookhub.api.RetrofitClient;
import com.example.bookhub.models.UserDetail;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        TextView tvName = findViewById(R.id.tv_info_name);
        TextView tvEmail = findViewById(R.id.tv_info_email);
        TextView tvPhone = findViewById(R.id.tv_info_phone);
        // ... nút back ...

        // Lấy ID user hiện tại
        SharedPreferences prefs = getSharedPreferences("BookHubPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("CURRENT_USER_ID", -1);

        // GỌI API
        if (userId != -1) {
            RetrofitClient.getApiService().getUserProfile(userId).enqueue(new Callback<UserDetail>() {
                @Override
                public void onResponse(Call<UserDetail> call, Response<UserDetail> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        UserDetail user = response.body();
                        tvName.setText(user.getFullName());
                        tvEmail.setText(user.getEmail());
                        tvPhone.setText(user.getPhone().isEmpty() ? "Chưa cập nhật" : user.getPhone());
                    }
                }
                @Override
                public void onFailure(Call<UserDetail> call, Throwable t) { }
            });
        }
    }
}
