package com.example.berrydabest;

/*
* Note:
* Some code of this Activity Activity_Profile are refactored to Activity_Profile_Tools to improve code readability and possibly code reuse.
* */

import static com.example.berrydabest.Activity_Profile_Tools.readPreference;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Activity_Profile extends AppCompatActivity {

    private LinearLayout eventContent;
    private TextView tv_profile_username, tv_profile_userID;
    private Button btn_profile_edit, btn_profile_swap1, btn_profile_swap2;
    private BottomNavigationView navigationView;
    private Boolean btn_upcoming = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final Handler handler = new Handler();
        String email = readPreference(this, "Email", "");

        // findViewById
        tv_profile_username = findViewById(R.id.tv_profile_username);
        tv_profile_userID = findViewById(R.id.tv_profile_email);
        btn_profile_edit = findViewById(R.id.btn_profile_edit);
        btn_profile_swap1 = findViewById(R.id.btn_profile_swap1);
        btn_profile_swap2 = findViewById(R.id.btn_profile_swap2);
        eventContent = findViewById(R.id.layout_profile_content);
        navigationView = findViewById(R.id.navigation);

//        MyThread connectThread = new MyThread("yikhengl@gmail.com", handler);
//        connectThread.start();
        Activity_Profile.MyThread connectThread = new Activity_Profile.MyThread(email, handler);
        connectThread.start();

        btn_upcoming = true;
        btn_profile_swap1.setBackgroundColor(btn_profile_swap1.getContext().getResources().getColor(R.color.btn_selected));
        btn_profile_swap2.setBackgroundColor(btn_profile_swap2.getContext().getResources().getColor(R.color.btn_deselected));

        // Listeners
        // Go to Edit Profile page
        btn_profile_edit.setOnClickListener(view -> {
            try {
                Intent k = new Intent(Activity_Profile.this, Activity_EditProfile.class);
                Log.i("##### DEBUG #####", "email: " + email);
                startActivity(k);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Button: Upcoming events
        btn_profile_swap1.setOnClickListener(view -> {
            Toast.makeText(Activity_Profile.this, "Showing upcoming participated events", Toast.LENGTH_SHORT).show();
            btn_upcoming = true;
            btn_profile_swap1.setBackgroundColor(btn_profile_swap1.getContext().getResources().getColor(R.color.btn_selected));
            btn_profile_swap2.setBackgroundColor(btn_profile_swap2.getContext().getResources().getColor(R.color.btn_deselected));
            // Create thread
            MyThread connectThread1 = new MyThread(email, handler);
            connectThread1.start();
        });

        btn_profile_swap2.setOnClickListener(view -> {
            Toast.makeText(Activity_Profile.this, "Showing past participated events", Toast.LENGTH_SHORT).show();
            btn_upcoming = false;
            btn_profile_swap1.setBackgroundColor(btn_profile_swap1.getContext().getResources().getColor(R.color.btn_deselected));
            btn_profile_swap2.setBackgroundColor(btn_profile_swap2.getContext().getResources().getColor(R.color.btn_selected));
            // Create thread
            MyThread connectThread12 = new MyThread(email, handler);
            connectThread12.start();
        });


        // Bottom navigation bar
        navigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    startActivity(new Intent(this, Activity_Profile.class));
                    Toast.makeText(Activity_Profile.this, "Home", Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.navigation_calendar:
                    // Handle dashboard navigation
                    Toast.makeText(Activity_Profile.this, "Calendar", Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.navigation_qrScanner:
                    // Handle notifications navigation
                    Intent intent = new Intent(this, QR_Scan.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.right, R.anim.left);
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

        String email;
        String username, phone;
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
                // 1. Open connection
                // connectSupabaseUser(String email, String apiKey, String Authorization)
                // connectSupabaseParticipation(String email, String apiKey, String Authorization)
                urlConnection = Activity_Profile_Tools.connectSupabaseUser(email, getString(R.string.SUPABASE_KEY), getString(R.string.SUPABASE_KEY));
                urlConnection2 = Activity_Profile_Tools.connectSupabaseParticipation(email, getString(R.string.SUPABASE_KEY), getString(R.string.SUPABASE_KEY));

                // 3. Obtain the status of connection
                int responseCode = urlConnection.getResponseCode();
                int responseCode2 = urlConnection2.getResponseCode();
                Log.i("##### DEBUG #####", "code: " + responseCode);
                Log.i("##### DEBUG #####", "code 2: " + responseCode2);

                // A4. User data
                if (responseCode == 200) {
                    // A4. Get response: User data
                    // A5. response -> JSON response
                    // getResponse(HttpURLConnection urlConnection)
                    String jsonResponse = Activity_Profile_Tools.readStream(urlConnection.getInputStream());
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

                            username = jsonObject.getString("Username");
                            email = jsonObject.getString("Email");
                            phone = jsonObject.getString("Phone");

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

                // B4. User participation data
                if (responseCode2 == 200) {
                    // B4. Get response: User participation data
                    // B5. response -> JSON response
                    // getResponse(HttpURLConnection urlConnection)
                    String jsonResponse2 = Activity_Profile_Tools.readStream(urlConnection2.getInputStream());
                    Log.i("##### DEBUG #####", "API Response 2: " + jsonResponse2);

                    // B6. Parse the JSON response to extract the entire row of data
                    try {
                        // B7. JSON response -> JSONArray
                        JSONArray jsonArray2 = new JSONArray(jsonResponse2);

                        if (jsonArray2.length() > 0) {

                            // B8. jsonArray[i]
                            JSONArray filteredArray = new JSONArray();
                            for (int i = 0; i < jsonArray2.length(); i++) {
                                JSONObject jsonObject2 = jsonArray2.getJSONObject(i);

                                String eventID = jsonObject2.getString("Event_Id");

                                // connectSupabaseEvent(String email, String eventID, String apiKey, String Authorization)
                                urlConnection3 = Activity_Profile_Tools.connectSupabaseEvent(email, eventID, getString(R.string.SUPABASE_KEY), getString(R.string.SUPABASE_KEY));

                                int responseCode3 = urlConnection3.getResponseCode();
                                Log.i("##### DEBUG #####", "code 3: " + responseCode3);

                                // C4. Participation data
                                if (responseCode3 == 200) {
                                    // C4. Get response: User data

                                    String result = Activity_Profile_Tools.readStream(urlConnection3.getInputStream());
                                    Log.i("##### DEBUG #####", "result: " + result);

                                    // C5. Process returned data
                                    try {
                                        JSONArray jsonArray3 = new JSONArray(result);


                                        if (jsonArray3.length() > 0) {

                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    // Clear content
                                                    eventContent.removeAllViews();
                                                }
                                            });

                                            // C6. JSON response -> JSONArray
                                            for (int j = 0; j < jsonArray3.length(); j++) {
                                                JSONObject jsonObject = jsonArray3.getJSONObject(j);
                                                String date = jsonObject.getString("Event_Date");

                                                if(btn_upcoming) {
                                                    if (Activity_Profile_Tools.CompareTwoDates(date)) {
                                                        filteredArray.put(jsonObject);
                                                        Log.i("##### DEBUG #####", "filteredArray:" + filteredArray);
                                                    }
                                                }
                                                else {
                                                    if (!Activity_Profile_Tools.CompareTwoDates(date)) {
                                                        filteredArray.put(jsonObject);
                                                        Log.i("##### DEBUG #####", "filteredArray:" + filteredArray);
                                                    }
                                                }

                                                handler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        try {

                                                            for(int k = 0; k < filteredArray.length(); k++){

                                                                // Create the parent LinearLayout: To hold event image + event details
                                                                // Create the inner LinearLayout for event details: To hold event name, event date, event desc
                                                                LinearLayout eventCard = Activity_Profile_Tools.createEventCard(Activity_Profile.this);
                                                                LinearLayout eventDetails = Activity_Profile_Tools.createEventDetails(Activity_Profile.this);

                                                                String eventName = filteredArray.getJSONObject(k).getString("Event_Name");
                                                                new Thread(){
                                                                    public void run(){
                                                                        try{
                                                                            // Get event image
                                                                            HttpURLConnection hc = Activity_Profile_Tools.connectSupabaseImage(eventName, getString(R.string.SUPABASE_KEY));
                                                                            InputStream input = new BufferedInputStream((hc.getInputStream()));
                                                                            Bitmap bm = BitmapFactory.decodeStream(input);

                                                                            ImageView img = Activity_Profile_Tools.createImg(Activity_Profile.this, bm);
                                                                            // Add event image
                                                                            handler.post(new Runnable() {
                                                                                @Override
                                                                                public void run() {

                                                                                    eventCard.addView(img, 0);
                                                                                }
                                                                            });
                                                                        }
                                                                        catch(IOException e){
                                                                            e.printStackTrace();
                                                                        }
                                                                    }
                                                                }.start();

                                                                // Add TextView
                                                                TextView event_name = Activity_Profile_Tools.createEventName(Activity_Profile.this, filteredArray, k);
                                                                TextView event_date = Activity_Profile_Tools.createEventDate(Activity_Profile.this, filteredArray, k);
                                                                TextView event_desc = Activity_Profile_Tools.createEventDesc(Activity_Profile.this, filteredArray, k);

                                                                // Add ImageView and TextViews to the inner LinearLayout
                                                                eventDetails.addView(event_name);
                                                                eventDetails.addView(event_date);
                                                                eventDetails.addView(event_desc);

                                                                // Add the inner LinearLayout to the parent LinearLayout
                                                                eventCard.addView(eventDetails);

                                                                eventCard.setOnClickListener(new View.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(View view) {
                                                                        String eventName = ((TextView)((ViewGroup)((ViewGroup)((ViewGroup) view).getChildAt(1))).getChildAt(0)).getText().toString();
                                                                        Intent i = new Intent(Activity_Profile.this, EventAnalytic.class);
                                                                        i.putExtra("EventName", eventName);
                                                                        startActivity(i);
                                                                    }
                                                                });

                                                                // Add the parent LinearLayout to this custom view
                                                                eventContent.addView(eventCard);
                                                                eventContent.setPadding(8, 8, 8, 8);
                                                            }
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                });

                                            }
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (urlConnection2 != null) {
                    urlConnection2.disconnect();
                }
                if (urlConnection3 != null) {
                    urlConnection3.disconnect();
                }

            }

        }

    }
}