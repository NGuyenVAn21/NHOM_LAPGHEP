package com.example.bookhub;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.tong_nhomdtdd.Book;

public class BookDetailActivity extends AppCompatActivity {

    private ImageView favoriteButton;
    private boolean isFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        Book book = getIntent().getParcelableExtra("BOOK");
        if (book != null) {
            setupBookDetails(book);
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
        if ("Có sẵn".equals(book.getStatus())) {
            statusText.setTextColor(ContextCompat.getColor(this, R.color.success_green));
        } else {
            statusText.setTextColor(ContextCompat.getColor(this, R.color.error_red));
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
                showToast("Đã thêm vào giỏ hàng");
            }
        });

        findViewById(R.id.readButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast("Mở sách để đọc");
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