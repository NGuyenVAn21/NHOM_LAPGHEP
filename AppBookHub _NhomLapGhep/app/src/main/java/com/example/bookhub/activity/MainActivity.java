package com.example.bookhub.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Chuyển thẳng đến màn hình đọc sách
        Intent intent = new Intent(this, ReadingActivity.class);
        startActivity(intent);
        finish();
    }
}