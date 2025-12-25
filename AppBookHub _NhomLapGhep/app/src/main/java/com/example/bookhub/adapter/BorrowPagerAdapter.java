package com.example.bookhub.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.example.bookhub.BorrowingFragment;
import com.example.bookhub.HistoryFragment;
import com.example.bookhub.ReservationsFragment;


public class BorrowPagerAdapter extends FragmentStateAdapter {

    public BorrowPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Tại đây, nó sẽ gọi đến các FILE RỜI chứa code logic API mà chúng ta đã viết
        switch (position) {
            case 0:
                return new BorrowingFragment(); // Gọi file BorrowingFragment.java
            case 1:
                return new HistoryFragment();   // Gọi file HistoryFragment.java
            case 2:
                return new ReservationsFragment(); // Gọi file ReservationsFragment.java
            default:
                return new BorrowingFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }

}