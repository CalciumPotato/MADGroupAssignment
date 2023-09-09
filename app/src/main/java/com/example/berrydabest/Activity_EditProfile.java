package com.example.berrydabest;

import static com.example.berrydabest.Activity_Profile_Tools.getResponse;
import static com.example.berrydabest.Activity_Profile_Tools.readPreference;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;

public class Activity_EditProfile extends AppCompatActivity {

    private ImageView img_back_editProfile;
    private TextView tv_editProfile_email;
    private EditText et_editProfile_name, et_editProfile_password, et_editProfile_phone;
    private Button btn_editProfile_save;

    final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        String email = readPreference(this, "Email", "");
        Log.i("##### DEBUG #####", "email: " + email);

        // findViewById
        img_back_editProfile = findViewById(R.id.img_back_editProfile);
        tv_editProfile_email = findViewById(R.id.tv_editProfile_email);
        et_editProfile_name = findViewById(R.id.et_editProfile_name);
        et_editProfile_password = findViewById(R.id.et_editProfile_password);
        et_editProfile_phone = findViewById(R.id.et_editProfile_phone);
        btn_editProfile_save = findViewById(R.id.btn_editProfile_save);

        MyThread getUserInfoThread = new MyThread(email, handler);
        getUserInfoThread.start();

        // Listener
        img_back_editProfile.setOnClickListener(view -> finish());

        btn_editProfile_save.setOnClickListener(view -> {
            // Create thread
            MyThread2 connectThread = new MyThread2(email,
                    et_editProfile_password.getText().toString(),
                    et_editProfile_name.getText().toString(),
                    et_editProfile_phone.getText().toString(),
                    handler);
            connectThread.start();
        });

    }

    // Create thread 1: Get user details
    private class MyThread extends Thread {
        private String email, password, username, phone;
        private Handler handler;

        HttpURLConnection urlConnection = null;

        // Constructor
        public MyThread(String email, Handler handler) {
            this.email = email;
            this.handler = handler;
        }

        @Override
        public void run() {
            try {
                // A1. Get user info to fill into the EditText
                urlConnection = Activity_Profile_Tools.connectSupabaseUser(email, getString(R.string.SUPABASE_KEY), getString(R.string.SUPABASE_KEY));

                // A2. Obtain the status of connection
                // 200: Connected
                int responseCode = urlConnection.getResponseCode();
                Log.i("##### DEBUG #####", "code: " + responseCode);

                if (responseCode == 200)
                {
                    // A3. Get and build input
                    StringBuilder response = getResponse(urlConnection);
                    String jsonResponse = response.toString();
                    Log.i("##### DEBUG #####", "API Response: " + jsonResponse);

                    // A4. Parse the JSON response to extract the entire row of data
                    try {
                        // A4. JSON response -> JSONArray
                        JSONArray jsonArray = new JSONArray(jsonResponse);

                        // If jsonArray is not empty:
                        if (jsonArray.length() > 0) {

                            // jsonArray[i]
                            // Data of row[i]:
                            JSONObject jsonObject = jsonArray.getJSONObject(0);
                            Log.i("##### DEBUG #####", "jsonObject: " + jsonObject);

                            // A6. Get data from jsonObject to our variable
                            username = jsonObject.getString("Username");
                            password = jsonObject.getString("Password");
                            email = jsonObject.getString("Email");
                            phone = jsonObject.getString("Phone");

                            // A7. Update UI elements with the retrieved data
                            runOnUiThread(() -> {
                                et_editProfile_name.setText(username);
                                et_editProfile_password.setText(password);
                                tv_editProfile_email.setText(email);
                                et_editProfile_phone.setText(phone);
                            });

                        }
                    } catch (JSONException jsonException) {
                        jsonException.printStackTrace();
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Create thread 2: Update user details in Supabase
    private class MyThread2 extends Thread {

        private String email, password, username, phone;
        private Handler handler;

        HttpURLConnection urlConnection = null;

        // 2.3 Constructor
        public MyThread2(String email, String password, String username, String phone, Handler handler) {
            this.email = email;
            this.password = password;
            this.username = username;
            this.phone = phone;
            this.handler = handler;
        }

        @Override
        public void run() {
            try {
                // Create a new instance of a JSONObject
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("Password", password);
                jsonObject.put("Username", username);
                jsonObject.put("Phone", phone);

                // B1. Connect to table User
                urlConnection = Activity_Profile_Tools.updateSupabaseUser(email, getString(R.string.SUPABASE_KEY), getString(R.string.SUPABASE_KEY));

                // 4.4 Writing to the table in Supabase
                OutputStream output = urlConnection.getOutputStream();
                output.write(jsonObject.toString().getBytes());
                output.flush();

                // B2. Obtain the status of connection
                int responseCode = urlConnection.getResponseCode();
                Log.i("##### DEBUG #####", "code: " + responseCode);

                // responseCode: Get user data
                if (responseCode == 204)
                {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Activity_EditProfile.this, "User info updated! Reload the page to see changes.", Toast.LENGTH_SHORT).show();
                        }
                    });

                    finish();
                }
                else {
                    throw new IOException("Invalid response from the server! Code: " + responseCode);
                }

            } catch (IOException | JSONException e) {
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