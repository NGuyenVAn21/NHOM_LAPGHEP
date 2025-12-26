package com.example.bookhub.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bookhub.R;
import com.example.bookhub.api.RetrofitClient;
import com.example.bookhub.models.UpdateProfileRequest;
import com.example.bookhub.models.UserDetail;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {
    EditText etName, etEmail, etPhone;
    int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        etName = findViewById(R.id.etEditFullname);
        etEmail = findViewById(R.id.etEditEmail);
        etPhone = findViewById(R.id.etEditPhone);

        SharedPreferences prefs = getSharedPreferences("BookHubPrefs", MODE_PRIVATE);
        userId = prefs.getInt("CURRENT_USER_ID", -1);

        loadCurrentInfo();

        findViewById(R.id.btnSaveProfile).setOnClickListener(v -> saveProfile());
    }

    private void loadCurrentInfo() {
        RetrofitClient.getApiService().getUserProfile(userId).enqueue(new Callback<UserDetail>() {
            @Override
            public void onResponse(Call<UserDetail> call, Response<UserDetail> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserDetail u = response.body();
                    etName.setText(u.getFullName());
                    etEmail.setText(u.getEmail());
                    etPhone.setText(u.getPhone());
                }
            }
            @Override
            public void onFailure(Call<UserDetail> call, Throwable t) {}
        });
    }

    private void saveProfile() {
        String name = etName.getText().toString();
        String email = etEmail.getText().toString();
        String phone = etPhone.getText().toString();

        UpdateProfileRequest req = new UpdateProfileRequest(name, email, phone);
        RetrofitClient.getApiService().updateProfile(userId, req).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(EditProfileActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();

                    // Cập nhật lại tên trong SharedPreferences để trang chủ hiển thị đúng
                    getSharedPreferences("BookHubPrefs", MODE_PRIVATE).edit()
                            .putString("CURRENT_USER_NAME", name).apply();

                    finish();
                } else {
                    Toast.makeText(EditProfileActivity.this, "Lỗi cập nhật", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(EditProfileActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }
}