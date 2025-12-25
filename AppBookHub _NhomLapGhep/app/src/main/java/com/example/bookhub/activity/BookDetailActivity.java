package com.example.bookhub.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.bookhub.R;
import com.example.bookhub.api.RetrofitClient;
import com.example.bookhub.models.ActionResponse;
import com.example.bookhub.models.Book;
import com.example.bookhub.models.BorrowRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookDetailActivity extends AppCompatActivity {

    private ImageView favoriteButton;
    private Button borrowButton;
    private boolean isFavorite = false;
    private Book currentBook; // Lưu biến toàn cục để dùng khi bấm nút

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        currentBook = getIntent().getParcelableExtra("BOOK");
        if (currentBook != null) {
            setupBookDetails(currentBook);
        }
        setupClickListeners();
    }

    private void setupBookDetails(Book book) {
        ((TextView) findViewById(R.id.bookTitle)).setText(book.getTitle());
        ((TextView) findViewById(R.id.bookAuthor)).setText(book.getAuthor());
        ((TextView) findViewById(R.id.bookPrice)).setText(book.getPrice());
        ((TextView) findViewById(R.id.bookDescription)).setText(book.getDescription());

        // Rating
        ((TextView) findViewById(R.id.ratingText)).setText(String.format("%.1f (%d đánh giá)", book.getRating(), book.getReviews()));

        // Chi tiết
        ((TextView) findViewById(R.id.categoryValue)).setText(book.getCategory());
        ((TextView) findViewById(R.id.publisherValue)).setText(book.getPublisher());
        ((TextView) findViewById(R.id.yearValue)).setText(String.valueOf(book.getYear()));
        ((TextView) findViewById(R.id.pagesValue)).setText(book.getPages() + " trang");

        // Trạng thái
        TextView statusText = findViewById(R.id.statusValue);
        statusText.setText(book.getStatus());

        // Logic hiển thị màu và nút bấm
        boolean isAvailable = "Có sẵn".equals(book.getStatus());

        if (isAvailable) {
            statusText.setTextColor(ContextCompat.getColor(this, R.color.success_green));
        } else {
            statusText.setTextColor(ContextCompat.getColor(this, R.color.error_red));
        }

        setupActionButtons(isAvailable);
    }

    private void setupActionButtons(boolean isAvailable) {
        borrowButton = findViewById(R.id.borrowButton);
        Button cartButton = findViewById(R.id.cartButton);
        Button readButton = findViewById(R.id.readButton);

        // Chỉ hiện nút Mượn nếu sách có sẵn
        if (isAvailable) {
            borrowButton.setVisibility(View.VISIBLE);
            borrowButton.setEnabled(true); // Đảm bảo nút bấm được
            cartButton.setVisibility(View.VISIBLE);
            readButton.setText("Đọc thử");
        } else {
            borrowButton.setVisibility(View.GONE);
            cartButton.setVisibility(View.GONE);
            readButton.setText("Đọc online");
        }
    }

    private void setupClickListeners() {
        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        favoriteButton = findViewById(R.id.favoriteButton);
        favoriteButton.setOnClickListener(v -> toggleFavorite());

        // --- XỬ LÝ SỰ KIỆN MƯỢN SÁCH ---
        if (borrowButton != null) {
            borrowButton.setOnClickListener(v -> {
                performBorrowBook();
            });
        }

        findViewById(R.id.cartButton).setOnClickListener(v -> showToast("Đã thêm vào giỏ hàng"));

        findViewById(R.id.readButton).setOnClickListener(v -> {
            Intent intent = new Intent(BookDetailActivity.this, ReadingActivity.class);
            startActivity(intent);
        });
    }

    // Hàm gọi API Mượn sách
    private void performBorrowBook() {
        if (currentBook == null) return;

        // 1. Lấy UserID từ SharedPreferences
        SharedPreferences prefs = getSharedPreferences("BookHubPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("CURRENT_USER_ID", -1);

        if (userId == -1) {
            showToast("Vui lòng đăng nhập lại để mượn sách");
            return;
        }

        // Khóa nút để tránh bấm nhiều lần
        borrowButton.setEnabled(false);
        borrowButton.setText("Đang xử lý...");

        // 2. Tạo Request
        BorrowRequest request = new BorrowRequest(userId, currentBook.getId());

        // 3. Gọi API
        RetrofitClient.getApiService().borrowBook(request).enqueue(new Callback<ActionResponse>() {
            @Override
            public void onResponse(Call<ActionResponse> call, Response<ActionResponse> response) {
                borrowButton.setEnabled(true);
                borrowButton.setText("Mượn sách");

                if (response.isSuccessful() && response.body() != null) {
                    ActionResponse result = response.body();
                    if (result.isSuccess()) {
                        showToast("Thành công: " + result.getMessage());

                        // Cập nhật lại trạng thái sách trên giao diện nếu cần
                        // Hoặc chuyển hướng sang màn hình Quản lý mượn
                        // Intent intent = new Intent(BookDetailActivity.this, BorrowHistoryActivity.class);
                        // startActivity(intent);
                    } else {
                        showToast("Thất bại: " + result.getMessage());
                    }
                } else {
                    showToast("Lỗi server hoặc sách đã hết hàng");
                }
            }

            @Override
            public void onFailure(Call<ActionResponse> call, Throwable t) {
                borrowButton.setEnabled(true);
                borrowButton.setText("Mượn sách");
                showToast("Lỗi kết nối mạng: " + t.getMessage());
            }
        });
    }

    private void toggleFavorite() {
        isFavorite = !isFavorite;
        if (isFavorite) {
            favoriteButton.setImageResource(R.drawable.ic_favorite);
            favoriteButton.setColorFilter(ContextCompat.getColor(this, R.color.error_red));
            showToast("Đã thêm vào danh sách yêu thích");
        } else {
            favoriteButton.setImageResource(R.drawable.ic_favorite_border);
            favoriteButton.setColorFilter(ContextCompat.getColor(this, R.color.black));
            showToast("Đã xóa khỏi danh sách yêu thích");
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}