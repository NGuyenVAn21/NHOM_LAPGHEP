package com.example.bookhub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AccountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account);

        // Padding cho status bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        /* ---------------------------------------------------
         * 1. SỔ XUỐNG — THÔNG TIN CÁ NHÂN
         * --------------------------------------------------- */
        LinearLayout itemInfo = findViewById(R.id.itemInfo);
        LinearLayout subMenuInfo = findViewById(R.id.subMenuInfo);
        ImageView iconInfoArrow = findViewById(R.id.iconInfoArrow);

        itemInfo.setOnClickListener(v -> {
            if (subMenuInfo.getVisibility() == View.GONE) {
                subMenuInfo.setVisibility(View.VISIBLE);
                iconInfoArrow.animate().rotation(90).setDuration(150).start();
            } else {
                subMenuInfo.setVisibility(View.GONE);
                iconInfoArrow.animate().rotation(0).setDuration(150).start();
            }
        });

        // 3 mục con
        findViewById(R.id.subProfile).setOnClickListener(v ->
                startActivity(new Intent(AccountActivity.this, InfoActivity.class)));

        findViewById(R.id.subPassword).setOnClickListener(v ->
                Toast.makeText(AccountActivity.this, "Tính năng đổi mật khẩu", Toast.LENGTH_SHORT).show());

        findViewById(R.id.subNotification).setOnClickListener(v ->
                startActivity(new Intent(AccountActivity.this, NotificationSettingsActivity.class)));

        /* ---------------------------------------------------
         * 2. SỔ XUỐNG — LỊCH SỬ
         * --------------------------------------------------- */
        LinearLayout itemHistory = findViewById(R.id.itemHistory);
        LinearLayout subMenuHistory = findViewById(R.id.subMenuHistory);
        ImageView iconHistoryArrow = findViewById(R.id.iconHistoryArrow);

        itemHistory.setOnClickListener(v -> {
            if (subMenuHistory.getVisibility() == View.GONE) {
                subMenuHistory.setVisibility(View.VISIBLE);
                iconHistoryArrow.animate().rotation(90).setDuration(150).start();
            } else {
                subMenuHistory.setVisibility(View.GONE);
                iconHistoryArrow.animate().rotation(0).setDuration(150).start();
            }
        });

        findViewById(R.id.subReadHistory).setOnClickListener(v ->
                startActivity(new Intent(AccountActivity.this, ReadHistoryActivity.class)));

        findViewById(R.id.subBorrowHistory).setOnClickListener(v ->
                startActivity(new Intent(AccountActivity.this, BorrowHistoryActivity.class)));

        findViewById(R.id.subRatedBooks).setOnClickListener(v ->
                startActivity(new Intent(AccountActivity.this, RatedBooksActivity.class)));

        /* ---------------------------------------------------
         * 3. CÁC MỤC BÌNH THƯỜNG
         * --------------------------------------------------- */
        findViewById(R.id.itemFavorite).setOnClickListener(v ->
                startActivity(new Intent(AccountActivity.this, FavoriteActivity.class)));

        findViewById(R.id.itemSettings).setOnClickListener(v ->
                startActivity(new Intent(AccountActivity.this, SettingsActivity.class)));

        findViewById(R.id.itemHelp).setOnClickListener(v ->
                startActivity(new Intent(AccountActivity.this, HelpActivity.class)));

        findViewById(R.id.itemAbout).setOnClickListener(v ->
                startActivity(new Intent(AccountActivity.this, AboutActivity.class)));

        findViewById(R.id.itemLogout).setOnClickListener(v -> {
            Intent intent = new Intent(AccountActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            // 3. Bắt đầu chuyển trang
            startActivity(intent);
        });
        /* ---------------------------------------------------
         * 4. NÚT CHỈNH SỬA HỒ SƠ
         * --------------------------------------------------- */
        findViewById(R.id.btnEditProfile).setOnClickListener(v ->
                startActivity(new Intent(AccountActivity.this, EditProfileActivity.class)));
    }
}
