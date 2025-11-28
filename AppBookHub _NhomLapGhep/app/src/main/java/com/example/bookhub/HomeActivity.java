package com.example.bookhub;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        TextView seeAllEvents = findViewById(R.id.text_see_all_events);


        if (seeAllEvents != null) {
            seeAllEvents.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(HomeActivity.this, EventsActivity.class);
                    startActivity(intent);
                }
            });
        }

        // Xử lý click cho Card Event nổi bật
        View featuredEvent = findViewById(R.id.card_featured_event_1);
        if (featuredEvent != null) {
            featuredEvent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(HomeActivity.this, EventDetailActivity.class);
                    intent.putExtra("EVENT_TITLE", "Tuần lễ Sách Mới");
                    startActivity(intent);
                }
            });
        }
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        // Đặt mục "Trang chủ" là item được chọn mặc định
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.navigation_home) {
                    return true;

                } else if (itemId == R.id.navigation_books) {
                    Intent intent = new Intent(HomeActivity.this, BookSearchActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;

                } else if (itemId == R.id.navigation_borrow) {
                    Intent intent = new Intent(HomeActivity.this, BorrowHistoryActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;

                } else if (itemId == R.id.navigation_account) {
                    Intent intent = new Intent(HomeActivity.this, AccountActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;
                }

                return false;
            }
        });
    }
}