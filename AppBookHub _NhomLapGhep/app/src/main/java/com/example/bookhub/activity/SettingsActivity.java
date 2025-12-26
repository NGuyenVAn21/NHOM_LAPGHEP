package com.example.bookhub.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.bookhub.R;

public class SettingsActivity extends AppCompatActivity {

    private Switch switchDarkMode;
    private RadioGroup rgFont;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Khởi tạo SharedPreferences
        prefs = getSharedPreferences("BookHubPrefs", MODE_PRIVATE);

        // Ánh xạ View
        switchDarkMode = findViewById(R.id.switchDarkMode);
        rgFont = findViewById(R.id.rgFont);

        // 1. Cài đặt trạng thái ban đầu từ bộ nhớ
        setupInitialState();

        // 2. Xử lý Chế độ Nền tối (Dark Mode)
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                prefs.edit().putBoolean("DARK_MODE", true).apply();
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                prefs.edit().putBoolean("DARK_MODE", false).apply();
            }
        });

        // 3. Xử lý Cỡ chữ (Lưu vào bộ nhớ để ReadingActivity đọc)
        rgFont.setOnCheckedChangeListener((group, checkedId) -> {
            SharedPreferences.Editor editor = prefs.edit();
            if (checkedId == R.id.fontSmall) {
                editor.putInt("FONT_SIZE", 14); // Cỡ nhỏ
            } else if (checkedId == R.id.fontMedium) {
                editor.putInt("FONT_SIZE", 18); // Cỡ vừa (Mặc định)
            } else if (checkedId == R.id.fontLarge) {
                editor.putInt("FONT_SIZE", 24); // Cỡ to
            }
            editor.apply();
            Toast.makeText(this, "Đã lưu cài đặt cỡ chữ", Toast.LENGTH_SHORT).show();
        });

        // Nút Back trên toolbar (nếu có)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Cài đặt");
        }
    }

    private void setupInitialState() {
        // Load Dark Mode
        boolean isDarkMode = prefs.getBoolean("DARK_MODE", false);
        switchDarkMode.setChecked(isDarkMode);

        // Load Font Size
        int currentFont = prefs.getInt("FONT_SIZE", 18);
        if (currentFont == 14) {
            rgFont.check(R.id.fontSmall);
        } else if (currentFont == 24) {
            rgFont.check(R.id.fontLarge);
        } else {
            rgFont.check(R.id.fontMedium);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}