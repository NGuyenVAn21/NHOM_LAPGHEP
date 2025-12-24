package com.example.bookhub;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookhub.api.ApiService;
import com.example.bookhub.api.RetrofitClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.net.URLEncoder;
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
    private ImageButton btnCart;
    private TextView cartBadge;

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
        setupCartButton();

        // GỌI API LẤY SÁCH
        fetchBooksFromApi();
    }

    private void initViews() {
        booksRecyclerView = findViewById(R.id.booksRecyclerView);
        searchInput = findViewById(R.id.searchInput);
        filterChips = findViewById(R.id.filterChips);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        btnCart = findViewById(R.id.btnCart);
        cartBadge = findViewById(R.id.cartBadge);
    }

    private void setupRecyclerView() {
        booksRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new BookAdapter(filteredBooks, new BookAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Book book) {
                Intent intent = new Intent(BookSearchActivity.this, BookDetailActivity.class);
                intent.putExtra("BOOK_ID", book.getId());
                startActivity(intent);
            }
        });
        booksRecyclerView.setAdapter(adapter);
    }

    private void setupCartButton() {
        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BookSearchActivity.this, CartActivity.class);
                startActivity(intent);
            }
        });
    }

    private void updateCartBadge() {
        CartManager cartManager = CartManager.getInstance(this);
        int cartCount = cartManager.getCartItemCount();

        if (cartCount > 0) {
            cartBadge.setText(String.valueOf(cartCount));
            cartBadge.setVisibility(View.VISIBLE);
        } else {
            cartBadge.setVisibility(View.GONE);
        }
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

    //  filterBooks():
    private void filterBooks(String category, String query) {
        String keyword = query.trim();

        if (keyword.isEmpty() && category.equals("Tất cả")) {
            // Hiển thị tất cả sách
            fetchBooksFromApi();
        } else if (keyword.isEmpty() && !category.equals("Tất cả")) {
            // Lọc theo category trên client side
            filterByCategory(category);
        } else {
            // Gọi API tìm kiếm và lọc theo category
            searchBooksFromApi(keyword, category);
        }
    }

    // Lọc theo category trên client side
    private void filterByCategory(String category) {
        filteredBooks.clear();
        if (category.equals("Tất cả")) {
            filteredBooks.addAll(allBooks);
        } else {
            for (Book book : allBooks) {
                if (book.getCategory() != null && book.getCategory().equals(category)) {
                    filteredBooks.add(book);
                }
            }
        }
        adapter.notifyDataSetChanged();

        if (filteredBooks.isEmpty()) {
            Toast.makeText(this, "Không có sách thuộc thể loại này", Toast.LENGTH_SHORT).show();
        }
    }

    private void searchBooksFromApi(String keyword, final String category) {
        try {
            String encodedKeyword = URLEncoder.encode(keyword, "UTF-8");

            ApiService apiService = RetrofitClient.getApiService();
            apiService.searchBooks(encodedKeyword).enqueue(new Callback<List<Book>>() {
                @Override
                public void onResponse(Call<List<Book>> call, Response<List<Book>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        // Lọc theo category nếu cần
                        List<Book> searchResults = response.body();
                        filteredBooks.clear();

                        if (category.equals("Tất cả")) {
                            filteredBooks.addAll(searchResults);
                        } else {
                            for (Book book : searchResults) {
                                if (book.getCategory() != null && book.getCategory().equals(category)) {
                                    filteredBooks.add(book);
                                }
                            }
                        }

                        adapter.notifyDataSetChanged();

                        if (filteredBooks.isEmpty()) {
                            Toast.makeText(BookSearchActivity.this, "Không tìm thấy sách phù hợp", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(BookSearchActivity.this, "Lỗi tìm kiếm", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<List<Book>> call, Throwable t) {
                    Log.e("SEARCH_API_ERROR", "Lỗi: " + t.getMessage());
                    Toast.makeText(BookSearchActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e("ENCODING_ERROR", "Lỗi encode: " + e.getMessage());
            Toast.makeText(this, "Lỗi tìm kiếm tiếng Việt", Toast.LENGTH_SHORT).show();
        }
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

    @Override
    protected void onResume() {
        super.onResume();
        // Cập nhật badge khi quay lại màn hình
        updateCartBadge();
    }
}