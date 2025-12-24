package com.example.bookhub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity {

    private RecyclerView cartRecyclerView;
    private CartAdapter cartAdapter;
    private View emptyCartView, cartFooter;
    private TextView tvTotalPrice, tvCartCount;
    private Button btnCheckout, btnBrowseBooks;
    private CartManager cartManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        cartManager = CartManager.getInstance(this);
        initViews();
        setupCart();
        setupClickListeners();
    }

    private void initViews() {
        cartRecyclerView = findViewById(R.id.cartRecyclerView);
        emptyCartView = findViewById(R.id.emptyCartView);
        cartFooter = findViewById(R.id.cartFooter);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        tvCartCount = findViewById(R.id.tvCartCount);
        btnCheckout = findViewById(R.id.btnCheckout);
        btnBrowseBooks = findViewById(R.id.btnBrowseBooks);

        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupCart() {
        List<Book> cartItems = cartManager.getCartItems();

        if (cartItems.isEmpty()) {
            showEmptyCart();
        } else {
            showCartItems(cartItems);
        }
    }

    private void showEmptyCart() {
        emptyCartView.setVisibility(View.VISIBLE);
        cartRecyclerView.setVisibility(View.GONE);
        cartFooter.setVisibility(View.GONE);
    }

    private void showCartItems(List<Book> cartItems) {
        emptyCartView.setVisibility(View.GONE);
        cartRecyclerView.setVisibility(View.VISIBLE);
        cartFooter.setVisibility(View.VISIBLE);

        cartAdapter = new CartAdapter(cartItems, new CartAdapter.OnCartItemClickListener() {
            @Override
            public void onRemoveClick(int position) {
                Book book = cartItems.get(position);
                cartManager.removeFromCart(book.getId());
                cartItems.remove(position);
                cartAdapter.notifyItemRemoved(position);

                if (cartItems.isEmpty()) {
                    showEmptyCart();
                } else {
                    updateTotalPrice();
                }

                Toast.makeText(CartActivity.this, "Đã xóa sách khỏi giỏ", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemClick(Book book) {
                Intent intent = new Intent(CartActivity.this, BookDetailActivity.class);
                intent.putExtra("BOOK_ID", book.getId());
                startActivity(intent);
            }
        });

        cartRecyclerView.setAdapter(cartAdapter);
        updateTotalPrice();
    }

    private void updateTotalPrice() {
        List<Book> cartItems = cartManager.getCartItems();
        int total = 0;

        for (Book book : cartItems) {
            // Giả sử price có định dạng "200.000 VND"
            String priceStr = book.getPrice().replace(" VND", "").replace(".", "");
            try {
                int price = Integer.parseInt(priceStr);
                total += price;
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        NumberFormat formatter = NumberFormat.getInstance(Locale.getDefault());
        String formattedTotal = formatter.format(total) + " VND";
        tvTotalPrice.setText(formattedTotal);
    }

    private void setupClickListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnBrowseBooks.setOnClickListener(v -> {
            Intent intent = new Intent(this, BookSearchActivity.class);
            startActivity(intent);
            finish();
        });

        btnCheckout.setOnClickListener(v -> {
            // Xử lý thanh toán/mượn sách
            Toast.makeText(this, "Tính năng đang phát triển", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupCart(); // Refresh khi quay lại activity
    }
}