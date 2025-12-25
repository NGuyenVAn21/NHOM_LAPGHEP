package com.example.bookhub.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookhub.R;
import com.example.bookhub.api.ApiService;
import com.example.bookhub.api.LoginRequest;
import com.example.bookhub.api.LoginResponse;
import com.example.bookhub.api.RetrofitClient;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private MaterialButton btnLogin;
    private TextView tvForgotPassword;
    private TextView tvRegister;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 1. Khởi tạo các view
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvRegister = findViewById(R.id.tvRegister);
        progressBar = findViewById(R.id.progressBar);

        // 2. Xử lý sự kiện nút Đăng nhập
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogin();
            }
        });

        // 3. Xử lý sự kiện Quên mật khẩu
        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, "Chức năng đang phát triển", Toast.LENGTH_SHORT).show();
            }
        });

        // 4. Xử lý sự kiện chuyển sang màn hình Đăng ký
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void handleLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Kiểm tra dữ liệu đầu vào
        if (username.isEmpty()) {
            etUsername.setError("Vui lòng nhập tên đăng nhập");
            etUsername.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Vui lòng nhập mật khẩu");
            etPassword.requestFocus();
            return;
        }

        // Hiển thị progress bar và khóa nút bấm
        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        // GỌI API
        // 1. Tạo đối tượng request
        LoginRequest request = new LoginRequest(username, password);
        // 2. Gọi hàm login từ ApiService thông qua RetrofitClient
        ApiService apiService = RetrofitClient.getApiService();
        Call<LoginResponse> call = apiService.login(request);
        // 3. Thực thi call (enqueue chạy bất đồng bộ - không làm đơ ứng dụng)
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                // Tắt loading
                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    // TRƯỜNG HỢP THÀNH CÔNG
                    LoginResponse loginResponse = response.body();
                    int userId = loginResponse.getUser().getId(); // <--- Lấy ID
                    String fullName = loginResponse.getUser().getFullName();

                    getSharedPreferences("BookHubPrefs", MODE_PRIVATE)
                            .edit()
                            .putInt("CURRENT_USER_ID", userId) // Lưu ID với khóa là "CURRENT_USER_ID"
                            .putString("CURRENT_USER_NAME", fullName) // Lưu tên luôn để dùng sau này
                            .apply();

                    // Hiển thị thông báo chào mừng
                    String welcomeMsg = "Xin chào " + loginResponse.getUser().getFullName();
                    Toast.makeText(LoginActivity.this, welcomeMsg, Toast.LENGTH_SHORT).show();

                    // Chuyển sang màn hình Home
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    // Xóa cờ task để user không bấm Back quay lại màn login được
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();

                } else {
                    // TRƯỜNG HỢP THẤT BẠI (Sai pass, tài khoản không tồn tại...)
                    Toast.makeText(LoginActivity.this, "Sai tài khoản hoặc mật khẩu!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                // TRƯỜNG HỢP LỖI MẠNG (Không kết nối được server, timeout...)
                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);
                Log.e("API_ERROR", "Lỗi: " + t.getMessage());
                Toast.makeText(LoginActivity.this, "Lỗi kết nối Server! Vui lòng kiểm tra mạng.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}