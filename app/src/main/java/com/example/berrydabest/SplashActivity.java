package com.example.berrydabest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {
            // Start the main activity
            Intent intent = new Intent(SplashActivity.this, SignIn.class);
            startActivity(intent);
            finish();
        }, 1000); // Delay 1 second before going into MainPAge
    }
}
