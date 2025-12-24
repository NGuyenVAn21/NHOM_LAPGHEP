package com.example.bookhub;

import android.os.Bundle;
<<<<<<< HEAD
=======
import android.text.TextUtils;
>>>>>>> c2cac68d4af52a97d36a6f7803a2b9c2807cb0c6
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
<<<<<<< HEAD
import androidx.appcompat.app.AppCompatActivity;
import com.example.bookhub.api.ApiService;
import com.example.bookhub.api.RegisterRequest;
import com.example.bookhub.api.RetrofitClient;
=======

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookhub.api.RetrofitClient;
import com.example.bookhub.models.RegisterRequest;
import com.example.bookhub.models.RegisterResponse;
>>>>>>> c2cac68d4af52a97d36a6f7803a2b9c2807cb0c6
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

<<<<<<< HEAD
=======
    // SỬA Ở ĐÂY: Dùng EditText
>>>>>>> c2cac68d4af52a97d36a6f7803a2b9c2807cb0c6
    private EditText etFullName, etEmail, etUsername, etPassword, etConfirmPassword;
    private MaterialButton btnRegister;
    private TextView tvLogin;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

<<<<<<< HEAD
=======
        initViews();
        setupListeners();
    }

    private void initViews() {
>>>>>>> c2cac68d4af52a97d36a6f7803a2b9c2807cb0c6
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
        progressBar = findViewById(R.id.progressBar);
<<<<<<< HEAD

        btnRegister.setOnClickListener(v -> handleRegister());
        tvLogin.setOnClickListener(v -> finish());
    }

    private void handleRegister() {
=======
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> performRegister());

        tvLogin.setOnClickListener(v -> finish()); // Quay lại màn hình đăng nhập
    }

    private void performRegister() {
>>>>>>> c2cac68d4af52a97d36a6f7803a2b9c2807cb0c6
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

<<<<<<< HEAD
        // Validation ( logic kiểm tra rỗng )
        if (fullName.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
=======
        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Vui lòng nhập họ tên");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Vui lòng nhập email");
            return;
        }
        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Vui lòng nhập tên đăng nhập");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Vui lòng nhập mật khẩu");
>>>>>>> c2cac68d4af52a97d36a6f7803a2b9c2807cb0c6
            return;
        }
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Mật khẩu không khớp");
            return;
        }

<<<<<<< HEAD
        progressBar.setVisibility(View.VISIBLE);
        btnRegister.setEnabled(false);

        // 1. Tạo request
        RegisterRequest request = new RegisterRequest(fullName, email, username, password);

        // 2. Gọi API
        RetrofitClient.getApiService().register(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                progressBar.setVisibility(View.GONE);
                btnRegister.setEnabled(true);

                if (response.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this, "Đăng ký thành công! Hãy đăng nhập.", Toast.LENGTH_LONG).show();
                    finish(); // Quay về màn hình Login
                } else {
                    // Xử lý lỗi (Ví dụ: Trùng username)
                    Toast.makeText(RegisterActivity.this, "Đăng ký thất bại (Trùng tên/Email)", Toast.LENGTH_SHORT).show();
=======
        showLoading(true);

        RegisterRequest registerRequest = new RegisterRequest(fullName, email, username, password);

        Call<RegisterResponse> call = RetrofitClient.getApiService().register(registerRequest);
        call.enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                showLoading(false);
                // Kiểm tra status code 200 (OK)
                if (response.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_LONG).show();
                    finish(); // Quay về login
                } else {
                    Toast.makeText(RegisterActivity.this, "Đăng ký thất bại (Tài khoản đã tồn tại?)", Toast.LENGTH_SHORT).show();
>>>>>>> c2cac68d4af52a97d36a6f7803a2b9c2807cb0c6
                }
            }

            @Override
<<<<<<< HEAD
            public void onFailure(Call<Void> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnRegister.setEnabled(true);
                Toast.makeText(RegisterActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
=======
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                showLoading(false);
                Toast.makeText(RegisterActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnRegister.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnRegister.setEnabled(true);
        }
    }
>>>>>>> c2cac68d4af52a97d36a6f7803a2b9c2807cb0c6
}