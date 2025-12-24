package com.example.bookhub;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

public class BorrowPagerAdapter extends FragmentStateAdapter {

    public BorrowPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new BorrowingFragment();
            case 1:
                return new HistoryFragment();
            case 2:
                return new ReservationFragment();
            default:
                return new BorrowingFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }

    // ==================== FRAGMENT 1: ĐANG MƯỢN ====================
    public static class BorrowingFragment extends Fragment {
        private RecyclerView recyclerView;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_borrowing, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            recyclerView = view.findViewById(R.id.recycler_borrowing);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

            List<BookItem> books = new ArrayList<>();
            books.add(new BookItem("Tư Duy Phản Biện", "10/11/2023", "24/11/2023", "Đang mượn", "borrowing"));
            books.add(new BookItem("Đặc Nhân Tâm", "05/11/2023", "19/11/2023", "Đang mượn", "borrowing"));
            books.add(new BookItem("Sapiens", "01/11/2023", "15/11/2023", "Quá hạn", "overdue"));

            recyclerView.setAdapter(new BorrowingListAdapter(books, getContext()));
        }
    }

    // ==================== FRAGMENT 2: LỊCH SỬ ====================
    public static class HistoryFragment extends Fragment {
        private RecyclerView recyclerView;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_borrow_history, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            recyclerView = view.findViewById(R.id.recycler_history);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

            List<BookItem> books = new ArrayList<>();
            books.add(new BookItem("Sapiens", "01/11/2023", "15/11/2023", "Đã trả", "returned"));
            books.add(new BookItem("Atomic Habits", "20/10/2023", "03/11/2023", "Đã trả", "returned"));
            books.add(new BookItem("1984", "15/10/2023", "29/10/2023", "Đã trả", "returned"));

            recyclerView.setAdapter(new HistoryListAdapter(books));
        }
    }

    // ==================== FRAGMENT 3: ĐẶT TRƯỚC ====================
    public static class ReservationFragment extends Fragment {
        private RecyclerView recyclerView;
        private ReservationListAdapter adapter;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_reservation, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            recyclerView = view.findViewById(R.id.recycler_reservation);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

            List<BookItem> books = new ArrayList<>();
            books.add(new BookItem("Nhà Giả Kim", "12/11/2023", "20/11/2023", "Sẵn sàng", "ready"));
            books.add(new BookItem("Tư Duy Phản Biện", "10/11/2023", "18/11/2023", "Chờ sách", "waiting"));

            adapter = new ReservationListAdapter(books, getContext());
            recyclerView.setAdapter(adapter);
        }
    }

    // ==================== MODEL ====================
    public static class BookItem {
        public String title, date1, date2, status, type;

        public BookItem(String title, String date1, String date2, String status, String type) {
            this.title = title;
            this.date1 = date1;
            this.date2 = date2;
            this.status = status;
            this.type = type;
        }
    }

    // ==================== ADAPTER 1: ĐANG MƯỢN ====================
    static class BorrowingListAdapter extends RecyclerView.Adapter<BorrowingListAdapter.VH> {
        private List<BookItem> books;
        private android.content.Context ctx;

        BorrowingListAdapter(List<BookItem> books, android.content.Context ctx) {
            this.books = books;
            this.ctx = ctx;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_borrowing_book, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            BookItem b = books.get(pos);
            h.title.setText(b.title);
            h.date1.setText("Mượn ngày: " + b.date1);
            h.date2.setText("Hạn: " + b.date2);
            h.status.setText(b.status);
            h.status.setBackgroundColor(b.type.equals("overdue") ?
                    android.graphics.Color.parseColor("#F44336") : android.graphics.Color.parseColor("#9C27B0"));

            h.btn1.setOnClickListener(v -> Toast.makeText(ctx, "Trả " + b.title, Toast.LENGTH_SHORT).show());
            h.btn2.setOnClickListener(v -> Toast.makeText(ctx, "Gia hạn " + b.title, Toast.LENGTH_SHORT).show());
            h.btn3.setOnClickListener(v -> Toast.makeText(ctx, "Xem " + b.title, Toast.LENGTH_SHORT).show());
        }

        @Override
        public int getItemCount() {
            return books.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            TextView title, date1, date2, status;
            MaterialButton btn1, btn2, btn3;

            VH(@NonNull View v) {
                super(v);
                title = v.findViewById(R.id.text_title);
                date1 = v.findViewById(R.id.text_borrow_date);
                date2 = v.findViewById(R.id.text_due_date);
                status = v.findViewById(R.id.text_status);
                btn1 = v.findViewById(R.id.btn_return);
                btn2 = v.findViewById(R.id.btn_renew);
                btn3 = v.findViewById(R.id.btn_view);
            }
        }
    }

    // ==================== ADAPTER 2: LỊCH SỬ ====================
    static class HistoryListAdapter extends RecyclerView.Adapter<HistoryListAdapter.VH> {
        private List<BookItem> books;

        HistoryListAdapter(List<BookItem> books) {
            this.books = books;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_history_book, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            BookItem b = books.get(pos);
            h.title.setText(b.title);
            h.date1.setText("Mượn ngày: " + b.date1);
            h.date2.setText("Trả ngày: " + b.date2);
            h.status.setText(b.status);
            h.status.setBackgroundColor(android.graphics.Color.parseColor("#FFEBEE"));
            h.status.setTextColor(android.graphics.Color.parseColor("#F44336"));
        }

        @Override
        public int getItemCount() {
            return books.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            TextView title, date1, date2, status;

            VH(@NonNull View v) {
                super(v);
                title = v.findViewById(R.id.text_title);
                date1 = v.findViewById(R.id.text_borrow_date);
                date2 = v.findViewById(R.id.text_return_date);
                status = v.findViewById(R.id.text_status);
            }
        }
    }

    // ==================== ADAPTER 3: ĐẶT TRƯỚC ====================
    static class ReservationListAdapter extends RecyclerView.Adapter<ReservationListAdapter.VH> {
        private List<BookItem> books;
        private android.content.Context ctx;

        ReservationListAdapter(List<BookItem> books, android.content.Context ctx) {
            this.books = books;
            this.ctx = ctx;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_reservation_book, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            BookItem b = books.get(pos);
            h.title.setText(b.title);
            h.date1.setText("Đặt ngày: " + b.date1);
            h.date2.setText("Có sách: " + b.date2);
            h.status.setText(b.status);
            h.status.setBackgroundColor(b.type.equals("ready") ?
                    android.graphics.Color.parseColor("#4CAF50") : android.graphics.Color.parseColor("#FF9800"));

            if (b.type.equals("ready")) {
                h.btn1.setVisibility(View.VISIBLE);
                h.btn1.setOnClickListener(v -> Toast.makeText(ctx, "Mượn " + b.title, Toast.LENGTH_SHORT).show());
            } else {
                h.btn1.setVisibility(View.GONE);
            }

            h.btn2.setOnClickListener(v -> {
                books.remove(pos);
                notifyItemRemoved(pos);
                Toast.makeText(ctx, "Hủy đặt " + b.title, Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public int getItemCount() {
            return books.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            TextView title, date1, date2, status;
            MaterialButton btn1, btn2;

            VH(@NonNull View v) {
                super(v);
                title = v.findViewById(R.id.text_title);
                date1 = v.findViewById(R.id.text_reservation_date);
                date2 = v.findViewById(R.id.text_available_date);
                status = v.findViewById(R.id.text_status);
                btn1 = v.findViewById(R.id.btn_borrow);
                btn2 = v.findViewById(R.id.btn_cancel);
            }
        }
    }
}