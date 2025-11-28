package com.example.bookhub;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

// TÔI ĐÃ THÊM DÒNG NÀY: Import nút bấm Material
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private TextView tvWelcome;

    // TÔI ĐÃ THÊM DÒNG NÀY: Khai báo 2 nút bấm
    private MaterialButton btnViewBooks, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Lấy SharedPreferences
        sharedPreferences = getSharedPreferences("BookHubPrefs", MODE_PRIVATE);

        // 2. Kiểm tra đăng nhập (Nếu chưa thì đá về Login ngay)
        if (!isLoggedIn()) {
            navigateToLogin();
            return;
        }

        // 3. Cấu hình Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Trang chủ");
        }

        // 4. Hiển thị lời chào
        tvWelcome = findViewById(R.id.tvWelcome);

        String fullName = sharedPreferences.getString("fullName", "");
        String username = sharedPreferences.getString("username", "User");

        if (!fullName.isEmpty()) {
            tvWelcome.setText("Xin chào, " + fullName + "!");
        } else {
            tvWelcome.setText("Xin chào, " + username + "!");
        }

        // --- PHẦN CODE MỚI THÊM VÀO ---

        // 5. Ánh xạ nút bấm từ XML (activity_main.xml phải có id btnViewBooks và btnLogout)
        btnViewBooks = findViewById(R.id.btnViewBooks);
        btnLogout = findViewById(R.id.btnLogout);

        // 6. Xử lý sự kiện: Nút "Vào xem Tủ Sách"
        btnViewBooks.setOnClickListener(v -> {
            // Chuyển sang màn hình MyBooksActivity (Trang chứa các Tab Đang mượn/Lịch sử...)
            Intent intent = new Intent(MainActivity.this, MyBooksActivity.class);
            startActivity(intent);
        });

        // 7. Xử lý sự kiện: Nút "Đăng xuất" (Nút to ở giữa màn hình)
        btnLogout.setOnClickListener(v -> logout());

        // --- HẾT PHẦN CODE MỚI ---
    }

    // --- VẪN GIỮ MENU ĐĂNG XUẤT Ở GÓC TRÊN (DỰ PHÒNG) ---
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 101, 0, "Đăng xuất")
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 101) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isLoggedIn() {
        return sharedPreferences.getBoolean("isLoggedIn", false);
    }

    private void logout() {
        // Xóa dữ liệu đăng nhập
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Toast.makeText(this, "Đăng xuất thành công!", Toast.LENGTH_SHORT).show();
        navigateToLogin();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        // Xóa lịch sử activity để không bấm Back quay lại được
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}