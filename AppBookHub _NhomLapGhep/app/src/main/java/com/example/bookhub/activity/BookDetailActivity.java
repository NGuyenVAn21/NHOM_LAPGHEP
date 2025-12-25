package com.example.bookhub.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
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

        currentBook = getIntent().getParcelableExtra("BOOK");
        String extraImage = getIntent().getStringExtra("BOOK_IMAGE");

        initViews();

        if (currentBook != null) {
            setupBookDetails(currentBook, extraImage);
        }

        setupClickListeners();
    }

    private void initViews() {
        favoriteButton = findViewById(R.id.favoriteButton);
        borrowButton = findViewById(R.id.borrowButton);
        cartButton = findViewById(R.id.cartButton);
        readButton = findViewById(R.id.readButton);
        bookCover = findViewById(R.id.bookImage);
    }

    private void setupBookDetails(Book book, String extraImage) {
        ((TextView) findViewById(R.id.bookTitle)).setText(book.getTitle());
        ((TextView) findViewById(R.id.bookAuthor)).setText(book.getAuthor());
        ((TextView) findViewById(R.id.bookPrice)).setText(book.getPrice());
        ((TextView) findViewById(R.id.bookDescription)).setText(book.getDescription());
        ((TextView) findViewById(R.id.ratingText)).setText(String.format("%.1f (%d đánh giá)", book.getRating(), book.getReviews()));
        ((TextView) findViewById(R.id.categoryValue)).setText(book.getCategory());
        ((TextView) findViewById(R.id.publisherValue)).setText(book.getPublisher());
        ((TextView) findViewById(R.id.yearValue)).setText(String.valueOf(book.getYear()));
        ((TextView) findViewById(R.id.pagesValue)).setText(book.getPages() + " trang");

        String imgUrl = (extraImage != null) ? extraImage : book.getImageUrl();
        if (imgUrl != null && !imgUrl.startsWith("http")) {
            imgUrl = "http://10.0.2.2:5280/images/" + imgUrl;
        }
        if (bookCover != null) {
            Glide.with(this).load(imgUrl).placeholder(R.drawable.ic_menu_book_round).into(bookCover);
        }

        // LOGIC HIỂN THỊ TRẠNG THÁI
        TextView statusText = findViewById(R.id.statusValue);
        // Kiểm tra status từ Server hoặc từ logic
        String status = book.getStatus();
        // Nếu trong DB là 'Có sẵn' hoặc 'Available'
        boolean isAvailable = "Có sẵn".equals(status) || "Available".equals(status);

        statusText.setText(status);
        if (isAvailable) {
            statusText.setTextColor(ContextCompat.getColor(this, R.color.success_green));
        } else {
            statusText.setTextColor(ContextCompat.getColor(this, R.color.error_red));
        }

        setupActionButtons(isAvailable);
    }

    // --- LOGIC ĐỔI NÚT MƯỢN / ĐẶT TRƯỚC ---
    private void setupActionButtons(boolean isAvailable) {
        if (isAvailable) {
            // SÁCH CÓ SẴN
            borrowButton.setText("Mượn sách");
            borrowButton.setBackgroundColor(getColor(R.color.purple_500));
            borrowButton.setVisibility(View.VISIBLE);
            cartButton.setVisibility(View.VISIBLE);

            borrowButton.setOnClickListener(v -> performBorrowBook());
        } else {
            // SÁCH HẾT HÀNG -> HIỆN NÚT ĐẶT TRƯỚC
            borrowButton.setText("Đặt trước");
            borrowButton.setBackgroundColor(Color.parseColor("#FF9800")); // Màu Cam
            borrowButton.setVisibility(View.VISIBLE);
            cartButton.setVisibility(View.GONE); // Hết hàng thì ẩn giỏ

            borrowButton.setOnClickListener(v -> performReserveBook());
        }
    }

    private void setupClickListeners() {
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        favoriteButton.setOnClickListener(v -> toggleFavorite());
        cartButton.setOnClickListener(v -> showToast("Đã thêm vào giỏ hàng"));
        readButton.setOnClickListener(v -> {
            Intent intent = new Intent(BookDetailActivity.this, ReadingActivity.class);
            startActivity(intent);
        });
    }

    // --- HÀM 1: MƯỢN SÁCH ---
    private void performBorrowBook() {
        if (currentBook == null) return;
        SharedPreferences prefs = getSharedPreferences("BookHubPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("CURRENT_USER_ID", -1);

        if (userId == -1) { showToast("Vui lòng đăng nhập lại!"); return; }

        borrowButton.setEnabled(false);
        borrowButton.setText("Đang xử lý...");

        BorrowRequest request = new BorrowRequest(userId, currentBook.getId());

        RetrofitClient.getApiService().borrowBook(request).enqueue(new Callback<ActionResponse>() {
            @Override
            public void onResponse(Call<ActionResponse> call, Response<ActionResponse> response) {
                borrowButton.setEnabled(true);
                borrowButton.setText("Mượn sách");
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isSuccess()) {
                        showToast("Mượn thành công!");
                    } else {
                        showToast(response.body().getMessage());
                    }
                } else {
                    handleError(response);
                }
            }
            @Override
            public void onFailure(Call<ActionResponse> call, Throwable t) {
                borrowButton.setEnabled(true);
                showToast("Lỗi kết nối");
            }
        });
    }

    // --- HÀM 2: ĐẶT TRƯỚC (Dành cho sách hết hàng) ---
    private void performReserveBook() {
        if (currentBook == null) return;
        SharedPreferences prefs = getSharedPreferences("BookHubPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("CURRENT_USER_ID", -1);

        if (userId == -1) { showToast("Vui lòng đăng nhập lại!"); return; }

        borrowButton.setEnabled(false);
        borrowButton.setText("Đang xếp hàng...");

        BorrowRequest request = new BorrowRequest(userId, currentBook.getId());

        RetrofitClient.getApiService().reserveBook(request).enqueue(new Callback<ActionResponse>() {
            @Override
            public void onResponse(Call<ActionResponse> call, Response<ActionResponse> response) {
                borrowButton.setEnabled(true);
                borrowButton.setText("Đặt trước");

                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isSuccess()) {
                        showToast(response.body().getMessage()); // "Đã vào danh sách chờ..."
                    } else {
                        showToast(response.body().getMessage());
                    }
                } else {
                    handleError(response);
                }
            }
            @Override
            public void onFailure(Call<ActionResponse> call, Throwable t) {
                borrowButton.setEnabled(true);
                showToast("Lỗi kết nối");
            }
        });
    }

    private void handleError(Response<?> response) {
        try {
            String errorBody = response.errorBody().string();
            JSONObject jsonObject = new JSONObject(errorBody);
            showToast(jsonObject.getString("message"));
        } catch (Exception e) {
            showToast("Lỗi server: " + response.code());
        }
    }

    private void toggleFavorite() {
        isFavorite = !isFavorite;
        favoriteButton.setImageResource(isFavorite ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);
        favoriteButton.setColorFilter(ContextCompat.getColor(this, isFavorite ? R.color.error_red : R.color.black));
        showToast(isFavorite ? "Đã thêm vào yêu thích" : "Đã xóa khỏi yêu thích");
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}