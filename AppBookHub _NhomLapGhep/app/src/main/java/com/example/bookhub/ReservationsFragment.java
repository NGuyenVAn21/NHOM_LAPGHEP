package com.example.bookhub;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookhub.api.RetrofitClient;
import com.example.bookhub.models.ActionRequest;
import com.example.bookhub.models.ActionResponse;
import com.example.bookhub.models.BorrowRecord;
import com.example.bookhub.models.BorrowRequest;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReservationsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ReservationAdapter adapter;
    private List<BorrowRecord> list = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reservation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ RecyclerView
        recyclerView = view.findViewById(R.id.recycler_reservation);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ReservationAdapter(getContext(), list, this);
        recyclerView.setAdapter(adapter);

        // Tải dữ liệu lần đầu
        loadData();
    }

    // Hàm gọi API lấy danh sách đặt trước
    public void loadData() {
        if (getContext() == null) return;

        SharedPreferences prefs = requireContext().getSharedPreferences("BookHubPrefs", Context.MODE_PRIVATE);
        int userId = prefs.getInt("CURRENT_USER_ID", -1);

        if (userId == -1) return;

        RetrofitClient.getApiService().getReservations(userId).enqueue(new Callback<List<BorrowRecord>>() {
            @Override
            public void onResponse(Call<List<BorrowRecord>> call, Response<List<BorrowRecord>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    list.clear();
                    list.addAll(response.body());
                    adapter.notifyDataSetChanged();

                    // Nếu danh sách trống, có thể hiện thông báo (tùy chỉnh thêm)
                }
            }
            @Override
            public void onFailure(Call<List<BorrowRecord>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- ADAPTER XỬ LÝ DANH SÁCH ---
    public static class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ViewHolder> {
        private Context context;
        private List<BorrowRecord> list;
        private ReservationsFragment fragment;

        public ReservationAdapter(Context context, List<BorrowRecord> list, ReservationsFragment fragment) {
            this.context = context;
            this.list = list;
            this.fragment = fragment;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_reservation_book, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            BorrowRecord record = list.get(position);
            holder.tvTitle.setText(record.getTitle());
            holder.tvDate.setText("Ngày đặt: " + record.getBorrowDate());
            holder.tvStatus.setText(record.getDisplayStatus());

            // Tô màu trạng thái
            try {
                holder.tvStatus.setBackgroundColor(Color.parseColor(record.getStatusColor()));
            } catch (Exception e) {
                holder.tvStatus.setBackgroundColor(Color.GRAY);
            }

            // 1. XỬ LÝ NÚT HỦY ĐẶT
            holder.btnCancel.setOnClickListener(v -> {
                new AlertDialog.Builder(context)
                        .setTitle("Hủy đặt trước")
                        .setMessage("Bạn muốn hủy đặt cuốn sách này?")
                        .setPositiveButton("Đồng ý", (dialog, which) -> cancelReservation(record.getRecordId()))
                        .setNegativeButton("Không", null)
                        .show();
            });

            // 2. XỬ LÝ NÚT MƯỢN NGAY
            // Chỉ hiện nút khi trạng thái là "Sẵn sàng"
            if ("Sẵn sàng".equals(record.getDisplayStatus())) {
                holder.btnBorrow.setVisibility(View.VISIBLE);

                holder.btnBorrow.setOnClickListener(v -> {
                    // Gọi hàm xác nhận mượn
                    confirmBorrow(record.getBookId());
                });
            } else {
                holder.btnBorrow.setVisibility(View.GONE);
            }
        }

        // HÀM GỌI API MƯỢN SÁCH (CHUYỂN TỪ READY -> BORROWING)
        private void confirmBorrow(int bookId) {
            SharedPreferences prefs = context.getSharedPreferences("BookHubPrefs", Context.MODE_PRIVATE);
            int userId = prefs.getInt("CURRENT_USER_ID", -1);

            if (userId == -1) return;

            // Tạo request
            BorrowRequest request = new BorrowRequest(userId, bookId);

            // Gọi API
            RetrofitClient.getApiService().borrowBook(request).enqueue(new Callback<ActionResponse>() {
                @Override
                public void onResponse(Call<ActionResponse> call, Response<ActionResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        if (response.body().isSuccess()) {
                            Toast.makeText(context, "Mượn thành công! Vui lòng kiểm tra tab Đang mượn.", Toast.LENGTH_LONG).show();
                            // Load lại danh sách để xóa cuốn sách vừa mượn khỏi tab này
                            fragment.loadData();
                        } else {
                            Toast.makeText(context, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, "Lỗi server: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ActionResponse> call, Throwable t) {
                    Toast.makeText(context, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        }

        //  HÀM HỦY ĐẶT
        private void cancelReservation(int recordId) {
            SharedPreferences prefs = context.getSharedPreferences("BookHubPrefs", Context.MODE_PRIVATE);
            int userId = prefs.getInt("CURRENT_USER_ID", -1);

            ActionRequest request = new ActionRequest(userId, recordId);
            RetrofitClient.getApiService().cancelReservation(request).enqueue(new Callback<ActionResponse>() {
                @Override
                public void onResponse(Call<ActionResponse> call, Response<ActionResponse> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(context, "Đã hủy đặt trước", Toast.LENGTH_SHORT).show();
                        fragment.loadData(); // Load lại danh sách
                    }
                }
                @Override
                public void onFailure(Call<ActionResponse> call, Throwable t) {
                    Toast.makeText(context, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvDate, tvStatus;
            MaterialButton btnBorrow, btnCancel;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.text_title);
                tvDate = itemView.findViewById(R.id.text_reservation_date);
                tvStatus = itemView.findViewById(R.id.text_status);
                btnBorrow = itemView.findViewById(R.id.btn_borrow);
                btnCancel = itemView.findViewById(R.id.btn_cancel);
            }
        }
    }
}