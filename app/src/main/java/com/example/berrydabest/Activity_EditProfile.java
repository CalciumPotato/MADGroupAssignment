package com.example.berrydabest;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Activity_EditProfile extends AppCompatActivity {

    private ImageView img_back_editProfile;
    private EditText et_editProfile_name, et_editProfile_email, et_editProfile_phone;
    private Button btn_editProfile_save;

    final Handler handler = new Handler();
    Intent intent_receive = getIntent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        Log.i("##### DEBUG #####", "email: " + intent_receive.getStringExtra("email"));

        // findViewById
        img_back_editProfile = findViewById(R.id.img_back_editProfile);
        et_editProfile_name = findViewById(R.id.et_editProfile_name);
        et_editProfile_email = findViewById(R.id.et_editProfile_email);
        et_editProfile_phone = findViewById(R.id.et_editProfile_phone);
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

                // Create thread
                MyThread connectThread = new MyThread(et_editProfile_email.getText().toString(),
                        handler);
                connectThread.start();
            }
        });

    }

    // Create thread
    private class MyThread extends Thread {

        private String email;
        private String username, phone;
        //private String password, picture, google_acc;
        private Handler handler;

        HttpURLConnection urlConnection = null;

        // 2.3 Constructor
        public MyThread(String email, Handler handler) {
            this.email = email;
            // Question 4
            this.username = username;
            this.phone = phone;
            this.handler = handler;
        }

        @Override
        public void run() {
            try {

                // 1. Access to the Supabase URL
                urlConnection = Activity_Profile_Tools.connectSupabaseUser(email, getString(R.string.SUPABASE_KEY), getString(R.string.SUPABASE_KEY));

                // Create a new instance of a JSONObject
/*
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("Email", email);
                jsonObject.put("Username", username);
                jsonObject.put("Phone", phone);
*/

                // 2.4 Obtain the status of connection
                // 200: Connected
                int responseCode = urlConnection.getResponseCode();
                Log.i("##### DEBUG #####", "code: " + responseCode);

                // 2.5.2 Read the content
                InputStream input = urlConnection.getInputStream();
                String returned_result = Activity_Profile_Tools.readStream(input);
                Log.i("##### DEBUG #####", "returned result: " + returned_result);

                if (responseCode == 200)
                {

                    // Sub thread can't update UI, need to call handler to call Main thread to update it
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Activity_EditProfile.this, "Successfully Connected!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

                    BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    String jsonResponse = response.toString();
                    Log.i("##### DEBUG #####", "API Response: " + jsonResponse);

/*                    // Delete: Question 3.3
                    Intent successIntent = new Intent(Activity_EditProfile.this, SuccessActivity.class);
                    successIntent.putExtra("result", returned_result);
                    Log.i("##### WebServiceActivity #####", "result = " + returned_result);
                    startActivity(successIntent);*/

                }
                else {
                    throw new IOException("Invalid response from the server! Code: " + responseCode);
                }

                // 2.5.3 Once done all the reading, close the input stream
                input.close();



            } catch (IOException e) {
                e.printStackTrace();

            } finally {

                // 2.x Disconnect
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }
    }

}