package com.example.bookhub;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

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
        private TextView bookTitle, bookAuthor, bookRating, bookPages, bookStatus;
        private View statusAvailable, statusBorrowed;
        private CardView bookCard;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
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