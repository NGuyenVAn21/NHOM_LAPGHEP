package com.example.bookhub;

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
import com.example.bookhub.models.BorrowRecord;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private HistoryAdapter adapter;
    private List<BorrowRecord> historyList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Layout: fragment_borrow_history (hoặc fragment_history tùy tên file của bạn)
        return inflater.inflate(R.layout.fragment_borrow_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ID: recycler_history
        recyclerView = view.findViewById(R.id.recycler_history);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new HistoryAdapter(getContext(), historyList);
        recyclerView.setAdapter(adapter);

        loadData();
    }

    private void loadData() {
        SharedPreferences prefs = requireContext().getSharedPreferences("BookHubPrefs", Context.MODE_PRIVATE);
        int userId = prefs.getInt("CURRENT_USER_ID", -1);

        if (userId == -1) return;

        RetrofitClient.getApiService().getHistory(userId).enqueue(new Callback<List<BorrowRecord>>() {
            @Override
            public void onResponse(Call<List<BorrowRecord>> call, Response<List<BorrowRecord>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    historyList.clear();
                    historyList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onFailure(Call<List<BorrowRecord>> call, Throwable t) {
                // Fail silently or log
            }
        });
    }

    // --- ADAPTER ---
    public static class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
        private Context context;
        private List<BorrowRecord> list;

        public HistoryAdapter(Context context, List<BorrowRecord> list) {
            this.context = context;
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_history_book, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            BorrowRecord record = list.get(position);
            holder.tvTitle.setText(record.getTitle());
            holder.tvBorrowDate.setText("Mượn: " + record.getBorrowDate());
            holder.tvReturnDate.setText("Trả: " + record.getReturnDate());
            holder.tvStatus.setText("Đã trả");
            // Set màu đỏ nhạt cho text Đã trả
            holder.tvStatus.setTextColor(Color.parseColor("#F44336"));
            holder.tvStatus.setBackgroundColor(Color.parseColor("#FFEBEE"));
        }

        @Override
        public int getItemCount() { return list.size(); }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvBorrowDate, tvReturnDate, tvStatus;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.text_title);
                tvBorrowDate = itemView.findViewById(R.id.text_borrow_date);
                tvReturnDate = itemView.findViewById(R.id.text_return_date); // Đảm bảo ID này có trong item_history_book
                tvStatus = itemView.findViewById(R.id.text_status);
            }
        }
    }
}