package com.example.berrydabest;

import android.content.Intent;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Activity_Profile extends AppCompatActivity {

    private TextView tv_profile_username, tv_profile_userID;
    private Button btn_profile_edit, btn_profile_swap1, btn_profile_swap2;
    private ConstraintLayout layout_profile_content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final Handler handler = new Handler();
        Intent intent_receive = getIntent();

        // findViewById
        layout_profile_content = findViewById(R.id.layout_profile_content);
        tv_profile_username = findViewById(R.id.tv_profile_username);
        tv_profile_userID = findViewById(R.id.tv_profile_email);
        btn_profile_edit = findViewById(R.id.btn_profile_edit);
        btn_profile_swap1 = findViewById(R.id.btn_profile_swap1);
        btn_profile_swap2 = findViewById(R.id.btn_profile_swap2);
        BottomNavigationView navigationView = findViewById(R.id.navigation);

        // Get user details
        // 2.6 Create thread
/*
        MyThread connectThread = new MyThread(et_name.getText().toString(),
                et_ingredients.getText().toString(),
                et_price.getText().toString(),
                handler);
*/
        // Later change to dynamically retrieve user information from Preference of something
        MyThread connectThread = new MyThread("yikhengl@gmail.com", handler);
        connectThread.start();

        // Add code to check if got event. If no, show "no event" text on ConstraintView
        // ...
        // To check if event is upcoming or past:
        // 1. Get event details from database where it is created by current User ID.
        // 2. Compare the date: Bigger date = upcoming; Smaller date = past.

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

        btn_profile_swap1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(Activity_Profile.this, "Showing upcoming events", Toast.LENGTH_SHORT).show();
            }
        });

        btn_profile_swap2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(Activity_Profile.this, "Showing past events", Toast.LENGTH_SHORT).show();
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

    // Create thread
    private class MyThread extends Thread {

        private String email;
        private String username, phone;
        private Handler handler;

        HttpURLConnection urlConnection = null, urlConnection2 = null, urlConnection3 = null;

        // 2.3 Constructor
        public MyThread(String email, Handler handler) {
            this.email = email;
            this.handler = handler;
        }

        @Override
        public void run() {
            try {

                // 1. Access to the Supabase URL
                // A1.1 Table: User
                email = "yikhengl@gmail.com";
                String tableUrl = "https://lqhrxmdxtxyycnftttks.supabase.co/rest/v1/";
                String tableName = "User";
                String tableFilter = "Email=eq." + email;
                String urlString = tableUrl + tableName + "?" + tableFilter;

                // B1.1 Table: Participation
                String tableUrl2 = "https://lqhrxmdxtxyycnftttks.supabase.co/rest/v1/";
                String tableName2 = "Participation";
                String tableFilter2 = "User_Email=eq." + email;
                String urlString2 = tableUrl2 + tableName2 + "?" + tableFilter2;

                URL url = new URL(urlString);
                URL url2 = new URL(urlString2);

                Log.i("##### DEBUG #####", "url: " + url.toString());
                Log.i("##### DEBUG #####", "url 2: " + url2.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection2 = (HttpURLConnection) url2.openConnection();

                // 2. API: Follow the format in Supabase (API Docs, Project Settings)
                urlConnection.setRequestProperty("apiKey", getString(R.string.SUPABASE_KEY));
                urlConnection.setRequestProperty("Authorization", "Bearer " + getString(R.string.SUPABASE_KEY));
                urlConnection2.setRequestProperty("apiKey", getString(R.string.SUPABASE_KEY));
                urlConnection2.setRequestProperty("Authorization", "Bearer " + getString(R.string.SUPABASE_KEY));


                // 3. Obtain the status of connection
                int responseCode = urlConnection.getResponseCode();
                int responseCode2 = urlConnection2.getResponseCode();
                Log.i("##### DEBUG #####", "code: " + responseCode);
                Log.i("##### DEBUG #####", "code 2: " + responseCode2);

                // A4. User data
                if (responseCode == 200)
                {
                    // A4. Get response: User data
                    BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    // A5. response -> JSON response
                    String jsonResponse = response.toString();
                    Log.i("##### DEBUG #####", "API Response: " + jsonResponse);

                    // A6. Parse the JSON response to extract the entire row of data
                    try {
                        // A7. JSON response -> JSONArray
                        JSONArray jsonArray = new JSONArray(jsonResponse);

                        if (jsonArray.length() > 0) {

                            // A8. jsonArray[i]
                            // Data of row[i]:
                            JSONObject jsonObject = jsonArray.getJSONObject(0);
//                            Log.i("##### DEBUG #####", "jsonObject: " + jsonObject);

                            String username = jsonObject.optString("Username");
                            String email = jsonObject.optString("Email");
                            String phone = jsonObject.optString("Phone");

                            // A9. Update your UI elements (e.g., TextViews) with the retrieved data
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tv_profile_username.setText(username);
                                    tv_profile_userID.setText(email);
                                }
                            });

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                if (responseCode2 == 200)
                {
                    // B4. Get response: User data
                    BufferedReader in2 = new BufferedReader(new InputStreamReader(urlConnection2.getInputStream()));
                    String inputLine2;
                    StringBuilder response2 = new StringBuilder();

                    while ((inputLine2 = in2.readLine()) != null) {
                        response2.append(inputLine2);
                    }
                    in2.close();

                    // B5. response -> JSON response
                    String jsonResponse2 = response2.toString();
                    Log.i("##### DEBUG #####", "API Response 2: " + jsonResponse2);

                    // B6. Parse the JSON response to extract the entire row of data
                    try {
                        // B7. JSON response -> JSONArray
                        JSONArray jsonArray2 = new JSONArray(jsonResponse2);

                        if (jsonArray2.length() > 0) {

                            // B8. jsonArray[i]
                            for (int i = 0; i < jsonArray2.length(); i++) {
                                JSONObject jsonObject2 = jsonArray2.getJSONObject(i);

                                String eventID = jsonObject2.optString("Event_Id");

                                // C1.1 Table: Event
                                String tableUrl3 = "https://lqhrxmdxtxyycnftttks.supabase.co/rest/v1/";
                                String tableName3 = "Event";
                                String tableFilter3 = "Event_Name=eq." + eventID;
                                String urlString3 = tableUrl3 + tableName3 + "?" + tableFilter3;
                                URL url3 = new URL(urlString3);
                                Log.i("##### DEBUG #####", "url 3: " + url3.toString());
                                urlConnection3 = (HttpURLConnection) url3.openConnection();
                                urlConnection3.setRequestProperty("apiKey", getString(R.string.SUPABASE_KEY));
                                urlConnection3.setRequestProperty("Authorization", "Bearer " + getString(R.string.SUPABASE_KEY));
                                int responseCode3 = urlConnection3.getResponseCode();
                                Log.i("##### DEBUG #####", "code 3: " + responseCode3);

                                // C4. Participation data
                                if (responseCode3 == 200)
                                {
                                    // C4. Get response: User data
                                    BufferedReader in3 = new BufferedReader(new InputStreamReader(urlConnection3.getInputStream()));
                                    String inputLine3;
                                    StringBuilder response3 = new StringBuilder();

                                    while ((inputLine3 = in3.readLine()) != null) {
                                        response3.append(inputLine3);
                                    }
                                    in3.close();

                                    // C5. response -> JSON response
                                    String jsonResponse3 = response3.toString();
                                    Log.i("##### DEBUG #####", "API Response 3: " + jsonResponse3);

                                    // C6. Parse the JSON response to extract the entire row of data
                                    try {
                                        // C7. JSON response -> JSONArray
                                        JSONArray jsonArray3 = new JSONArray(jsonResponse3);

                                        if (jsonArray3.length() > 0) {

                                            // C8. jsonArray[i]
/*                                            for (int j = 0; j< jsonArray3.length(); j++) {
                                                JSONObject jsonObject3 = jsonArray3.getJSONObject(j);

                                                String eventName = jsonObject3.optString("Event_Name");
                                                String eventDate = jsonObject3.optString("Event_Date");
                                                String eventDescription = jsonObject3.optString("Event_Description");

                                                // C9. Update your UI elements (e.g., TextViews) with the retrieved data
                                                int finalJ = j;

                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        // Create TextViews for each field
                                                        TextView tvEventName = new TextView(Activity_Profile.this);
                                                        TextView tvEventDate = new TextView(Activity_Profile.this);
                                                        TextView tvEventDescription = new TextView(Activity_Profile.this);

                                                        // Set text for the TextViews
                                                        tvEventName.setText(eventName);
                                                        tvEventDate.setText(eventDate);
                                                        tvEventDescription.setText(eventDescription);

                                                        // Add TextViews to the ConstraintLayout
                                                        layout_profile_content.addView(tvEventName);
                                                        layout_profile_content.addView(tvEventDate);
                                                        layout_profile_content.addView(tvEventDescription);

                                                        // Create layout params for the TextViews
                                                        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                                                                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                                                                ConstraintLayout.LayoutParams.WRAP_CONTENT
                                                        );

                                                        // - Apply constraints to position the TextViews
                                                        if (finalJ > 4) {
                                                            // For TextViews after the first one, set constraints to position them below the previous TextView
                                                            params.topToBottom = layout_profile_content.getChildAt(finalJ - 3).getId(); // Subtract 3 because you're adding 3 TextViews at each iteration
                                                        }

                                                        // Apply the layout params to the TextView
                                                        tvEventName.setLayoutParams(params);
                                                        tvEventDate.setLayoutParams(params);
                                                        tvEventDescription.setLayoutParams(params);
                                                    }
                                                });
                                            }*/

/*
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    for (int j = 0; j < jsonArray3.length(); j++) {
                                                        JSONObject jsonObject3 = null;
                                                        try {
                                                            jsonObject3 = jsonArray3.getJSONObject(j);
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }

                                                        String eventName = jsonObject3.optString("Event_Name");
                                                        String eventDate = jsonObject3.optString("Event_Date");
                                                        String eventDescription = jsonObject3.optString("Event_Description");

                                                        // Create TextViews for each field
                                                        TextView tvEventName = new TextView(Activity_Profile.this);
                                                        TextView tvEventDate = new TextView(Activity_Profile.this);
                                                        TextView tvEventDescription = new TextView(Activity_Profile.this);

                                                        // Set text for the TextViews
                                                        tvEventName.setText(eventName);
                                                        tvEventDate.setText(eventDate);
                                                        tvEventDescription.setText(eventDescription);

                                                        // Create layout params for the TextViews
                                                        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                                                                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                                                                ConstraintLayout.LayoutParams.WRAP_CONTENT
                                                        );

                                                        // Apply constraints to position the TextViews
                                                        if (j > 0) {
                                                            // For TextViews after the first one, set constraints to position them below the previous TextView
                                                            params.topToBottom = layout_profile_content.getChildAt((j - 1) * 3).getId(); // Multiply by 3 because you're adding 3 TextViews at each iteration
                                                        }

                                                        // Add TextViews to the ConstraintLayout
                                                        layout_profile_content.addView(tvEventName);
                                                        layout_profile_content.addView(tvEventDate);
                                                        layout_profile_content.addView(tvEventDescription);

                                                        // Apply the layout params to the TextViews
                                                        tvEventName.setLayoutParams(params);
                                                        tvEventDate.setLayoutParams(params);
                                                        tvEventDescription.setLayoutParams(params);
                                                    }
                                                }
                                            });
*/

// Initialize a variable to keep track of the previous ConstraintLayout
                                            ConstraintLayout previousLayout = null;
                                            int yOffset = 0; // Initialize the Y-offset

                                            for (int j = 0; j < jsonArray3.length(); j++) {
                                                JSONObject jsonObject3 = null;
                                                try {
                                                    jsonObject3 = jsonArray3.getJSONObject(j);
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }

                                                // Create a new ConstraintLayout
                                                ConstraintLayout constraintLayout = new ConstraintLayout(Activity_Profile.this);
                                                constraintLayout.setId(View.generateViewId()); // Generate a unique ID for each ConstraintLayout

                                                // Calculate the new Y-coordinate for this ConstraintLayout
                                                int newY = yOffset + (j * 20);

                                                // Set the Y-coordinate for the ConstraintLayout
                                                ConstraintLayout.LayoutParams constraintLayoutParams = new ConstraintLayout.LayoutParams(
                                                        ConstraintLayout.LayoutParams.MATCH_PARENT,
                                                        ConstraintLayout.LayoutParams.WRAP_CONTENT
                                                );
                                                constraintLayoutParams.topMargin = newY;
                                                constraintLayout.setLayoutParams(constraintLayoutParams);

                                                String eventName = jsonObject3.optString("Event_Name");
                                                String eventDate = jsonObject3.optString("Event_Date");
                                                String eventDescription = jsonObject3.optString("Event_Description");

                                                // Create TextViews for each field
                                                TextView tvEventName = new TextView(Activity_Profile.this);
                                                TextView tvEventDate = new TextView(Activity_Profile.this);
                                                TextView tvEventDescription = new TextView(Activity_Profile.this);

                                                // Set text for the TextViews
                                                tvEventName.setText(eventName);
                                                tvEventDate.setText(eventDate);
                                                tvEventDescription.setText(eventDescription);

                                                // Add TextViews to the ConstraintLayout
                                                constraintLayout.addView(tvEventName);
                                                constraintLayout.addView(tvEventDate);
                                                constraintLayout.addView(tvEventDescription);

                                                // Add the ConstraintLayout to the parent ConstraintLayout
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        layout_profile_content.addView(constraintLayout);
                                                    }
                                                });

                                                // Update the previousLayout to the current ConstraintLayout for the next iteration
                                                previousLayout = constraintLayout;
                                            }

                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }


                                // Disconnect
                                if (urlConnection3 != null) {
                                    urlConnection3.disconnect();
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }



            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // 2.x Disconnect
                if (urlConnection3 != null) {
                    urlConnection3.disconnect();
                }
            }
        }
    }

    private String readStream (InputStream is)
    {
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            int i = is.read();

            while (i != -1) {
                bo.write(i);
                i = is.read();
            }

            return bo.toString();

        } catch (IOException e) {
            return "";
        }
    }
}