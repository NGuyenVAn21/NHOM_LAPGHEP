package com.example.bookhub.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.bookhub.R;

public class EventDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        // Ánh xạ View
        TextView titleDetail = findViewById(R.id.event_title_detail);
        TextView descDetail = findViewById(R.id.event_description);
        TextView tvStart = findViewById(R.id.tv_start_date);
        TextView tvEnd = findViewById(R.id.tv_end_date);
        ImageView imgBanner = findViewById(R.id.img_detail_banner);
        Button registerButton = findViewById(R.id.button_register);

        // Nhận dữ liệu từ Intent (do Adapter gửi sang)
        String title = getIntent().getStringExtra("EVENT_TITLE");
        String desc = getIntent().getStringExtra("EVENT_DESC");
        String start = getIntent().getStringExtra("EVENT_START");
        String end = getIntent().getStringExtra("EVENT_END");
        String imageUrl = getIntent().getStringExtra("EVENT_IMAGE");

        // Hiển thị dữ liệu
        titleDetail.setText(title);
        descDetail.setText(desc);
        tvStart.setText(start);
        tvEnd.setText(end);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this).load(imageUrl).into(imgBanner);
        }

        // Cấu hình nút Back
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chi tiết Sự kiện");
        }

        // Xử lý nút Đăng ký (Tạm thời chỉ thông báo)
        registerButton.setOnClickListener(v ->
                Toast.makeText(EventDetailActivity.this, "Đăng ký thành công: " + title, Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}