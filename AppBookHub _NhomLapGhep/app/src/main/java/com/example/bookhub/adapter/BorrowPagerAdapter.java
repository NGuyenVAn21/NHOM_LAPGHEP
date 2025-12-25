package com.example.bookhub.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.bumptech.glide.Glide;
import com.example.bookhub.R;
import com.example.bookhub.api.RetrofitClient;
import com.example.bookhub.models.ActionRequest;
import com.example.bookhub.models.ActionResponse;
import com.example.bookhub.models.BorrowRecord;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BorrowPagerAdapter extends FragmentStateAdapter {

    public BorrowPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new BorrowingFragment();
            case 1: return new HistoryFragment();
            case 2: return new ReservationFragment();
            default: return new BorrowingFragment();
        }
    }

    @Override
    public int getItemCount() { return 3; }

    // Hàm lấy UserID từ SharedPreferences (Dùng chung cho các fragment)
    private static int getCurrentUserId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("BookHubPrefs", Context.MODE_PRIVATE);
        return prefs.getInt("CURRENT_USER_ID", -1);
    }

    // ==================== FRAGMENT 1: ĐANG MƯỢN ====================
    public static class BorrowingFragment extends Fragment {
        private RecyclerView recyclerView;
        private BorrowingListAdapter adapter;
        private List<BorrowRecord> list = new ArrayList<>();

        @Nullable @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_borrowing, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            recyclerView = view.findViewById(R.id.recycler_borrowing);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

            // Pass 'this' để adapter có thể gọi lại hàm loadData sau khi trả sách thành công
            adapter = new BorrowingListAdapter(list, getContext(), this);
            recyclerView.setAdapter(adapter);

            loadData();
        }

        public void loadData() {
            int uid = getCurrentUserId(getContext());
            if (uid == -1) return;

            RetrofitClient.getApiService().getCurrentBorrows(uid).enqueue(new Callback<List<BorrowRecord>>() {
                @Override
                public void onResponse(Call<List<BorrowRecord>> call, Response<List<BorrowRecord>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        list.clear();
                        list.addAll(response.body());
                        adapter.notifyDataSetChanged();
                    }
                }
                @Override public void onFailure(Call<List<BorrowRecord>> call, Throwable t) {}
            });
        }
    }

    // ==================== FRAGMENT 2: LỊCH SỬ ====================
    public static class HistoryFragment extends Fragment {
        private RecyclerView recyclerView;
        private HistoryListAdapter adapter;
        private List<BorrowRecord> list = new ArrayList<>();

        @Nullable @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_borrow_history, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            recyclerView = view.findViewById(R.id.recycler_history);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

            adapter = new HistoryListAdapter(list, getContext());
            recyclerView.setAdapter(adapter);

            loadData();
        }

        private void loadData() {
            int uid = getCurrentUserId(getContext());
            RetrofitClient.getApiService().getHistory(uid).enqueue(new Callback<List<BorrowRecord>>() {
                @Override
                public void onResponse(Call<List<BorrowRecord>> call, Response<List<BorrowRecord>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        list.clear();
                        list.addAll(response.body());
                        adapter.notifyDataSetChanged();
                    }
                }
                @Override public void onFailure(Call<List<BorrowRecord>> call, Throwable t) {}
            });
        }
    }

    // ==================== FRAGMENT 3: ĐẶT TRƯỚC ====================
    public static class ReservationFragment extends Fragment {
        private RecyclerView recyclerView;
        private ReservationListAdapter adapter;
        private List<BorrowRecord> list = new ArrayList<>();

        @Nullable @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_reservation, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            recyclerView = view.findViewById(R.id.recycler_reservation);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

            adapter = new ReservationListAdapter(list, getContext(), this);
            recyclerView.setAdapter(adapter);

            loadData();
        }

        public void loadData() {
            int uid = getCurrentUserId(getContext());
            RetrofitClient.getApiService().getReservations(uid).enqueue(new Callback<List<BorrowRecord>>() {
                @Override
                public void onResponse(Call<List<BorrowRecord>> call, Response<List<BorrowRecord>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        list.clear();
                        list.addAll(response.body());
                        adapter.notifyDataSetChanged();
                    }
                }
                @Override public void onFailure(Call<List<BorrowRecord>> call, Throwable t) {}
            });
        }
    }

    // =========================================================
    // KHU VỰC ADAPTER NỘI BỘ (XỬ LÝ HIỂN THỊ VÀ NÚT BẤM)
    // =========================================================

    // 1. ADAPTER ĐANG MƯỢN
    static class BorrowingListAdapter extends RecyclerView.Adapter<BorrowingListAdapter.VH> {
        private List<BorrowRecord> list;
        private Context ctx;
        private BorrowingFragment fragment; // Để reload data

        BorrowingListAdapter(List<BorrowRecord> list, Context ctx, BorrowingFragment fragment) {
            this.list = list;
            this.ctx = ctx;
            this.fragment = fragment;
        }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_borrowing_book, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            BorrowRecord b = list.get(pos);
            h.title.setText(b.getTitle());
            h.date1.setText("Mượn ngày: " + b.getBorrowDate());
            h.date2.setText("Hạn trả: " + b.getDueDate());
            h.status.setText(b.getDisplayStatus());

            // Set màu status từ API
            try {
                h.status.setBackgroundColor(Color.parseColor(b.getStatusColor()));
            } catch (Exception e) {
                h.status.setBackgroundColor(Color.GRAY);
            }

            // Load ảnh
            Glide.with(ctx).load(b.getCoverUrl()).placeholder(R.drawable.gradient_header).into(h.imgCover);

            // Xử lý nút TRẢ SÁCH
            h.btnReturn.setOnClickListener(v -> {
                new AlertDialog.Builder(ctx)
                        .setTitle("Xác nhận")
                        .setMessage("Bạn muốn trả sách này?")
                        .setPositiveButton("Đồng ý", (dialog, which) -> {
                            callApiAction("return", b.getRecordId());
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            });

            // Xử lý nút GIA HẠN
            h.btnRenew.setOnClickListener(v -> callApiAction("extend", b.getRecordId()));
        }

        private void callApiAction(String type, int recordId) {
            int uid = getCurrentUserId(ctx);
            ActionRequest req = new ActionRequest(uid, recordId);
            Call<ActionResponse> call = type.equals("return") ?
                    RetrofitClient.getApiService().returnBook(req) :
                    RetrofitClient.getApiService().extendBook(req);

            call.enqueue(new Callback<ActionResponse>() {
                @Override
                public void onResponse(Call<ActionResponse> call, Response<ActionResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Toast.makeText(ctx, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                        if (response.body().isSuccess()) {
                            fragment.loadData(); // Reload lại danh sách
                        }
                    } else {
                        Toast.makeText(ctx, "Thất bại", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override public void onFailure(Call<ActionResponse> call, Throwable t) {
                    Toast.makeText(ctx, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override public int getItemCount() { return list.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView title, date1, date2, status;
            MaterialButton btnReturn, btnRenew, btnView;
            ImageView imgCover; // Nếu layout item_borrowing_book có ImageView

            VH(@NonNull View v) {
                super(v);
                title = v.findViewById(R.id.text_title);
                date1 = v.findViewById(R.id.text_borrow_date);
                date2 = v.findViewById(R.id.text_due_date);
                status = v.findViewById(R.id.text_status);
                btnReturn = v.findViewById(R.id.btn_return);
                btnRenew = v.findViewById(R.id.btn_renew);
                btnView = v.findViewById(R.id.btn_view);
                // Bạn cần thêm ID bookImage vào item_borrowing_book.xml nếu chưa có
                // imgCover = v.findViewById(R.id.bookImage);
            }
        }
    }

    // 2. ADAPTER LỊCH SỬ (Chỉ hiển thị)
    static class HistoryListAdapter extends RecyclerView.Adapter<HistoryListAdapter.VH> {
        private List<BorrowRecord> list;
        private Context ctx;

        HistoryListAdapter(List<BorrowRecord> list, Context ctx) { this.list = list; this.ctx = ctx; }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history_book, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            BorrowRecord b = list.get(pos);
            h.title.setText(b.getTitle());
            h.date1.setText("Mượn: " + b.getBorrowDate());
            h.date2.setText("Trả: " + b.getReturnDate());
            h.status.setText(b.getDisplayStatus());
            // Glide load ảnh nếu có ImageView
        }

        @Override public int getItemCount() { return list.size(); }

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

    // 3. ADAPTER ĐẶT TRƯỚC
    static class ReservationListAdapter extends RecyclerView.Adapter<ReservationListAdapter.VH> {
        private List<BorrowRecord> list;
        private Context ctx;
        private ReservationFragment fragment;

        ReservationListAdapter(List<BorrowRecord> list, Context ctx, ReservationFragment fragment) {
            this.list = list;
            this.ctx = ctx;
            this.fragment = fragment;
        }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reservation_book, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            BorrowRecord b = list.get(pos);
            h.title.setText(b.getTitle());
            h.date1.setText("Đặt lúc: " + b.getBorrowDate()); // Tận dụng field
            h.status.setText(b.getDisplayStatus());

            try { h.status.setBackgroundColor(Color.parseColor(b.getStatusColor())); } catch (Exception e) {}

            // Nút Hủy
            h.btnCancel.setOnClickListener(v -> {
                int uid = getCurrentUserId(ctx);
                ActionRequest req = new ActionRequest(uid, b.getRecordId());
                RetrofitClient.getApiService().cancelReservation(req).enqueue(new Callback<ActionResponse>() {
                    @Override
                    public void onResponse(Call<ActionResponse> call, Response<ActionResponse> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(ctx, "Đã hủy", Toast.LENGTH_SHORT).show();
                            fragment.loadData();
                        }
                    }
                    @Override public void onFailure(Call<ActionResponse> call, Throwable t) {}
                });
            });
        }

        @Override public int getItemCount() { return list.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView title, date1, status;
            MaterialButton btnBorrow, btnCancel;
            VH(@NonNull View v) {
                super(v);
                title = v.findViewById(R.id.text_title);
                date1 = v.findViewById(R.id.text_reservation_date);
                status = v.findViewById(R.id.text_status);
                btnBorrow = v.findViewById(R.id.btn_borrow);
                btnCancel = v.findViewById(R.id.btn_cancel);
            }
        }
    }
}