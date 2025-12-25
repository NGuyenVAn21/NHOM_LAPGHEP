package com.example.bookhub.activity;

import android.os.Bundle;
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

public class EventsActivity extends AppCompatActivity {

    private RecyclerView recyclerEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        // Thiết lập nút Back trên ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Danh sách Sự kiện");
        }

        // Cấu hình RecyclerView
        recyclerEvents = findViewById(R.id.recycler_events);
        recyclerEvents.setLayoutManager(new LinearLayoutManager(this));

        // Gọi API
        fetchEvents();
    }

    private void fetchEvents() {
        RetrofitClient.getApiService().getAllEvents().enqueue(new Callback<List<Event>>() {
            @Override
            public void onResponse(Call<List<Event>> call, Response<List<Event>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Event> eventList = response.body();
                    // Gắn Adapter
                    EventAdapter adapter = new EventAdapter(EventsActivity.this, eventList);
                    recyclerEvents.setAdapter(adapter);
                } else {
                    Toast.makeText(EventsActivity.this, "Không có sự kiện nào", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Event>> call, Throwable t) {
                Toast.makeText(EventsActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Quay lại trang trước khi bấm nút Back
        return true;
    }
}