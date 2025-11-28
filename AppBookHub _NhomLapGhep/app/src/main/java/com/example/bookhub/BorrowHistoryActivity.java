package com.example.bookhub;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class BorrowHistoryActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private BorrowPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrow_history);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Tạo adapter cho ViewPager2
        pagerAdapter = new BorrowPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // Kết nối TabLayout với ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Đang mượn");
                    break;
                case 1:
                    tab.setText("Lịch sử");
                    break;
                case 2:
                    tab.setText("Đặt trước");
                    break;
            }
        }).attach();
        setupBottomNavigation();
    }
    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.navigation_borrow);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.navigation_home) {
                    Intent intent = new Intent(BorrowHistoryActivity.this, HomeActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;

                } else if (itemId == R.id.navigation_books) {
                    Intent intent = new Intent(BorrowHistoryActivity.this, BookSearchActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;

                } else if (itemId == R.id.navigation_borrow) {
                    return true;

                } else if (itemId == R.id.navigation_account) {
                    Intent intent = new Intent(BorrowHistoryActivity.this, AccountActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;
                }

                return false;
            }
        });
    }

}