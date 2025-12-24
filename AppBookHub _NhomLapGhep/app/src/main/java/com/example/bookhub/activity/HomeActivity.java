package com.example.bookhub.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bookhub.R;
import com.example.bookhub.adapter.BookAdapter;
import com.example.bookhub.adapter.HomeAdapter; // Dùng Adapter mới
import com.example.bookhub.api.RetrofitClient;
import com.example.bookhub.model.Book;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerNewBooks, recyclerPopularBooks;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // 1. Ánh xạ View
        recyclerNewBooks = findViewById(R.id.recycler_new_books);
        recyclerPopularBooks = findViewById(R.id.recycler_popular_books); // Ánh xạ thêm cái này
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);

        // 2. Cài đặt RecyclerView nằm NGANG (Horizontal)
        recyclerNewBooks.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerPopularBooks.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // 3. Gọi API lấy sách
        fetchBooks();

        // 4. Cài đặt Footer (Bottom Navigation)
        setupBottomNavigation();

        // Sự kiện xem tất cả (giữ nguyên)
        View seeAllEvents = findViewById(R.id.text_see_all_events);
        if (seeAllEvents != null) {
            seeAllEvents.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, EventsActivity.class)));
        }
    }

    // Trong HomeActivity.java

    private void fetchBooks() {
        RetrofitClient.getApiService().getAllBooks().enqueue(new Callback<List<Book>>() {
            @Override
            public void onResponse(Call<List<Book>> call, Response<List<Book>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Book> bookList = response.body();

                    // --- Adapter cho Sách Mới ---
                    HomeAdapter adapterNew = new HomeAdapter(bookList, book -> openBookDetail(book));
                    recyclerNewBooks.setAdapter(adapterNew);

                    // --- Adapter cho Sách Phổ Biến (Tạm thời dùng chung list để hiển thị) ---
                    // Trong thực tế bạn có thể đảo ngược list hoặc filter theo rating
                    HomeAdapter adapterPopular = new HomeAdapter(bookList, book -> openBookDetail(book));
                    recyclerPopularBooks.setAdapter(adapterPopular);
                }
            }

            @Override
            public void onFailure(Call<List<Book>> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Lỗi API: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hàm mở chi tiết sách (Viết riêng cho gọn)
    private void openBookDetail(Book book) {
        Intent intent = new Intent(HomeActivity.this, BookDetailActivity.class);
        intent.putExtra("BOOK_TITLE", book.getTitle());
        intent.putExtra("BOOK_AUTHOR", book.getAuthor());
        intent.putExtra("BOOK_DESC", book.getDescription());
        intent.putExtra("BOOK_IMAGE", book.getImageUrl());
        intent.putExtra("BOOK_PRICE", book.getPrice());
        startActivity(intent);
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.navigation_home) {
                    return true;
                } else if (itemId == R.id.navigation_books) {
                    startActivity(new Intent(HomeActivity.this, BookSearchActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (itemId == R.id.navigation_borrow) {
                    startActivity(new Intent(HomeActivity.this, BorrowHistoryActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (itemId == R.id.navigation_account) {
                    startActivity(new Intent(HomeActivity.this, AccountActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                }
                return false;
            }
        });
    }
}