package com.example.bookhub.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bookhub.R;
import com.example.bookhub.adapter.RatedBooksAdapter;
import com.example.bookhub.api.RetrofitClient;
import com.example.bookhub.models.UserReview;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RatedBooksActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rated_books);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Sách đã đánh giá");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerRatedBooks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        SharedPreferences prefs = getSharedPreferences("BookHubPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("CURRENT_USER_ID", -1);

        RetrofitClient.getApiService().getUserReviews(userId).enqueue(new Callback<List<UserReview>>() {
            @Override
            public void onResponse(Call<List<UserReview>> call, Response<List<UserReview>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    RatedBooksAdapter adapter = new RatedBooksAdapter(RatedBooksActivity.this, response.body());
                    recyclerView.setAdapter(adapter);
                }
            }
            @Override
            public void onFailure(Call<List<UserReview>> call, Throwable t) {
                Toast.makeText(RatedBooksActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}