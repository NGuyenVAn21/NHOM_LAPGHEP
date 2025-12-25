package com.example.bookhub.activity;

import android.app.AlertDialog;
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
import com.example.bookhub.models.CheckStatusResponse; // Import mới
import com.example.bookhub.models.RegistrationRequest;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventDetailActivity extends AppCompatActivity {

    private int eventId = -1;
    private int currentUserId = 1;
    private Button btnRegister;
    private boolean isRegistered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        // Ánh xạ View
        TextView titleDetail = findViewById(R.id.event_title_detail);
        TextView descDetail = findViewById(R.id.event_description);
        TextView tvStart = findViewById(R.id.tv_start_date);
        TextView tvEnd = findViewById(R.id.tv_end_date);
        ImageView imgBanner = findViewById(R.id.img_detail_banner);
        btnRegister = findViewById(R.id.button_register);

        // Nhận dữ liệu
        eventId = getIntent().getIntExtra("EVENT_ID", -1);
        String title = getIntent().getStringExtra("EVENT_TITLE");
        String desc = getIntent().getStringExtra("EVENT_DESC"); // <-- Kiểm tra cái này
        String start = getIntent().getStringExtra("EVENT_START");
        String end = getIntent().getStringExtra("EVENT_END");
        String imageUrl = getIntent().getStringExtra("EVENT_IMAGE");

        // Hiển thị
        if (titleDetail != null) titleDetail.setText(title);

        // Fix lỗi hiển thị thông tin trống: Nếu desc null hoặc rỗng thì hiện thông báo mặc định
        if (descDetail != null) {
            if (desc == null || desc.trim().isEmpty()) {
                descDetail.setText("Chưa có mô tả chi tiết cho sự kiện này.");
            } else {
                descDetail.setText(desc);
            }
        }

        if (tvStart != null) tvStart.setText(start);
        if (tvEnd != null) tvEnd.setText(end);
        if (imageUrl != null && imgBanner != null) {
            Glide.with(this).load(imageUrl).into(imgBanner);
        }

        // --- GỌI HÀM CHECK TRẠNG THÁI NGAY KHI VÀO ---
        if (eventId != -1) {
            checkUserRegistrationStatus();
        }

        // Xử lý Click
        btnRegister.setOnClickListener(v -> {
            if (isRegistered) {
                Toast.makeText(this, "Bạn đã đăng ký sự kiện này rồi!", Toast.LENGTH_SHORT).show();
            } else {
                callRegisterApi(title);
            }
        });
    }

    // Hàm kiểm tra trạng thái từ Server
    private void checkUserRegistrationStatus() {
        RetrofitClient.getApiService().checkRegistrationStatus(currentUserId, eventId)
                .enqueue(new Callback<CheckStatusResponse>() {
                    @Override
                    public void onResponse(Call<CheckStatusResponse> call, Response<CheckStatusResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            if (response.body().isRegistered()) {
                                // Nếu Server bảo đã đăng ký -> Đổi giao diện ngay
                                setRegisteredState();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<CheckStatusResponse> call, Throwable t) {
                        // Kệ nó, nếu lỗi mạng thì cứ để nút Đăng ký hiện
                    }
                });
    }

    private void callRegisterApi(String eventTitle) {
        btnRegister.setEnabled(false);
        btnRegister.setText("Đang xử lý...");

        RegistrationRequest request = new RegistrationRequest(currentUserId, eventId);
        RetrofitClient.getApiService().registerEvent(request).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                btnRegister.setEnabled(true);
                if (response.isSuccessful()) {
                    showSuccessDialog(btnRegister);
                } else {
                    // Xử lý lỗi như cũ...
                    btnRegister.setText("Đăng ký tham gia ngay");
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                btnRegister.setEnabled(true);
                btnRegister.setText("Đăng ký tham gia ngay");
                Toast.makeText(EventDetailActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showSuccessDialog(Button btnRegister) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_success, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        view.findViewById(R.id.btn_dialog_ok).setOnClickListener(v -> {
            dialog.dismiss();
            setRegisteredState();
        });
        dialog.show();
    }

    // Hàm đổi giao diện nút
    private void setRegisteredState() {
        btnRegister.setText("Đã Đăng Ký ✓");
        btnRegister.setBackgroundColor(Color.GRAY);
        btnRegister.setEnabled(false); // Không cho bấm nữa
        isRegistered = true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}