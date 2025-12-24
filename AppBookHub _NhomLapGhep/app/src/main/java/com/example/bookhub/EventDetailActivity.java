package com.example.bookhub;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EventDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        // Lấy dữ liệu từ Intent
        String eventTitle = getIntent().getStringExtra("EVENT_TITLE");

        TextView titleDetail = findViewById(R.id.event_title_detail);
        Button registerButton = findViewById(R.id.button_register);

        // Cập nhật tiêu đề trên màn hình và Action Bar
        if (eventTitle != null) {
            titleDetail.setText(eventTitle);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Chi tiết Sự kiện");
            }
        }

        // Bắt sự kiện click nút Đăng ký tham gia
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(EventDetailActivity.this, "Đăng ký thành công sự kiện: " + eventTitle, Toast.LENGTH_SHORT).show();
            }
        });
    }
}