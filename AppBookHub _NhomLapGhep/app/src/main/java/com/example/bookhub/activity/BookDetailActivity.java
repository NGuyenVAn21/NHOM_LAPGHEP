package com.example.bookhub.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide; // Nhớ import Glide để load ảnh bìa
import com.example.bookhub.R;
import com.example.bookhub.api.RetrofitClient;
import com.example.bookhub.models.ActionResponse;
import com.example.bookhub.models.Book;
import com.example.bookhub.models.BorrowRequest;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookDetailActivity extends AppCompatActivity {

    private ImageView favoriteButton, bookCover;
    private Button borrowButton, cartButton, readButton;
    private boolean isFavorite = false;
    private Book currentBook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        // Nhận dữ liệu sách từ Intent
        currentBook = getIntent().getParcelableExtra("BOOK");

        // Nếu không có object Book, thử nhận từng trường riêng lẻ (phòng hờ)
        if (currentBook == null) {
            // Logic nhận dữ liệu thủ công nếu cần
            String title = getIntent().getStringExtra("BOOK_TITLE");
            if (title != null) {
                // Tạo object book tạm thời nếu cần
            }
        }

        initViews();
        if (currentBook != null) {
            setupBookDetails(currentBook);
        }
        setupClickListeners();
    }

    private void initViews() {
        favoriteButton = findViewById(R.id.favoriteButton);
        borrowButton = findViewById(R.id.borrowButton);
        cartButton = findViewById(R.id.cartButton);
        readButton = findViewById(R.id.readButton);
        bookCover = findViewById(R.id.bookImage); // Đảm bảo ID này đúng trong XML
    }

    private void setupBookDetails(Book book) {
        ((TextView) findViewById(R.id.bookTitle)).setText(book.getTitle());
        ((TextView) findViewById(R.id.bookAuthor)).setText(book.getAuthor());
        ((TextView) findViewById(R.id.bookPrice)).setText(book.getPrice());
        ((TextView) findViewById(R.id.bookDescription)).setText(book.getDescription());

        // Rating
        ((TextView) findViewById(R.id.ratingText)).setText(String.format("%.1f (%d đánh giá)", book.getRating(), book.getReviews()));

        // Thông tin chi tiết
        ((TextView) findViewById(R.id.categoryValue)).setText(book.getCategory());
        ((TextView) findViewById(R.id.publisherValue)).setText(book.getPublisher());
        ((TextView) findViewById(R.id.yearValue)).setText(String.valueOf(book.getYear()));
        ((TextView) findViewById(R.id.pagesValue)).setText(book.getPages() + " trang");

        // Load ảnh bìa
        String imgUrl = book.getImageUrl();
        if (imgUrl != null && !imgUrl.startsWith("http")) {
            imgUrl = "http://10.0.2.2:5280/images/" + imgUrl; // Thay port phù hợp
        }
        if (bookCover != null) {
            Glide.with(this).load(imgUrl).placeholder(R.drawable.ic_menu_book_round).into(bookCover);
        }

        // Trạng thái kho
        TextView statusText = findViewById(R.id.statusValue);
        statusText.setText(book.getStatus());

        boolean isAvailable = "Có sẵn".equals(book.getStatus());
        if (isAvailable) {
            statusText.setTextColor(ContextCompat.getColor(this, R.color.success_green));
        } else {
            statusText.setTextColor(ContextCompat.getColor(this, R.color.error_red));
        }

        // Ẩn hiện nút mượn
        setupActionButtons(isAvailable);
    }

    private void setupActionButtons(boolean isAvailable) {
        if (isAvailable) {
            borrowButton.setVisibility(View.VISIBLE);
            borrowButton.setEnabled(true);
            borrowButton.setBackgroundColor(getColor(R.color.purple_500)); // Màu chủ đạo
            cartButton.setVisibility(View.VISIBLE);
            readButton.setText("Đọc thử");
        } else {
            borrowButton.setVisibility(View.GONE); // Hoặc hiện nhưng disable
            cartButton.setVisibility(View.GONE);
            readButton.setText("Đọc online");
        }
    }

    private void setupClickListeners() {
        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        favoriteButton.setOnClickListener(v -> toggleFavorite());

        borrowButton.setOnClickListener(v -> performBorrowBook());

        cartButton.setOnClickListener(v -> showToast("Đã thêm vào giỏ hàng"));

        readButton.setOnClickListener(v -> {
            Intent intent = new Intent(BookDetailActivity.this, ReadingActivity.class);
            startActivity(intent);
        });
    }

    // --- LOGIC MƯỢN SÁCH QUAN TRỌNG ---
    private void performBorrowBook() {
        if (currentBook == null) return;

        SharedPreferences prefs = getSharedPreferences("BookHubPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("CURRENT_USER_ID", -1);

        if (userId == -1) {
            showToast("Vui lòng đăng nhập lại!");
            return;
        }

        // Disable nút để tránh bấm nhiều lần
        borrowButton.setEnabled(false);
        borrowButton.setText("Đang xử lý...");

        BorrowRequest request = new BorrowRequest(userId, currentBook.getId());

        RetrofitClient.getApiService().borrowBook(request).enqueue(new Callback<ActionResponse>() {
            @Override
            public void onResponse(Call<ActionResponse> call, Response<ActionResponse> response) {
                // Luôn bật lại nút dù thành công hay thất bại
                borrowButton.setEnabled(true);
                borrowButton.setText("Mượn sách");

                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isSuccess()) {
                        showToast("Mượn thành công! Kiểm tra tab 'Đang mượn'");
                        // Tùy chọn: Chuyển hướng sang màn hình quản lý mượn
                        // Intent intent = new Intent(BookDetailActivity.this, BorrowHistoryActivity.class);
                        // startActivity(intent);
                        // finish();
                    } else {
                        showToast(response.body().getMessage());
                    }
                } else {
                    // XỬ LÝ LỖI TỪ SERVER (VD: Đã mượn rồi, Hết hàng...)
                    try {
                        String errorBody = response.errorBody().string();
                        JSONObject jsonObject = new JSONObject(errorBody);
                        String message = jsonObject.getString("message");
                        showToast(message);
                    } catch (Exception e) {
                        showToast("Lỗi: " + response.code());
                    }
                }
            }

            @Override
            public void onFailure(Call<ActionResponse> call, Throwable t) {
                borrowButton.setEnabled(true);
                borrowButton.setText("Mượn sách");
                showToast("Lỗi mạng: " + t.getMessage());
            }
        });
    }

    private void toggleFavorite() {
        isFavorite = !isFavorite;
        if (isFavorite) {
            favoriteButton.setImageResource(R.drawable.ic_favorite);
            favoriteButton.setColorFilter(ContextCompat.getColor(this, R.color.error_red));
            showToast("Đã thêm vào yêu thích");
        } else {
            favoriteButton.setImageResource(R.drawable.ic_favorite_border);
            favoriteButton.setColorFilter(ContextCompat.getColor(this, R.color.black));
            showToast("Đã xóa khỏi yêu thích");
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}