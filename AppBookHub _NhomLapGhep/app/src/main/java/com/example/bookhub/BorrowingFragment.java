package com.example.bookhub; // Hoặc com.example.bookhub.activity nếu bạn để ở đó

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookhub.activity.ReadingActivity; // Import màn hình đọc
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
    private List<BorrowRecord> borrowList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Đảm bảo tên file layout đúng là fragment_borrowing
        return inflater.inflate(R.layout.fragment_borrowing, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ RecyclerView (ID trong XML phải là recycler_borrowing)
        recyclerView = view.findViewById(R.id.recycler_borrowing);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new BorrowingAdapter(getContext(), borrowList, this);
        recyclerView.setAdapter(adapter);

        loadData();
    }

    public void loadData() {
        // Lấy UserID từ SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("BookHubPrefs", Context.MODE_PRIVATE);
        int userId = prefs.getInt("CURRENT_USER_ID", -1);

        if (userId == -1) return;

        RetrofitClient.getApiService().getCurrentBorrows(userId).enqueue(new Callback<List<BorrowRecord>>() {
            @Override
            public void onResponse(Call<List<BorrowRecord>> call, Response<List<BorrowRecord>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    borrowList.clear();
                    borrowList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<BorrowRecord>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi tải dữ liệu: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- ADAPTER NỘI BỘ (Để gọn code) ---
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
            // Đảm bảo tên layout item đúng là item_borrowing_book
            View view = LayoutInflater.from(context).inflate(R.layout.item_borrowing_book, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            BorrowRecord record = list.get(position);

            holder.tvTitle.setText(record.getTitle());
            holder.tvBorrowDate.setText("Mượn: " + record.getBorrowDate());
            holder.tvDueDate.setText("Hạn: " + record.getDueDate());
            holder.tvStatus.setText(record.getDisplayStatus());

            // Xử lý màu trạng thái
            try {
                holder.tvStatus.setBackgroundColor(Color.parseColor(record.getStatusColor()));
            } catch (Exception e) {
                holder.tvStatus.setBackgroundColor(Color.GRAY);
            }

            // Load ảnh bìa
            String imgUrl = record.getCoverUrl();
            // Nếu server trả về thiếu http (ví dụ: "cover1.jpg") thì ghép vào
            if (imgUrl != null && !imgUrl.startsWith("http")) {
                imgUrl = "http://10.0.2.2:5280/images/" + imgUrl;
            }
            Glide.with(context).load(imgUrl).placeholder(R.drawable.ic_menu_book_round).into(holder.imgCover);

            // Nút TRẢ SÁCH
            holder.btnReturn.setOnClickListener(v -> {
                new AlertDialog.Builder(context)
                        .setTitle("Xác nhận trả sách")
                        .setMessage("Bạn có chắc muốn trả cuốn sách này?")
                        .setPositiveButton("Đồng ý", (dialog, which) -> callApiAction("return", record.getRecordId()))
                        .setNegativeButton("Hủy", null)
                        .show();
            });

            // Nút GIA HẠN
            holder.btnRenew.setOnClickListener(v -> callApiAction("extend", record.getRecordId()));

            // Nút XEM (Đọc sách)
            holder.btnView.setOnClickListener(v -> {
                Intent intent = new Intent(context, ReadingActivity.class);
                // Truyền dữ liệu sách sang màn hình đọc nếu cần
                context.startActivity(intent);
            });
        }

        private void callApiAction(String actionType, int recordId) {
            SharedPreferences prefs = context.getSharedPreferences("BookHubPrefs", Context.MODE_PRIVATE);
            int userId = prefs.getInt("CURRENT_USER_ID", -1);

            ActionRequest request = new ActionRequest(userId, recordId);
            Call<ActionResponse> call;

            if (actionType.equals("return")) {
                call = RetrofitClient.getApiService().returnBook(request);
            } else {
                call = RetrofitClient.getApiService().extendBook(request);
            }

            call.enqueue(new Callback<ActionResponse>() {
                @Override
                public void onResponse(Call<ActionResponse> call, Response<ActionResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Toast.makeText(context, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                        if (response.body().isSuccess()) {
                            fragment.loadData(); // Tải lại danh sách
                        }
                    } else {
                        Toast.makeText(context, "Thao tác thất bại", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<ActionResponse> call, Throwable t) {
                    Toast.makeText(context, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() { return list.size(); }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvBorrowDate, tvDueDate, tvStatus;
            MaterialButton btnReturn, btnRenew, btnView;
            ImageView imgCover;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                // Các ID này phải khớp trong file item_borrowing_book.xml
                tvTitle = itemView.findViewById(R.id.text_title);
                tvBorrowDate = itemView.findViewById(R.id.text_borrow_date);
                tvDueDate = itemView.findViewById(R.id.text_due_date);
                tvStatus = itemView.findViewById(R.id.text_status);
                btnReturn = itemView.findViewById(R.id.btn_return);
                btnRenew = itemView.findViewById(R.id.btn_renew);
                btnView = itemView.findViewById(R.id.btn_view);

                // Nếu item chưa có ID cho ảnh, hãy thêm id="@+id/imgBookCover" vào XML
                // Ở đây tôi giả định bạn có hoặc sẽ thêm nó. Nếu không có, hãy comment dòng này.
                imgCover = itemView.findViewById(R.id.bookImage); // Check lại ID ảnh trong layout item
                if (imgCover == null) imgCover = itemView.findViewById(R.id.img_book_cover); // Dự phòng
            }
        }
    }
}