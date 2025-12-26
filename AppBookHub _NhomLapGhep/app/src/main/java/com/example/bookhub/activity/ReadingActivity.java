package com.example.bookhub.activity;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat; // Thêm thư viện này

import com.example.bookhub.R;
import com.example.bookhub.api.RetrofitClient;
import com.example.bookhub.models.Chapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReadingActivity extends AppCompatActivity {

    private TextView tvChapterTitle, tvContent, tvScreenTitle;
    private Button btnPrevChapter, btnNextChapter;
    private ImageButton btnBookmark, btnBack, btnSearch, btnNotification;
    private ScrollView scrollView; // Cần cái này để đổi màu nền

    // Dữ liệu động
    private List<Chapter> chapterList = new ArrayList<>();
    private int currentChapterIndex = 0;
    private int bookId = -1;
    private boolean isBookmarked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading);

        bookId = getIntent().getIntExtra("BOOK_ID", -1);
        String bookTitle = getIntent().getStringExtra("BOOK_TITLE");

        initViews();

        if (bookTitle != null) {
            tvScreenTitle.setText(bookTitle);
        }

        setupClickListeners();

        // Áp dụng cài đặt ngay khi mở
        applySettings();

        if (bookId != -1) {
            fetchBookChapters();
        } else {
            showToast("Lỗi: Không tìm thấy sách!");
            finish();
        }
    }

    // --- QUAN TRỌNG: Gọi hàm này mỗi khi màn hình hiện lên ---
    @Override
    protected void onResume() {
        super.onResume();
        applySettings();
    }

    // --- HÀM XỬ LÝ CÀI ĐẶT ---
    private void applySettings() {
        SharedPreferences prefs = getSharedPreferences("BookHubPrefs", MODE_PRIVATE);

        // 1. CẬP NHẬT CỠ CHỮ
        // Lấy cỡ chữ đã lưu, mặc định là 18 nếu chưa chỉnh
        int fontSize = prefs.getInt("FONT_SIZE", 18);
        if (tvContent != null) {
            tvContent.setTextSize(fontSize);
        }

        // 2. CẬP NHẬT MÀU NỀN (DARK MODE)
        // Vì file XML bạn đang set cứng background="@color/white", nên ta phải đổi bằng code
        boolean isDarkMode = prefs.getBoolean("DARK_MODE", false);

        if (isDarkMode) {
            // Chế độ Tối: Nền đen, Chữ trắng xám
            if (scrollView != null) scrollView.setBackgroundColor(Color.parseColor("#121212"));
            if (tvContent != null) tvContent.setTextColor(Color.parseColor("#E0E0E0"));
            if (tvChapterTitle != null) tvChapterTitle.setTextColor(Color.WHITE);
        } else {
            // Chế độ Sáng: Nền trắng, Chữ đen xám
            if (scrollView != null) scrollView.setBackgroundColor(Color.WHITE);
            if (tvContent != null) tvContent.setTextColor(Color.parseColor("#333333"));
            if (tvChapterTitle != null) tvChapterTitle.setTextColor(Color.BLACK);
        }
    }

    private void initViews() {
        tvScreenTitle = findViewById(R.id.tvScreenTitle);
        tvChapterTitle = findViewById(R.id.tvChapterTitle);
        tvContent = findViewById(R.id.tvContent);
        btnPrevChapter = findViewById(R.id.btnPrevChapter);
        btnNextChapter = findViewById(R.id.btnNextChapter);
        btnBookmark = findViewById(R.id.btnBookmark);
        btnBack = findViewById(R.id.btnBack);
        btnSearch = findViewById(R.id.btnSearch);
        btnNotification = findViewById(R.id.btnNotification);

        // Ánh xạ ScrollView (Nhớ thêm ID này vào file XML nếu chưa có)
        scrollView = findViewById(R.id.scrollView);

        btnPrevChapter.setEnabled(false);
        btnNextChapter.setEnabled(false);
        tvContent.setText("Đang tải nội dung sách...");
    }

    // ... (Các hàm fetchBookChapters, displayChapter, setupClickListeners giữ nguyên như cũ) ...

    private void fetchBookChapters() {
        RetrofitClient.getApiService().getBookChapters(bookId).enqueue(new Callback<List<Chapter>>() {
            @Override
            public void onResponse(Call<List<Chapter>> call, Response<List<Chapter>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    chapterList = response.body();
                    currentChapterIndex = 0;
                    displayChapter();
                } else {
                    tvContent.setText("Cuốn sách này chưa có nội dung số hóa.");
                    tvChapterTitle.setText("Chưa có dữ liệu");
                }
            }

            @Override
            public void onFailure(Call<List<Chapter>> call, Throwable t) {
                tvContent.setText("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void displayChapter() {
        if (chapterList.isEmpty()) return;
        Chapter currentChapter = chapterList.get(currentChapterIndex);
        tvChapterTitle.setText(currentChapter.getTitle());
        tvContent.setText(currentChapter.getContent());
        if (scrollView != null) scrollView.scrollTo(0, 0);
        btnPrevChapter.setEnabled(currentChapterIndex > 0);
        btnPrevChapter.setAlpha(currentChapterIndex > 0 ? 1.0f : 0.5f);
        btnNextChapter.setEnabled(currentChapterIndex < chapterList.size() - 1);
        btnNextChapter.setAlpha(currentChapterIndex < chapterList.size() - 1 ? 1.0f : 0.5f);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnBookmark.setOnClickListener(v -> {
            isBookmarked = !isBookmarked;
            updateBookmarkButton();
            showToast(isBookmarked ? "Đã lưu trang sách" : "Đã bỏ lưu");
        });
        btnPrevChapter.setOnClickListener(v -> {
            if (currentChapterIndex > 0) {
                currentChapterIndex--;
                displayChapter();
            }
        });
        btnNextChapter.setOnClickListener(v -> {
            if (currentChapterIndex < chapterList.size() - 1) {
                currentChapterIndex++;
                displayChapter();
            }
        });
    }

    private void updateBookmarkButton() {
        if (isBookmarked) {
            btnBookmark.setImageResource(R.drawable.ic_bookmark);
        } else {
            btnBookmark.setImageResource(R.drawable.ic_bookmark_border);
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}