package com.example.bookhub.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.bookhub.R;
import com.example.bookhub.model.Book;
import java.util.List;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.BookHomeViewHolder> {

    private List<Book> books;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(Book book);
    }

    public HomeAdapter(List<Book> books, OnItemClickListener listener) {
        this.books = books;
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public BookHomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Dùng layout item_book_home (thẻ nhỏ) thay vì item_book (thẻ to)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book_home, parent, false);
        return new BookHomeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookHomeViewHolder holder, int position) {
        Book book = books.get(position);
        holder.title.setText(book.getTitle());
        holder.author.setText(book.getAuthor());

        // --- SỬA ĐOẠN NÀY ---
        String imageUrl = book.getImageUrl();

        // Kiểm tra: Nếu chỉ là tên file (không có http) thì ghép thêm link server
        if (imageUrl != null && !imageUrl.startsWith("http")) {
            // Thay 5177 bằng cổng API của bạn
            imageUrl = "http://10.0.2.2:5280/images/" + imageUrl;
        }

        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .placeholder(R.drawable.ic_menu_book_round) // Ảnh chờ
                .error(R.drawable.ic_menu_book_round)       // Ảnh lỗi
                .into(holder.cover);
    }

    @Override
    public int getItemCount() { return books.size(); }

    static class BookHomeViewHolder extends RecyclerView.ViewHolder {
        TextView title, author;
        ImageView cover;

        public BookHomeViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_book_title);
            author = itemView.findViewById(R.id.tv_book_author);
            cover = itemView.findViewById(R.id.img_book_cover);
        }
    }
}