package com.example.bookhub.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bookhub.R;
import com.example.bookhub.adapter.EventHomeAdapter;
import com.example.bookhub.adapter.HomeAdapter; // Dùng Adapter mới
import com.example.bookhub.adapter.UserRankAdapter;
import com.example.bookhub.api.RetrofitClient;
import com.example.bookhub.models.Book;
import com.example.bookhub.models.Event;
import com.example.bookhub.models.UserRank;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.example.bookhub.models.UserStatsResponse;

public class HomeActivity extends AppCompatActivity {
    private RecyclerView recyclerActiveReaders;
    private RecyclerView recyclerNewBooks, recyclerPopularBooks;
    private BottomNavigationView bottomNavigationView;
    private TextView tvBorrowCount, tvDueSoonCount;
    private int currentUserId;
    private TextView tvUsername;
    private EditText etSearch;
    private LinearLayout btnBorrow, btnReturn, btnRead;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // 1. Ánh xạ View
        recyclerActiveReaders = findViewById(R.id.recycler_active_readers);
        recyclerActiveReaders.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerNewBooks = findViewById(R.id.recycler_new_books);
        recyclerPopularBooks = findViewById(R.id.recycler_popular_books); // Ánh xạ thêm cái này
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        tvBorrowCount = findViewById(R.id.tv_borrowing_count);
        tvDueSoonCount = findViewById(R.id.tv_due_soon_count);
        tvUsername = findViewById(R.id.tv_home_username);
        etSearch = findViewById(R.id.et_search_home);
        btnBorrow = findViewById(R.id.btn_feature_borrow);
        btnReturn = findViewById(R.id.btn_feature_return);
        btnRead = findViewById(R.id.btn_feature_read);

        currentUserId = getSharedPreferences("BookHubPrefs", MODE_PRIVATE)
                .getInt("CURRENT_USER_ID", -1);
        if (currentUserId == -1) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return; // Dừng code tại đây
        }

        // 2. Cài đặt RecyclerView nằm NGANG (Horizontal)
        recyclerNewBooks.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerPopularBooks.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerActiveReaders.setNestedScrollingEnabled(false);

        SharedPreferences prefs = getSharedPreferences("BookHubPrefs", MODE_PRIVATE);
        String fullName = prefs.getString("CURRENT_USER_NAME", "Bạn");
        if (tvUsername != null) {
            tvUsername.setText("Xin chào, " + fullName + "!");
        }


        //XỬ LÝ TÌM KIẾM (Khi bấm Enter trên bàn phím)
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {

                String query = etSearch.getText().toString().trim();
                if (!query.isEmpty()) {
                    // Chuyển sang màn hình BookSearchActivity và gửi từ khóa
                    Intent intent = new Intent(HomeActivity.this, BookSearchActivity.class);
                    intent.putExtra("SEARCH_QUERY", query); // Gửi từ khóa sang kia
                    startActivity(intent);
                }
                return true;
            }
            return false;
        });

        // 3. XỬ LÝ CLICK CÁC NÚT TÍNH NĂNG

        // Nút Mượn sách -> Chuyển sang trang Tìm kiếm (để tìm sách mượn)
        btnBorrow.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, BookSearchActivity.class);
            startActivity(intent);
        });

        // Nút Lịch sử/Trả sách -> Chuyển sang trang Lịch sử mượn
        btnReturn.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, BorrowHistoryActivity.class);
            startActivity(intent);
        });

        btnRead.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, BookSearchActivity.class);
            startActivity(intent);
        });

        // 3. Gọi API lấy sách
        fetchNewBooks();
        fetchPopularBooks();
        fetchEvents();
        fetchActiveReaders();
        fetchUserStats();

        // 4. Cài đặt Footer (Bottom Navigation)
        setupBottomNavigation();

        // Sự kiện xem tất cả (giữ nguyên)
        View seeAllEvents = findViewById(R.id.text_see_all_events);
        if (seeAllEvents != null) {
            seeAllEvents.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, EventsActivity.class)));
        }
    }

    private void fetchUserStats() {
        RetrofitClient.getApiService().getUserStats(currentUserId).enqueue(new Callback<UserStatsResponse>() {
            @Override
            public void onResponse(Call<UserStatsResponse> call, Response<UserStatsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Cập nhật giao diện
                    tvBorrowCount.setText(String.valueOf(response.body().getBorrowing()));
                    tvDueSoonCount.setText(String.valueOf(response.body().getDueSoon()));
                }
            }

            @Override
            public void onFailure(Call<UserStatsResponse> call, Throwable t) {
                // Lỗi thì để mặc định là 0 hoặc "-"
                tvBorrowCount.setText("-");
                tvDueSoonCount.setText("-");
            }
        });
    }

    private void fetchNewBooks() {
        RetrofitClient.getApiService().getNewBooks().enqueue(new Callback<List<Book>>() {
            @Override
            public void onResponse(Call<List<Book>> call, Response<List<Book>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Dùng HomeAdapter đổ vào recyclerNewBooks
                    HomeAdapter adapter = new HomeAdapter(response.body(), book -> openBookDetail(book));
                    recyclerNewBooks.setAdapter(adapter);
                }
            }
            @Override
            public void onFailure(Call<List<Book>> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Lỗi : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchPopularBooks() {
        RetrofitClient.getApiService().getPopularBooks().enqueue(new Callback<List<Book>>() {
            @Override
            public void onResponse(Call<List<Book>> call, Response<List<Book>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Dùng HomeAdapter đổ vào recyclerPopularBooks
                    HomeAdapter adapter = new HomeAdapter(response.body(), book -> openBookDetail(book));
                    recyclerPopularBooks.setAdapter(adapter);
                }
            }
            @Override
            public void onFailure(Call<List<Book>> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Lỗi : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchEvents() {
        RetrofitClient.getApiService().getAllEvents().enqueue(new Callback<List<Event>>() {
            @Override
            public void onResponse(Call<List<Event>> call, Response<List<Event>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // SỬA ĐOẠN NÀY: Dùng EventHomeAdapter mới
                    EventHomeAdapter eventAdapter = new EventHomeAdapter(HomeActivity.this, response.body());

                    // Giả sử bạn có RecyclerView id là recycler_events_home
                    RecyclerView recyclerEvents = findViewById(R.id.recycler_events_featured); // ID trong layout home
                    recyclerEvents.setLayoutManager(new LinearLayoutManager(HomeActivity.this, LinearLayoutManager.HORIZONTAL, false));
                    recyclerEvents.setAdapter(eventAdapter);
                }
            }
            @Override
            public void onFailure(Call<List<Event>> call, Throwable t) {
                // Khi mất mạng hoặc lỗi server thì báo lỗi ra
                Toast.makeText(HomeActivity.this, "Lỗi tải sự kiện: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
    private void fetchActiveReaders() {
        RetrofitClient.getApiService().getActiveReaders().enqueue(new Callback<List<UserRank>>() {
            @Override
            public void onResponse(Call<List<UserRank>> call, Response<List<UserRank>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserRankAdapter adapter = new UserRankAdapter(HomeActivity.this, response.body());
                    recyclerActiveReaders.setAdapter(adapter);
                }
            }
            @Override
            public void onFailure(Call<List<UserRank>> call, Throwable t) {
                // Không làm gì hoặc log lỗi nhẹ
            }
        });
    }
}