package com.example.bookhub; // Hoặc com.example.bookhub.activity

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
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReservationsFragment extends Fragment { // Tên class có thể là ReservationFragment (số ít) tùy bạn tạo

    private RecyclerView recyclerView;
    private ReservationAdapter adapter;
    private List<BorrowRecord> list = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Layout: fragment_reservation hoặc fragment_reservations
        return inflater.inflate(R.layout.fragment_reservation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ID: recycler_reservation
        recyclerView = view.findViewById(R.id.recycler_reservation);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ReservationAdapter(getContext(), list, this);
        recyclerView.setAdapter(adapter);

        loadData();
    }

    public void loadData() {
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
                }
            }
            @Override public void onFailure(Call<List<BorrowRecord>> call, Throwable t) {}
        });
    }

    // --- ADAPTER ---
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
            return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_reservation_book, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            BorrowRecord record = list.get(position);
            holder.tvTitle.setText(record.getTitle());
            holder.tvDate.setText("Ngày đặt: " + record.getBorrowDate());
            holder.tvStatus.setText(record.getDisplayStatus());

            try {
                holder.tvStatus.setBackgroundColor(Color.parseColor(record.getStatusColor()));
            } catch (Exception e) {}

            // Nút HỦY ĐẶT
            holder.btnCancel.setOnClickListener(v -> {
                new AlertDialog.Builder(context)
                        .setTitle("Hủy đặt trước")
                        .setMessage("Bạn muốn hủy đặt cuốn sách này?")
                        .setPositiveButton("Đồng ý", (dialog, which) -> cancelReservation(record.getRecordId()))
                        .setNegativeButton("Không", null)
                        .show();
            });

            // Nút MƯỢN NGAY (Chỉ hiện khi sách Sẵn sàng)
            if ("Sẵn sàng".equals(record.getDisplayStatus())) {
                holder.btnBorrow.setVisibility(View.VISIBLE);
                holder.btnBorrow.setOnClickListener(v -> Toast.makeText(context, "Vui lòng đến thư viện nhận sách", Toast.LENGTH_LONG).show());
            } else {
                holder.btnBorrow.setVisibility(View.GONE);
            }
        }

        private void cancelReservation(int recordId) {
            SharedPreferences prefs = context.getSharedPreferences("BookHubPrefs", Context.MODE_PRIVATE);
            int userId = prefs.getInt("CURRENT_USER_ID", -1);

            ActionRequest request = new ActionRequest(userId, recordId);
            RetrofitClient.getApiService().cancelReservation(request).enqueue(new Callback<ActionResponse>() {
                @Override
                public void onResponse(Call<ActionResponse> call, Response<ActionResponse> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(context, "Đã hủy đặt trước", Toast.LENGTH_SHORT).show();
                        fragment.loadData();
                    }
                }
                @Override public void onFailure(Call<ActionResponse> call, Throwable t) {}
            });
        }

        @Override
        public int getItemCount() { return list.size(); }

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