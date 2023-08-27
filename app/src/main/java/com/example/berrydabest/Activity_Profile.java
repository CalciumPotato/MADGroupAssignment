package com.example.berrydabest;

import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class Activity_Profile extends AppCompatActivity {

    private Button btn_profile_edit;
    private ConstraintLayout layout_profile_content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // findViewById
        layout_profile_content = findViewById(R.id.layout_profile_content);
        btn_profile_edit = findViewById(R.id.btn_profile_edit);
        BottomNavigationView navigationView = findViewById(R.id.navigation);

        // Add code to check if got event. If no, show "no event" text on ConstriantView

        // Listeners
        // Go to Edit Profile page
        btn_profile_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent k = new Intent(Activity_Profile.this, Activity_EditProfile.class);
                    startActivity(k);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // Bottom navigation bar
        navigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Toast.makeText(Activity_Profile.this, "Home", Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.navigation_calendar:
                    // Handle dashboard navigation
                    Toast.makeText(Activity_Profile.this, "Calendar", Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.navigation_qrScanner:
                    // Handle notifications navigation
                    Toast.makeText(Activity_Profile.this, "QR Scanner", Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.navigation_myEvent:
                    // Handle notifications navigation
                    Toast.makeText(Activity_Profile.this, "My Event", Toast.LENGTH_SHORT).show();
                    return true;
            }
            return false;
        });
    }
}