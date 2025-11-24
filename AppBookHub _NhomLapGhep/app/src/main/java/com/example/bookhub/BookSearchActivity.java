package com.example.bookhub;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class BookSearchActivity extends AppCompatActivity {

    private RecyclerView booksRecyclerView;
    private EditText searchInput;
    private ChipGroup filterChips;
    private BottomNavigationView bottomNavigationView;
    private BookAdapter adapter;

    private List<Book> allBooks = new ArrayList<>();
    private List<Book> filteredBooks = new ArrayList<>();

    private final String[] categories = {"Tất cả", "Khoa học", "Văn học", "Lịch sử", "Kinh tế", "Tâm lý", "Kỹ năng"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_search);

        initViews();
        setupBooks();
        setupRecyclerView();
        setupFilterChips();
        setupSearch();
        setupBottomNavigation();
    }

    private void initViews() {
        booksRecyclerView = findViewById(R.id.booksRecyclerView);
        searchInput = findViewById(R.id.searchInput);
        filterChips = findViewById(R.id.filterChips);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
    }

    private void setupBooks() {
        // Thêm nhiều sách hơn
        allBooks.add(new Book(1, "Nhà Giả Kim", "Paulo Coelho", 4.7f, 356,
                "Có sẵn", "Văn học", "NXB Văn Học", 2022,
                "Nhà Giả Kim của Paulo Coelho là một câu chuyện về hành trình tìm kiếm vận mệnh của một chàng trai chăn cừu trẻ tuổi tên Santiago..."));

        allBooks.add(new Book(2, "Tư Duy Phản Biện", "Zoe McKey", 4.4f, 218,
                "Đã mượn", "Kỹ năng", "NXB Tổng hợp", 2020,
                "Tư Duy Phản Biện giúp bạn phát triển kỹ năng phân tích, đánh giá thông tin một cách khách quan và logic."));

        allBooks.add(new Book(3, "Đắc Nhân Tâm", "Dale Carnegie", 4.6f, 291,
                "Có sẵn", "Kỹ năng", "NXB Trẻ", 2021,
                "Đắc Nhân Tâm của Dale Carnegie là cuốn sách nổi tiếng về nghệ thuật thu phục lòng người."));

        allBooks.add(new Book(4, "Sức Mạnh Tiềm Thức", "Joseph Murphy", 4.5f, 320,
                "Có sẵn", "Tâm lý", "NXB Hồng Đức", 2021,
                "Sức Mạnh Tiềm Thức khám phá sức mạnh vô hạn của tiềm thức và cách thức vận dụng nó để đạt được thành công."));

        allBooks.add(new Book(5, "Sapiens", "Yuval Noah Harari", 4.8f, 443,
                "Có sẵn", "Khoa học", "NXB Thế giới", 2020,
                "Sapiens là cuốn sách về lịch sử nhân loại, từ thời kỳ đồ đá đến hiện đại."));

        allBooks.add(new Book(6, "Tâm Lý Học", "Robert S. Feldman", 4.3f, 528,
                "Đã mượn", "Tâm lý", "NXB Giáo dục", 2019,
                "Giáo trình tâm lý học toàn diện cho người mới bắt đầu."));

        allBooks.add(new Book(7, "Kinh Tế Học", "Gregory Mankiw", 4.5f, 389,
                "Có sẵn", "Kinh tế", "NXB Kinh Tế", 2021,
                "Giáo trình kinh tế học cơ bản cho mọi người."));

        allBooks.add(new Book(8, "Lịch Sử Việt Nam", "Lê Thành Khôi", 4.6f, 512,
                "Có sẵn", "Lịch sử", "NXB Chính Trị", 2020,
                "Toàn cảnh lịch sử Việt Nam từ thời nguyên thủy đến hiện đại."));

        filteredBooks.addAll(allBooks);
    }

    private void setupRecyclerView() {
        booksRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
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

    private void setupFilterChips() {
        for (int i = 0; i < categories.length; i++) {
            final String category = categories[i];
            Chip chip = new Chip(this);
            chip.setText(category);
            chip.setCheckable(true);
            chip.setChipBackgroundColorResource(R.color.chip_state_list);
            chip.setTextColor(getColorStateList(R.color.chip_text_state_list));
            chip.setChipStrokeWidth(0f);
            chip.setChipCornerRadius(20f);

            if (i == 0) {
                chip.setChecked(true);
            }

            chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    filterBooks(category, searchInput.getText().toString());
                }
            });

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

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.navigation_books);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.navigation_home) {
                    Intent intent = new Intent(BookSearchActivity.this, HomeActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;
                } else if (itemId == R.id.navigation_books) {
                    // Đã ở trang sách
                    return true;
                } else if (itemId == R.id.navigation_borrow) {
                    Intent intent = new Intent(BookSearchActivity.this, BorrowHistoryActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;
                } else if (itemId == R.id.navigation_account) {
                    Intent intent = new Intent(BookSearchActivity.this, AccountActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;
                }
                return false;
            }
        });

        // Đặt mục "Sách" là được chọn mặc định
        bottomNavigationView.setSelectedItemId(R.id.navigation_books);
    }

    private String getSelectedCategory() {
        int checkedChipId = filterChips.getCheckedChipId();
        if (checkedChipId != View.NO_ID) {
            Chip checkedChip = findViewById(checkedChipId);
            return checkedChip.getText().toString();
        }
        return "Tất cả";
    }

    private void filterBooks(String category, String query) {
        filteredBooks.clear();

        for (Book book : allBooks) {
            boolean matchesCategory = "Tất cả".equals(category) || book.getCategory().equals(category);
            boolean matchesQuery = query.isEmpty() ||
                    book.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    book.getAuthor().toLowerCase().contains(query.toLowerCase());

            if (matchesCategory && matchesQuery) {
                filteredBooks.add(book);
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void showToast(String message) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
    }
}