package com.example.bookhub.activity;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.bookhub.R;
import com.example.bookhub.api.RetrofitClient;
import com.example.bookhub.models.CheckStatusResponse;
import com.example.bookhub.models.RegistrationRequest;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventDetailActivity extends AppCompatActivity {

    private int eventId = -1;
    private int currentUserId;
    private Button btnRegister;
    private boolean isRegistered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        // Lấy ID User
        SharedPreferences prefs = getSharedPreferences("BookHubPrefs", MODE_PRIVATE);
        currentUserId = prefs.getInt("CURRENT_USER_ID", -1);

        // Ánh xạ View
        TextView titleDetail = findViewById(R.id.event_title_detail);
        TextView descDetail = findViewById(R.id.event_description);
        TextView tvStart = findViewById(R.id.tv_start_date);
        TextView tvEnd = findViewById(R.id.tv_end_date);
        ImageView imgBanner = findViewById(R.id.img_detail_banner);
        btnRegister = findViewById(R.id.button_register);

        // Nhận dữ liệu Intent
        eventId = getIntent().getIntExtra("EVENT_ID", -1);
        String title = getIntent().getStringExtra("EVENT_TITLE");
        String desc = getIntent().getStringExtra("EVENT_DESC");
        String start = getIntent().getStringExtra("EVENT_START");
        String end = getIntent().getStringExtra("EVENT_END");
        String imageUrl = getIntent().getStringExtra("EVENT_IMAGE");

        // Hiển thị
        if (titleDetail != null) titleDetail.setText(title);
        if (descDetail != null) descDetail.setText((desc == null || desc.trim().isEmpty()) ? "Chưa có mô tả." : desc);
        if (tvStart != null) tvStart.setText(start);
        if (tvEnd != null) tvEnd.setText(end);
        if (imageUrl != null && imgBanner != null) Glide.with(this).load(imageUrl).into(imgBanner);

        // Check trạng thái
        if (eventId != -1 && currentUserId != -1) checkUserRegistrationStatus();

        // Xử lý Click
        btnRegister.setOnClickListener(v -> {
            if (isRegistered) {
                // TRƯỜNG HỢP HỦY: Hiện Dialog xác nhận trước
                showCancelConfirmationDialog();
            } else {
                // TRƯỜNG HỢP ĐĂNG KÝ: Gọi API luôn
                callRegisterApi();
            }
        });
    }

    // --- HỘP THOẠI XÁC NHẬN HỦY (MỚI) ---
    private void showCancelConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận hủy")
                .setMessage("Bạn có chắc chắn muốn hủy đăng ký tham gia sự kiện này không?")
                .setPositiveButton("Đồng ý hủy", (dialog, which) -> {
                    // Người dùng chọn Đồng ý -> Mới gọi API Hủy
                    callCancelApi();
                })
                .setNegativeButton("Giữ lại", null) // Không làm gì
                .show();
    }

    // API Hủy Đăng Ký
    private void callCancelApi() {
        btnRegister.setEnabled(false);
        btnRegister.setText("Đang xử lý...");

        RegistrationRequest request = new RegistrationRequest(currentUserId, eventId);
        RetrofitClient.getApiService().cancelEventRegistration(request).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                btnRegister.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(EventDetailActivity.this, "Đã hủy đăng ký thành công!", Toast.LENGTH_SHORT).show();
                    updateButtonState(false); // Đổi nút về màu xanh
                } else {
                    Toast.makeText(EventDetailActivity.this, "Hủy thất bại, vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                    updateButtonState(true); // Giữ nguyên trạng thái
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                btnRegister.setEnabled(true);
                updateButtonState(true);
                Toast.makeText(EventDetailActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // API Đăng Ký
    private void callRegisterApi() {
        btnRegister.setEnabled(false);
        btnRegister.setText("Đang xử lý...");

        RegistrationRequest request = new RegistrationRequest(currentUserId, eventId);
        RetrofitClient.getApiService().registerEvent(request).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                btnRegister.setEnabled(true);
                if (response.isSuccessful()) {
                    showSuccessDialog(); // Hiện Dialog thông báo thành công
                } else {
                    Toast.makeText(EventDetailActivity.this, "Đăng ký thất bại", Toast.LENGTH_SHORT).show();
                    updateButtonState(false);
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                btnRegister.setEnabled(true);
                updateButtonState(false);
                Toast.makeText(EventDetailActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Dialog thông báo Đăng ký thành công (Giữ nguyên cái đẹp cũ của bạn)
    private void showSuccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_success, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        view.findViewById(R.id.btn_dialog_ok).setOnClickListener(v -> {
            dialog.dismiss();
            updateButtonState(true); // Đổi nút sang màu đỏ (Hủy)
        });
        dialog.show();
    }

    // Helper: Cập nhật giao diện nút
    private void updateButtonState(boolean registered) {
        isRegistered = registered;
        if (isRegistered) {
            btnRegister.setText("Hủy đăng ký");
            btnRegister.setBackgroundColor(Color.parseColor("#D32F2F")); // Đỏ
        } else {
            btnRegister.setText("Đăng ký tham gia");
            btnRegister.setBackgroundColor(Color.parseColor("#4CAF50")); // Xanh
        }
    }

    // API Check trạng thái ban đầu
    private void checkUserRegistrationStatus() {
        RetrofitClient.getApiService().checkRegistrationStatus(currentUserId, eventId)
                .enqueue(new Callback<CheckStatusResponse>() {
                    @Override
                    public void onResponse(Call<CheckStatusResponse> call, Response<CheckStatusResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            updateButtonState(response.body().isRegistered());
                        }
                    }
                    @Override
                    public void onFailure(Call<CheckStatusResponse> call, Throwable t) {}
                });
    }
}