package com.example.bookhub.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bookhub.R;
import com.example.bookhub.api.RetrofitClient;
// Lưu ý: Nếu package bạn là 'models' thì đổi dòng dưới thành .models.ChangePasswordRequest
import com.example.bookhub.models.ChangePasswordRequest;
import com.example.bookhub.models.ProfileStatsResponse; // Model thống kê

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountActivity extends AppCompatActivity {

    // 1. Khai báo các biến View
    private TextView tvAccountName;
    private TextView tvStatBorrowed, tvStatRead, tvStatDays;

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

        // 2. Ánh xạ View (Kết nối Java với XML)
        tvAccountName = findViewById(R.id.txtName);       // Tên người dùng
        tvStatBorrowed = findViewById(R.id.tvStatBorrowed); // Số sách mượn
        tvStatRead = findViewById(R.id.tvStatRead);         // Số sách đã đọc
        tvStatDays = findViewById(R.id.tvStatDays);         // Số ngày đọc

        updateUserInfo();
        fetchProfileStats();

        setupExpandableMenus();

        View itemMyEvents = findViewById(R.id.itemMyEvents);
        if (itemMyEvents != null) {
            itemMyEvents.setOnClickListener(v ->
                    startActivity(new Intent(AccountActivity.this, MyEventsActivity.class))
            );
        }

        setupActionButtons();

        findViewById(R.id.subPassword).setOnClickListener(v -> showChangePasswordDialog());

        findViewById(R.id.itemLogout).setOnClickListener(v -> performLogout());

        findViewById(R.id.btnEditProfile).setOnClickListener(v ->
                startActivity(new Intent(AccountActivity.this, EditProfileActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Cập nhật lại thông tin khi quay lại (VD: vừa đổi tên xong)
        updateUserInfo();
    }

    private void updateUserInfo() {
        if (tvAccountName != null) {
            SharedPreferences prefs = getSharedPreferences("BookHubPrefs", MODE_PRIVATE);
            String name = prefs.getString("CURRENT_USER_NAME", "Người dùng");
            tvAccountName.setText(name);
        }
    }

    private void fetchProfileStats() {
        SharedPreferences prefs = getSharedPreferences("BookHubPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("CURRENT_USER_ID", -1);

        if (userId != -1) {
            RetrofitClient.getApiService().getProfileStats(userId).enqueue(new Callback<ProfileStatsResponse>() {
                @Override
                public void onResponse(Call<ProfileStatsResponse> call, Response<ProfileStatsResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        ProfileStatsResponse stats = response.body();
                        // Hiển thị số liệu lên màn hình
                        tvStatBorrowed.setText(String.valueOf(stats.getTotalBorrowed()));
                        tvStatRead.setText(String.valueOf(stats.getTotalRead()));
                        tvStatDays.setText(String.valueOf(stats.getReadingDays()));
                    }
                }

                @Override
                public void onFailure(Call<ProfileStatsResponse> call, Throwable t) {
                    // Nếu lỗi thì giữ nguyên số 0 hoặc log lỗi
                }
            });
        }
    }

    private void setupExpandableMenus() {
        // Menu Thông tin
        LinearLayout itemInfo = findViewById(R.id.itemInfo);
        LinearLayout subMenuInfo = findViewById(R.id.subMenuInfo);
        ImageView iconInfoArrow = findViewById(R.id.iconInfoArrow);

        itemInfo.setOnClickListener(v -> {
            boolean isVisible = subMenuInfo.getVisibility() == View.VISIBLE;
            subMenuInfo.setVisibility(isVisible ? View.GONE : View.VISIBLE);
            iconInfoArrow.animate().rotation(isVisible ? 0 : 90).setDuration(150).start();
        });

        // Menu Lịch sử
        LinearLayout itemHistory = findViewById(R.id.itemHistory);
        LinearLayout subMenuHistory = findViewById(R.id.subMenuHistory);
        ImageView iconHistoryArrow = findViewById(R.id.iconHistoryArrow);

        itemHistory.setOnClickListener(v -> {
            boolean isVisible = subMenuHistory.getVisibility() == View.VISIBLE;
            subMenuHistory.setVisibility(isVisible ? View.GONE : View.VISIBLE);
            iconHistoryArrow.animate().rotation(isVisible ? 0 : 90).setDuration(150).start();
        });
    }

    private void setupActionButtons() {
        // Menu con của Thông tin
        findViewById(R.id.subProfile).setOnClickListener(v -> startActivity(new Intent(this, InfoActivity.class)));
        findViewById(R.id.subNotification).setOnClickListener(v -> startActivity(new Intent(this, NotificationSettingsActivity.class)));

        // Menu con của Lịch sử
        findViewById(R.id.subReadHistory).setOnClickListener(v -> startActivity(new Intent(this, ReadHistoryActivity.class)));
        findViewById(R.id.subBorrowHistory).setOnClickListener(v -> startActivity(new Intent(this, BorrowHistoryActivity.class)));
        findViewById(R.id.subRatedBooks).setOnClickListener(v -> startActivity(new Intent(this, RatedBooksActivity.class)));

        // Các mục menu chính khác
        findViewById(R.id.itemFavorite).setOnClickListener(v -> startActivity(new Intent(this, FavoriteActivity.class)));
        findViewById(R.id.itemSettings).setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        findViewById(R.id.itemHelp).setOnClickListener(v -> startActivity(new Intent(this, HelpActivity.class)));
        findViewById(R.id.itemAbout).setOnClickListener(v -> startActivity(new Intent(this, AboutActivity.class)));
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_change_password, null);

        EditText etOldPass = view.findViewById(R.id.et_old_pass);
        EditText etNewPass = view.findViewById(R.id.et_new_pass);

        builder.setView(view)
                .setTitle("Đổi mật khẩu")
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String oldP = etOldPass.getText().toString();
                    String newP = etNewPass.getText().toString();
                    changePasswordApi(oldP, newP);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void changePasswordApi(String oldP, String newP) {
        SharedPreferences prefs = getSharedPreferences("BookHubPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("CURRENT_USER_ID", -1);

        ChangePasswordRequest req = new ChangePasswordRequest(userId, oldP, newP);

        RetrofitClient.getApiService().changePassword(req).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AccountActivity.this, "Đổi mật khẩu thành công! Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
                    performLogout(); // Logout luôn cho an toàn
                } else {
                    Toast.makeText(AccountActivity.this, "Mật khẩu cũ không đúng!", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(AccountActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performLogout() {
        // 1. Xóa dữ liệu trong bộ nhớ
        SharedPreferences prefs = getSharedPreferences("BookHubPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear(); // Xóa sạch token, tên, id...
        editor.apply();

        // 2. Chuyển về màn hình Đăng nhập
        Intent intent = new Intent(AccountActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}