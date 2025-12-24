package com.example.bookhub;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView; // 1. Thêm import ImageView
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

<<<<<<< HEAD
import com.bumptech.glide.Glide; // 2. Thêm import Glide
=======
>>>>>>> c2cac68d4af52a97d36a6f7803a2b9c2807cb0c6
import com.example.bookhub.Book;
import com.example.bookhub.R;

import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    private List<Book> books;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(Book book);
    }

    public BookAdapter(List<Book> books, OnItemClickListener onItemClickListener) {
        this.books = books;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = books.get(position);
        holder.bind(book);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClick(book);
            }
        });
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    static class BookViewHolder extends RecyclerView.ViewHolder {
        // 3. Khai báo biến ImageView
        private ImageView bookImage;

        private TextView bookTitle, bookAuthor, bookRating, bookPages, bookStatus;
        private View statusAvailable, statusBorrowed;
        private CardView bookCard;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            // 4. Ánh xạ ID (Lưu ý: Trong file item_book.xml của bạn phải có ImageView với id là bookImage)
            bookImage = itemView.findViewById(R.id.bookImage);
            bookTitle = itemView.findViewById(R.id.bookTitle);
            bookAuthor = itemView.findViewById(R.id.bookAuthor);
            bookRating = itemView.findViewById(R.id.bookRating);
            bookPages = itemView.findViewById(R.id.bookPages);
            bookStatus = itemView.findViewById(R.id.bookStatus);
            statusAvailable = itemView.findViewById(R.id.statusAvailable);
            statusBorrowed = itemView.findViewById(R.id.statusBorrowed);
            bookCard = itemView.findViewById(R.id.bookCard);
        }

        public void bind(Book book) {
            bookTitle.setText(book.getTitle());
            bookAuthor.setText(book.getAuthor());
            bookRating.setText("⭐ " + book.getRating());
            bookPages.setText(book.getPages() + " trang");
            bookStatus.setText(book.getStatus());

            // --- 5. LOAD ẢNH BẰNG GLIDE ---
            if (book.getImageUrl() != null && !book.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(book.getImageUrl())                       // Link ảnh từ API
                        .placeholder(R.drawable.ic_launcher_background) // Ảnh hiển thị khi đang tải
                        .error(R.drawable.ic_launcher_foreground)       // Ảnh hiển thị nếu link lỗi
                        .into(bookImage);                               // Đẩy ảnh vào ImageView
            } else {

                bookImage.setImageResource(R.drawable.ic_launcher_foreground);
            }

            // Cập nhật hiển thị trạng thái
            if ("Có sẵn".equals(book.getStatus())) {
                statusAvailable.setVisibility(View.VISIBLE);
                statusBorrowed.setVisibility(View.GONE);
                bookStatus.setTextColor(itemView.getContext().getColor(R.color.success_green));
            } else {
                statusAvailable.setVisibility(View.GONE);
                statusBorrowed.setVisibility(View.VISIBLE);
                bookStatus.setTextColor(itemView.getContext().getColor(R.color.error_red));
            }
        }
    }
}