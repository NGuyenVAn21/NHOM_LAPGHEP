package com.example.bookhub;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookhub.api.ApiService;
import com.example.bookhub.api.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReadingActivity extends AppCompatActivity {

    private TextView tvChapterTitle, tvContent, tvScreenTitle;
    private Button btnPrevChapter, btnNextChapter;
    private ImageButton btnBookmark, btnBack, btnSearch, btnNotification;

    private int currentChapterIndex = 0;
    private List<Chapter> chapters;
    private int bookId;

    private boolean isBookmarked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading);

        // Lấy ID sách từ Intent
        bookId = getIntent().getIntExtra("BOOK_ID", 1);

        initViews();
        setupClickListeners();

        // Gọi API lấy danh sách chương
        fetchChapters(bookId);

        // Ẩn action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
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
    }

    private void fetchChapters(int bookId) {
        ApiService apiService = RetrofitClient.getApiService();
        Call<List<Chapter>> call = apiService.getChapters(bookId);

        call.enqueue(new Callback<List<Chapter>>() {
            @Override
            public void onResponse(Call<List<Chapter>> call, Response<List<Chapter>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    chapters = response.body();
                    if (!chapters.isEmpty()) {
                        // Lấy chapterId của chương đầu tiên
                        int firstChapterId = chapters.get(0).getChapterId();
                        // Gọi API lấy nội dung chương đầu tiên
                        fetchChapterContent(firstChapterId);
                    } else {
                        Toast.makeText(ReadingActivity.this, "Sách chưa có nội dung", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ReadingActivity.this, "Không lấy được danh sách chương", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Chapter>> call, Throwable t) {
                Toast.makeText(ReadingActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchChapterContent(int chapterId) {
        ApiService apiService = RetrofitClient.getApiService();
        Call<Chapter> call = apiService.getChapterContent(chapterId);

        call.enqueue(new Callback<Chapter>() {
            @Override
            public void onResponse(Call<Chapter> call, Response<Chapter> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Chapter chapter = response.body();
                    updateChapterUI(chapter);
                } else {
                    Toast.makeText(ReadingActivity.this, "Không lấy được nội dung chương", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Chapter> call, Throwable t) {
                Toast.makeText(ReadingActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateChapterUI(Chapter chapter) {
        tvChapterTitle.setText(chapter.getTitle());
        tvContent.setText(chapter.getContent()); // QUAN TRỌNG: Hiển thị nội dung

        // Cập nhật trạng thái nút
        btnPrevChapter.setEnabled(currentChapterIndex > 0);
        btnNextChapter.setEnabled(chapters != null && currentChapterIndex < chapters.size() - 1);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnSearch.setOnClickListener(v ->
                Toast.makeText(this, "Tính năng tìm kiếm", Toast.LENGTH_SHORT).show());

        btnNotification.setOnClickListener(v ->
                Toast.makeText(this, "Thông báo", Toast.LENGTH_SHORT).show());

        btnPrevChapter.setOnClickListener(v -> {
            if (currentChapterIndex > 0 && chapters != null) {
                currentChapterIndex--;
                int chapterId = chapters.get(currentChapterIndex).getChapterId();
                fetchChapterContent(chapterId);
                Toast.makeText(ReadingActivity.this, "Đã chuyển đến chương trước", Toast.LENGTH_SHORT).show();
            }
        });

        btnNextChapter.setOnClickListener(v -> {
            if (chapters != null && currentChapterIndex < chapters.size() - 1) {
                currentChapterIndex++;
                int chapterId = chapters.get(currentChapterIndex).getChapterId();
                fetchChapterContent(chapterId);
                Toast.makeText(ReadingActivity.this, "Đã chuyển đến chương tiếp theo", Toast.LENGTH_SHORT).show();
            }
        });

        btnBookmark.setOnClickListener(v -> {
            isBookmarked = !isBookmarked;
            updateBookmarkButton();

            if (isBookmarked) {
                Toast.makeText(this, "Đã lưu trang sách", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Đã bỏ lưu trang sách", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateBookmarkButton() {
        if (isBookmarked) {
            btnBookmark.setImageResource(R.drawable.ic_bookmark);
            btnBookmark.animate().scaleX(1.2f).scaleY(1.2f).setDuration(200)
                    .withEndAction(() -> btnBookmark.animate().scaleX(1f).scaleY(1f).setDuration(200));
        } else {
            btnBookmark.setImageResource(R.drawable.ic_bookmark_border);
        }
    }
}