package com.example.bookhub;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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

public class BorrowingFragment extends Fragment {

    private RecyclerView recyclerView;
    private BorrowingAdapter adapter;
    private List<BorrowRecord> list = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_borrowing, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Ánh xạ RecyclerView
        recyclerView = view.findViewById(R.id.recycler_borrowing);
        if (recyclerView == null) {
            Toast.makeText(getContext(), "Lỗi: Không tìm thấy ID recycler_borrowing", Toast.LENGTH_LONG).show();
            return;
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new BorrowingAdapter(getContext(), list, this);
        recyclerView.setAdapter(adapter);

        // 2. Gọi hàm tải dữ liệu
        loadData();
    }

    public void loadData() {
        if (getContext() == null) return;

        SharedPreferences prefs = requireContext().getSharedPreferences("BookHubPrefs", Context.MODE_PRIVATE);
        int uid = prefs.getInt("CURRENT_USER_ID", -1);

        if (uid == -1) {
            Toast.makeText(getContext(), "Chưa đăng nhập (ID=-1)", Toast.LENGTH_SHORT).show();
            return;
        }

        RetrofitClient.getApiService().getCurrentBorrows(uid).enqueue(new Callback<List<BorrowRecord>>() {
            @Override
            public void onResponse(Call<List<BorrowRecord>> call, Response<List<BorrowRecord>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    list.clear();
                    list.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "Lỗi API: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<BorrowRecord>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("BookHub", "Error", t);
            }
        });
    }

    // ADAPTER NỘI BỘ
    public static class BorrowingAdapter extends RecyclerView.Adapter<BorrowingAdapter.ViewHolder> {
        private Context context;
        private List<BorrowRecord> list;
        private BorrowingFragment fragment;

        public BorrowingAdapter(Context context, List<BorrowRecord> list, BorrowingFragment fragment) {
            this.context = context;
            this.list = list;
            this.fragment = fragment;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_borrowing_book, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            BorrowRecord b = list.get(position);
            holder.title.setText(b.getTitle());
            holder.date1.setText("Mượn: " + b.getBorrowDate());
            holder.date2.setText("Hạn: " + b.getDueDate());
            holder.status.setText(b.getDisplayStatus());

            try {
                holder.status.setBackgroundColor(Color.parseColor(b.getStatusColor()));
            } catch (Exception e) {
                holder.status.setBackgroundColor(Color.GRAY);
            }

            // Load ảnh
            String imgUrl = b.getCoverUrl();
            if (imgUrl != null && !imgUrl.startsWith("http")) {

                imgUrl = "http://10.0.2.2:5280/images/" + imgUrl;
            }
            Glide.with(context).load(imgUrl).placeholder(R.drawable.ic_menu_book_round).into(holder.imgCover);

            // Nút Trả sách
            holder.btnReturn.setOnClickListener(v -> {
                new AlertDialog.Builder(context)
                        .setTitle("Trả sách")
                        .setMessage("Xác nhận trả sách này?")
                        .setPositiveButton("Đồng ý", (dialog, which) -> callApiAction("return", b.getRecordId()))
                        .setNegativeButton("Hủy", null)
                        .show();
            });

            // Nút Gia hạn
            holder.btnRenew.setOnClickListener(v -> callApiAction("extend", b.getRecordId()));
            holder.btnView.setOnClickListener(v -> {
                Intent intent = new Intent(context, com.example.bookhub.activity.BookDetailActivity.class);

                // Tạo object Book tạm thời từ BorrowRecord để gửi sang màn hình chi tiết
                com.example.bookhub.models.Book tempBook = new com.example.bookhub.models.Book(
                        b.getBookId(),
                        b.getTitle(),
                        b.getAuthor(),
                        0f, // Rating (float)
                        0,  // Pages
                        b.getDisplayStatus(), // Status
                        // Lấy giá tiền từ API (Nếu null thì để 0 VND) ---
                        b.getPrice() != null ? b.getPrice() : "0 VND",
                        "Thông tin đang cập nhật..."
                );

                intent.putExtra("BOOK", tempBook);
                intent.putExtra("BOOK_IMAGE", b.getCoverUrl());
                context.startActivity(intent);
            });
        }

        private void callApiAction(String type, int recordId) {
            SharedPreferences prefs = context.getSharedPreferences("BookHubPrefs", Context.MODE_PRIVATE);
            int uid = prefs.getInt("CURRENT_USER_ID", -1);
            ActionRequest req = new ActionRequest(uid, recordId);

            Call<ActionResponse> call = type.equals("return") ?
                    RetrofitClient.getApiService().returnBook(req) :
                    RetrofitClient.getApiService().extendBook(req);

            call.enqueue(new Callback<ActionResponse>() {
                @Override
                public void onResponse(Call<ActionResponse> call, Response<ActionResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Toast.makeText(context, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                        if (response.body().isSuccess()) fragment.loadData();
                    }
                }
                @Override public void onFailure(Call<ActionResponse> call, Throwable t) {}
            });
        }

        @Override public int getItemCount() { return list.size(); }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView title, date1, date2, status;
            MaterialButton btnReturn, btnRenew, btnView;
            ImageView imgCover;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.text_title);
                date1 = itemView.findViewById(R.id.text_borrow_date);
                date2 = itemView.findViewById(R.id.text_due_date);
                status = itemView.findViewById(R.id.text_status);
                btnReturn = itemView.findViewById(R.id.btn_return);
                btnRenew = itemView.findViewById(R.id.btn_renew);
                btnView = itemView.findViewById(R.id.btn_view);
                imgCover = itemView.findViewById(R.id.bookImage);
            }
        }
    }
}