package com.example.bookhub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        TextView seeAllEvents = findViewById(R.id.text_see_all_events);

        // Bắt sự kiện click vào "Xem tất cả"
        seeAllEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chuyển sang màn hình EventsActivity
                Intent intent = new Intent(HomeActivity.this, EventsActivity.class);
                startActivity(intent);
            }
        });

        // Xử lý click cho Card Event nổi bật (Mô phỏng)
        findViewById(R.id.card_featured_event_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, EventDetailActivity.class);
                intent.putExtra("EVENT_TITLE", "Tuần lễ Sách Mới");
                startActivity(intent);
            }
        });
    }
}