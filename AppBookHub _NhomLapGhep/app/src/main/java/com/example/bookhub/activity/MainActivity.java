package com.example.bookhub.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Chuyển đến màn hình Home thay vì ReadingActivity
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}