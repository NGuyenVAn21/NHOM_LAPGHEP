package com.example.bookhub.activity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.bookhub.R;

public class UserDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);

        // 1. Ánh xạ View
        ImageView imgAvatar = findViewById(R.id.img_profile_avatar);
        TextView tvName = findViewById(R.id.tv_profile_name);
        TextView tvRank = findViewById(R.id.tv_profile_rank);
        TextView tvBorrowCount = findViewById(R.id.tv_stat_borrow);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // 2. Nhận dữ liệu từ Intent
        String name = getIntent().getStringExtra("USER_NAME");
        String avatarUrl = getIntent().getStringExtra("USER_AVATAR");
        int borrowCount = getIntent().getIntExtra("USER_BORROW_COUNT", 0);
        int rank = getIntent().getIntExtra("USER_RANK", 0);

        // 3. Hiển thị
        tvName.setText(name);
        tvBorrowCount.setText(String.valueOf(borrowCount));

        // Xử lý Rank badge
        if (rank > 0) {
            tvRank.setText("Top " + rank + " Độc giả tích cực");
        } else {
            tvRank.setText("Thành viên tích cực");
        }

        // Load Avatar
        if (avatarUrl != null && !avatarUrl.startsWith("http")) {
            avatarUrl = "http://10.0.2.2:5177/images/" + avatarUrl; // Nhớ check cổng port
        }

        Glide.with(this)
                .load(avatarUrl)
                .transform(new CircleCrop())
                .placeholder(R.drawable.ic_user)
                .error(R.drawable.ic_user)
                .into(imgAvatar);
    }
}