package com.example.bookhub;

import android.content.Intent;
<<<<<<< HEAD
import android.os.Bundle;
import android.util.Log;
=======
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
>>>>>>> c2cac68d4af52a97d36a6f7803a2b9c2807cb0c6
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

<<<<<<< HEAD
import com.example.bookhub.api.ApiService;
import com.example.bookhub.api.LoginRequest;
import com.example.bookhub.api.LoginResponse;
import com.example.bookhub.api.RetrofitClient;
=======
import com.example.bookhub.api.RetrofitClient;
import com.example.bookhub.models.LoginRequest;
import com.example.bookhub.models.LoginResponse;
>>>>>>> c2cac68d4af52a97d36a6f7803a2b9c2807cb0c6
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

<<<<<<< HEAD
    private EditText etUsername;
    private EditText etPassword;
    private MaterialButton btnLogin;
    private TextView tvForgotPassword;
    private TextView tvRegister;
    private ProgressBar progressBar;
=======
    // SỬA Ở ĐÂY: Dùng EditText thay vì TextInputEditText
    private EditText etUsername, etPassword;
    private MaterialButton btnLogin;
    private TextView tvRegister, tvForgotPassword;
    private ProgressBar progressBar;
    private SharedPreferences sharedPreferences;
>>>>>>> c2cac68d4af52a97d36a6f7803a2b9c2807cb0c6

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

<<<<<<< HEAD
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
=======
        sharedPreferences = getSharedPreferences("BookHubPrefs", MODE_PRIVATE);

        if (isLoggedIn()) {
            navigateToHome();
            return;
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        // Ánh xạ đúng ID trong XML
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> performLogin());

        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        tvForgotPassword.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng đang được phát triển", Toast.LENGTH_SHORT).show();
        });
    }

    private void performLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
>>>>>>> c2cac68d4af52a97d36a6f7803a2b9c2807cb0c6
            etUsername.setError("Vui lòng nhập tên đăng nhập");
            etUsername.requestFocus();
            return;
        }

<<<<<<< HEAD
        if (password.isEmpty()) {
=======
        if (TextUtils.isEmpty(password)) {
>>>>>>> c2cac68d4af52a97d36a6f7803a2b9c2807cb0c6
            etPassword.setError("Vui lòng nhập mật khẩu");
            etPassword.requestFocus();
            return;
        }

<<<<<<< HEAD
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
=======
        showLoading(true);

        LoginRequest loginRequest = new LoginRequest(username, password);

        Call<LoginResponse> call = RetrofitClient.getApiService().login(loginRequest);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    // Vì API C# trả về JSON có thể khác cấu trúc một chút, cần kiểm tra kỹ
                    // Giả sử C# trả về token thì coi như thành công
                    if (loginResponse.getToken() != null) {
                        saveLoginInfo(loginResponse);
                        Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                        navigateToHome();
                    } else {
                        Toast.makeText(LoginActivity.this, "Đăng nhập thất bại", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show();
>>>>>>> c2cac68d4af52a97d36a6f7803a2b9c2807cb0c6
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
<<<<<<< HEAD
                // TRƯỜNG HỢP LỖI MẠNG (Không kết nối được server, timeout...)
                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);
                Log.e("API_ERROR", "Lỗi: " + t.getMessage());
                Toast.makeText(LoginActivity.this, "Lỗi kết nối Server! Vui lòng kiểm tra mạng.", Toast.LENGTH_SHORT).show();
            }
        });
    }
=======
                showLoading(false);
                Toast.makeText(LoginActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnLogin.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnLogin.setEnabled(true);
        }
    }

    private void saveLoginInfo(LoginResponse response) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putString("token", response.getToken());

        if (response.getUser() != null) {
            editor.putInt("userId", response.getUser().getId());
            editor.putString("username", response.getUser().getUsername());
            editor.putString("fullName", response.getUser().getFullName());
            editor.putString("email", response.getUser().getEmail());
        }
        editor.apply();
    }

    private boolean isLoggedIn() {
        return sharedPreferences.getBoolean("isLoggedIn", false);
    }

    private void navigateToHome() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
>>>>>>> c2cac68d4af52a97d36a6f7803a2b9c2807cb0c6
}