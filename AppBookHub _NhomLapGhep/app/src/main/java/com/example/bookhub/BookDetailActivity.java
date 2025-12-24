package com.example.bookhub;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.bookhub.api.ApiService;
import com.example.bookhub.api.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.bumptech.glide.Glide;

public class BookDetailActivity extends AppCompatActivity {

    private ImageView favoriteButton;
    private boolean isFavorite = false;
    private int bookId;
    private Book currentBook; // Thêm biến để lưu trữ thông tin sách

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        // Lấy ID sách từ Intent (thay vì cả object)
        bookId = getIntent().getIntExtra("BOOK_ID", 1);

        // Gọi API lấy chi tiết sách
        fetchBookDetails(bookId);

        setupClickListeners();
    }

    private void fetchBookDetails(int bookId) {
        ApiService apiService = RetrofitClient.getApiService();
        apiService.getBookById(bookId).enqueue(new Callback<Book>() {
            @Override
            public void onResponse(Call<Book> call, Response<Book> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentBook = response.body(); // Lưu sách vào biến currentBook
                    setupBookDetails(currentBook);
                } else {
                    Toast.makeText(BookDetailActivity.this, "Không lấy được thông tin sách", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Book> call, Throwable t) {
                Toast.makeText(BookDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupBookDetails(Book book) {
        // Lưu sách vào biến currentBook để sử dụng sau
        currentBook = book;

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
        if ("Có sẵn".equals(book.getStatus())) {
            statusText.setTextColor(ContextCompat.getColor(this, R.color.success_green));
        } else {
            statusText.setTextColor(ContextCompat.getColor(this, R.color.error_red));
        }

        // LOAD ẢNH SÁCH
        ImageView bookCoverImage = findViewById(R.id.bookCoverImage);
        if (book.getImageUrl() != null && !book.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(book.getImageUrl())
                    .placeholder(R.drawable.gradient_book_cover)
                    .error(R.drawable.gradient_book_cover)
                    .into(bookCoverImage);
        } else {
            bookCoverImage.setImageResource(R.drawable.gradient_book_cover);
        }

        // Thiết lập nút hành động dựa trên trạng thái
        setupActionButtons("Có sẵn".equals(book.getStatus()));
    }

    private void setupActionButtons(boolean isAvailable) {
        Button borrowButton = findViewById(R.id.borrowButton);
        Button cartButton = findViewById(R.id.cartButton);
        Button readButton = findViewById(R.id.readButton);

        if (isAvailable) {
            borrowButton.setVisibility(View.VISIBLE);
            cartButton.setVisibility(View.VISIBLE);
            readButton.setText("Đọc online");
        } else {
            borrowButton.setVisibility(View.GONE);
            cartButton.setVisibility(View.GONE);
            readButton.setText("Đọc online");
        }
    }

    private void setupClickListeners() {
        findViewById(R.id.backButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        favoriteButton = findViewById(R.id.favoriteButton);
        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFavorite();
            }
        });

        findViewById(R.id.borrowButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast("Đã thêm vào danh sách mượn");
            }
        });

        findViewById(R.id.cartButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Thêm sách vào giỏ sử dụng currentBook
                if (currentBook != null) {
                    CartManager cartManager = CartManager.getInstance(BookDetailActivity.this);
                    cartManager.addToCart(currentBook);

                    Toast.makeText(BookDetailActivity.this,
                            "Đã thêm " + currentBook.getTitle() + " vào giỏ sách",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(BookDetailActivity.this, "Lỗi: Không có thông tin sách", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Trong setupClickListeners():
        findViewById(R.id.readButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BookDetailActivity.this, ReadingActivity.class);
                intent.putExtra("BOOK_ID", bookId);
                startActivity(intent);
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