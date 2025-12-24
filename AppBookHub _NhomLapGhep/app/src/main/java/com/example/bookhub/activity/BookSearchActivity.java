package com.example.bookhub.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookhub.R;
import com.example.bookhub.adapter.BookAdapter;
import com.example.bookhub.api.ApiService;
import com.example.bookhub.api.RetrofitClient;
import com.example.bookhub.model.Book;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookSearchActivity extends AppCompatActivity {

    private RecyclerView booksRecyclerView;
    private EditText searchInput;
    private ChipGroup filterChips;
    private BottomNavigationView bottomNavigationView;
    private BookAdapter adapter;

    // Danh sách gốc lấy từ API
    private List<Book> allBooks = new ArrayList<>();
    // Danh sách đang hiển thị (sau khi lọc)
    private List<Book> filteredBooks = new ArrayList<>();

    private final String[] categories = {"Tất cả", "Văn học", "Kinh tế", "Tâm lý", "Khoa học", "Lịch sử", "Kỹ năng"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_search);

        initViews();
        setupRecyclerView();
        setupFilterChips();
        setupSearch();
        setupBottomNavigation();

        // GỌI API LẤY SÁCH
        fetchBooksFromApi();
    }

    private void initViews() {
        booksRecyclerView = findViewById(R.id.booksRecyclerView);
        searchInput = findViewById(R.id.searchInput);
        filterChips = findViewById(R.id.filterChips);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
    }

    private void setupRecyclerView() {
        booksRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        // Khởi tạo adapter với danh sách rỗng ban đầu
        adapter = new BookAdapter(filteredBooks, new BookAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Book book) {
                Intent intent = new Intent(BookSearchActivity.this, BookDetailActivity.class);
                intent.putExtra("BOOK", book);
                startActivity(intent);
            }
        });
        booksRecyclerView.setAdapter(adapter);
    }

    // --- HÀM GỌI API ---
    private void fetchBooksFromApi() {
        ApiService apiService = RetrofitClient.getApiService();
        apiService.getAllBooks().enqueue(new Callback<List<Book>>() {
            @Override
            public void onResponse(Call<List<Book>> call, Response<List<Book>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allBooks.clear();
                    allBooks.addAll(response.body());

                    // Ban đầu hiển thị tất cả
                    filteredBooks.clear();
                    filteredBooks.addAll(allBooks);
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(BookSearchActivity.this, "Không lấy được dữ liệu sách", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Book>> call, Throwable t) {
                Log.e("API_ERROR", "Lỗi: " + t.getMessage());
                Toast.makeText(BookSearchActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupFilterChips() {
        for (int i = 0; i < categories.length; i++) {
            final String category = categories[i];
            Chip chip = new Chip(this);
            chip.setText(category);
            chip.setCheckable(true);

            // Style cho Chip
            chip.setChipBackgroundColorResource(R.color.chip_state_list);
            chip.setTextColor(getColorStateList(R.color.chip_text_state_list));

            if (i == 0) chip.setChecked(true);

            chip.setOnClickListener(v -> filterBooks(category, searchInput.getText().toString()));
            filterChips.addView(chip);
        }
    }

    private void setupSearch() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String selectedCategory = getSelectedCategory();
                filterBooks(selectedCategory, s.toString());
            }
        });
    }

    private String getSelectedCategory() {
        int checkedChipId = filterChips.getCheckedChipId();
        if (checkedChipId != View.NO_ID) {
            Chip checkedChip = findViewById(checkedChipId);
            return checkedChip.getText().toString();
        }
        return "Tất cả";
    }

    // Logic lọc sách (Client-side filtering)
    private void filterBooks(String category, String query) {
        filteredBooks.clear();
        String lowerQuery = query.toLowerCase();

        for (Book book : allBooks) {
            // 1. Logic lọc danh mục:
            // Nếu chọn "Tất cả" -> Luôn đúng (true)
            // Nếu chọn mục khác -> So sánh tên danh mục sách với chip đang chọn
            boolean matchesCategory;
            if (category.equals("Tất cả")) {
                matchesCategory = true;
            } else {
                matchesCategory = book.getCategory().equals(category);
            }

            // 2. Logic tìm kiếm từ khóa:
            boolean matchesQuery = query.isEmpty() ||
                    book.getTitle().toLowerCase().contains(lowerQuery) ||
                    book.getAuthor().toLowerCase().contains(lowerQuery);

            // 3. Kết hợp
            if (matchesCategory && matchesQuery) {
                filteredBooks.add(book);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.navigation_books);
        bottomNavigationView.setOnItemSelectedListener(item -> {
                    int itemId = item.getItemId();
                    if (itemId == R.id.navigation_home) {
                        startActivity(new Intent(BookSearchActivity.this, HomeActivity.class));
                        return true;
                    } else if (itemId == R.id.navigation_books) {
                        return true;
                    } else if (itemId == R.id.navigation_borrow) {
                        startActivity(new Intent(BookSearchActivity.this, BorrowHistoryActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                    } else if (itemId == R.id.navigation_account) {
                        startActivity(new Intent(BookSearchActivity.this, AccountActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                    }
                    return false;
                }
        );
    }
}