package com.example.bookhub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

public class EventsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        // Giả sử sử dụng ActionBar/Toolbar mặc định cho nút Back
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Danh sách Sự kiện");
        }

        MaterialCardView cardEvent1 = findViewById(R.id.card_event_1);
        MaterialCardView cardEvent2 = findViewById(R.id.card_event_2);

        // Click để chuyển sang EventDetailActivity
        cardEvent1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EventsActivity.this, EventDetailActivity.class);
                intent.putExtra("EVENT_TITLE", "Tuần lễ Sách Mới");
                startActivity(intent);
            }
        });

        cardEvent2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EventsActivity.this, EventDetailActivity.class);
                intent.putExtra("EVENT_TITLE", "Giảm giá 20% Sách Văn Học");
                startActivity(intent);
            }
        });
    }
}