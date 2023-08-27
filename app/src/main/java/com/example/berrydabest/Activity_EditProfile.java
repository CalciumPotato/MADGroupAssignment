package com.example.berrydabest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class Activity_EditProfile extends AppCompatActivity {

    private ImageView img_back_editProfile;
    private Button btn_editProfile_save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // findViewById
        img_back_editProfile = findViewById(R.id.img_back_editProfile);
        btn_editProfile_save = findViewById(R.id.btn_editProfile_save);

        // Listener
        img_back_editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btn_editProfile_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(Activity_EditProfile.this, "Changes saved", Toast.LENGTH_SHORT).show();
            }
        });
    }
}