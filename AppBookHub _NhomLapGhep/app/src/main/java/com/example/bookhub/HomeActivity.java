package com.example.bookhub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

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
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        LinearLayout navHome = findViewById(R.id.nav_home_container);
        LinearLayout navBooks = findViewById(R.id.nav_books_container);
        LinearLayout navBorrow = findViewById(R.id.nav_borrow_container);
        LinearLayout navProfile = findViewById(R.id.nav_profile_container);

        // 2. Bắt sự kiện Click

        // Nút Trang chủ (Hiện tại)
        navHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Đang ở trang chủ nên không làm gì, hoặc có thể reload lại
            }
        });

        // Nút Sách -> Chuyển sang BookSearchActivity
        navBooks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, BookSearchActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0); // Tắt hiệu ứng chuyển cảnh để mượt hơn
            }
        });

        // Nút Mượn sách -> Chuyển sang BorrowHistoryActivity
        navBorrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, BorrowHistoryActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

        // Nút Tài khoản -> Chuyển sang AccountActivity
        navProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, AccountActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });
    }
}