package com.example.bookhub;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<Book> cartItems;
    private OnCartItemClickListener listener;

    public interface OnCartItemClickListener {
        void onRemoveClick(int position);
        void onItemClick(Book book);
    }

    public CartAdapter(List<Book> cartItems, OnCartItemClickListener listener) {
        this.cartItems = cartItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        Book book = cartItems.get(position);
        holder.bind(book);

        holder.removeButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemoveClick(position);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(book);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        private ImageView bookImage;
        private TextView bookTitle, bookAuthor, bookPrice, removeButton;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            bookImage = itemView.findViewById(R.id.cartBookImage);
            bookTitle = itemView.findViewById(R.id.cartBookTitle);
            bookAuthor = itemView.findViewById(R.id.cartBookAuthor);
            bookPrice = itemView.findViewById(R.id.cartBookPrice);
            removeButton = itemView.findViewById(R.id.btnRemoveFromCart);
        }

        public void bind(Book book) {
            bookTitle.setText(book.getTitle());
            bookAuthor.setText(book.getAuthor());
            bookPrice.setText(book.getPrice());

            // Load ảnh
            if (book.getImageUrl() != null && !book.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(book.getImageUrl())
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(bookImage);
            } else {
                bookImage.setImageResource(R.drawable.ic_launcher_foreground);
            }
        }
    }
}