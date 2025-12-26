package com.example.bookhub.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bookhub.R;
import com.example.bookhub.adapter.EventAdapter;
import com.example.bookhub.api.RetrofitClient;
import com.example.bookhub.models.Event;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyEventsActivity extends AppCompatActivity {

    private RecyclerView recyclerMyEvents;
    private TextView tvEmpty;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_events);

        // 1. Ánh xạ View
        recyclerMyEvents = findViewById(R.id.recycler_my_events);
        tvEmpty = findViewById(R.id.tv_empty_events);
        ImageView btnBack = findViewById(R.id.btn_back_my_events);

        recyclerMyEvents.setLayoutManager(new LinearLayoutManager(this));

        // 2. Lấy ID người dùng
        SharedPreferences prefs = getSharedPreferences("BookHubPrefs", MODE_PRIVATE);
        currentUserId = prefs.getInt("CURRENT_USER_ID", -1);

        // 3. Xử lý nút Back
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        // 4. Gọi API lấy dữ liệu
        fetchMyEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Gọi lại API khi quay lại màn hình này (phòng trường hợp vừa hủy bên chi tiết)
        fetchMyEvents();
    }

    private void fetchMyEvents() {
        if (currentUserId == -1) return;

        RetrofitClient.getApiService().getMyEvents(currentUserId).enqueue(new Callback<List<Event>>() {
            @Override
            public void onResponse(Call<List<Event>> call, Response<List<Event>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Event> myEvents = response.body();

                    if (myEvents.isEmpty()) {
                        recyclerMyEvents.setVisibility(View.GONE);
                        tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        recyclerMyEvents.setVisibility(View.VISIBLE);
                        tvEmpty.setVisibility(View.GONE);

                        // Tái sử dụng EventAdapter có sẵn
                        EventAdapter adapter = new EventAdapter(MyEventsActivity.this, myEvents);
                        recyclerMyEvents.setAdapter(adapter);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Event>> call, Throwable t) {
                Toast.makeText(MyEventsActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }
}