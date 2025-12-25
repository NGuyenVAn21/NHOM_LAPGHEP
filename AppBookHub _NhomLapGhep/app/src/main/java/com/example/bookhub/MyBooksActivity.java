package com.example.bookhub;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MyBooksActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_books);

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager = findViewById(R.id.viewPager);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);

        // 1. Cấu hình ViewPager
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // 2. Gắn Tab với ViewPager
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Đang mượn"); break;
                case 1: tab.setText("Lịch sử"); break;
                case 2: tab.setText("Đặt trước"); break;
            }
        }).attach();

        // 3. Cấu hình Bottom Navigation
        // Mặc định chọn tab "Mượn sách"
        bottomNav.setSelectedItemId(R.id.nav_borrow);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                finish(); // Quay về trang chủ
                return true;
            }
            // Các nút khác xử lý sau
            return true;
        });
    }

    // Adapter để chuyển đổi nội dung 3 Tab
    private class ViewPagerAdapter extends FragmentStateAdapter {
        public ViewPagerAdapter(AppCompatActivity activity) {
            super(activity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0: return new BorrowingFragment();     // Tab Đang mượn
                case 1: return new HistoryFragment();       // Tab Lịch sử
                case 2: return new ReservationsFragment();  // Tab Đặt trước
                default: return new BorrowingFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }
}