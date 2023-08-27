package com.example.berrydabest;

import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navigationView = findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Toast.makeText(MainActivity.this, "Home", Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.navigation_calendar:
                    // Handle dashboard navigation
                    Toast.makeText(MainActivity.this, "Calendar", Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.navigation_qrScanner:
                    // Handle notifications navigation
                    Toast.makeText(MainActivity.this, "QR Scanner", Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.navigation_myEvent:
                    // Handle notifications navigation
                    Toast.makeText(MainActivity.this, "My Event", Toast.LENGTH_SHORT).show();
                    return true;
            }
            return false;
        });
    }
}