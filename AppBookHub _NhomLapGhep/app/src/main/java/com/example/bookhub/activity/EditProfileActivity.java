package com.example.bookhub.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookhub.R;
import com.example.bookhub.api.RetrofitClient;
// Kiểm tra lại package model của bạn là 'model' hay 'models' nhé
import com.example.bookhub.models.UpdateProfileRequest;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    // 1. KHAI BÁO BIẾN
    private EditText etFullName, etEmail, etPhone;
    private Button btnSave;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // 2. ÁNH XẠ VIEW (Kết nối code Java với giao diện XML)
        etFullName = findViewById(R.id.et_edit_fullname);
        etEmail = findViewById(R.id.et_edit_email);
        etPhone = findViewById(R.id.et_edit_phone);
        btnSave = findViewById(R.id.btn_save_profile);
        btnBack = findViewById(R.id.btn_back_edit_profile);

        // 3. HIỂN THỊ DỮ LIỆU CŨ LÊN (Để người dùng biết mình đang sửa cái gì)
        loadCurrentInfo();

        // 4. XỬ LÝ NÚT BACK
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // 5. XỬ LÝ NÚT LƯU
        if (btnSave != null) {
            btnSave.setOnClickListener(v -> handleUpdateProfile());
        }
    }

    private void loadCurrentInfo() {
        SharedPreferences prefs = getSharedPreferences("BookHubPrefs", MODE_PRIVATE);
        String currentName = prefs.getString("CURRENT_USER_NAME", "");
        // Nếu bạn có lưu email/phone trong prefs lúc login thì lấy ra ở đây, không thì để trống

        if (etFullName != null) etFullName.setText(currentName);
    }

    private void handleUpdateProfile() {
        String name = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Tên không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("BookHubPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("CURRENT_USER_ID", -1);

        if (userId == -1) {
            Toast.makeText(this, "Lỗi xác thực người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo request gửi lên Server
        UpdateProfileRequest request = new UpdateProfileRequest(name, email, phone);

        RetrofitClient.getApiService().updateProfile(userId, request).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(EditProfileActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();

                    // Cập nhật lại Cache tên để trang Home hiển thị đúng ngay lập tức
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("CURRENT_USER_NAME", name);
                    editor.apply();

                    finish(); // Đóng màn hình này
                } else {
                    Toast.makeText(EditProfileActivity.this, "Lỗi cập nhật: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(EditProfileActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}