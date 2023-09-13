package com.example.berrydabest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class CalendarActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        // Change status bar colour
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.darker_grey));
        }

        BottomNavigationView navigationView = findViewById(R.id.navigation);
        navigationView.setSelectedItemId(R.id.navigation_calendar);

        navigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    startActivity(new Intent(CalendarActivity.this, MainPage.class));
                    return true;
                case R.id.navigation_calendar:
                    // Handle dashboard navigation
                    return true;
                case R.id.navigation_qrScanner:
                    // Handle notifications navigation
                    Intent intent = new Intent(this, QR_Scan.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.right, R.anim.left);
                    Toast.makeText(CalendarActivity.this, "QR Scanner", Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.navigation_myEvent:
                    // Handle notifications navigation
                    startActivity(new Intent(CalendarActivity.this, MyEvent.class));
                    return true;
            }
            return false;
        });
    }
}