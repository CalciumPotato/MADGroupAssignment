package com.example.berrydabest;

import static com.example.berrydabest.Activity_Profile_Tools.readPreference;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Activity_Profile extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final Handler handler = new Handler();
        String email = readPreference(this, "Email", "notFound");
        Intent intent_receive = getIntent();

        // findViewById
        Button btn_profile_swap1 = findViewById(R.id.btn_profile_swap1);
        LinearLayout ll = findViewById(R.id.layout_profile_content);
        TextView tv_profile_username = findViewById(R.id.tv_profile_username);
        TextView tv_profile_userID = findViewById(R.id.tv_profile_email);
        Button btn_profile_edit = findViewById(R.id.btn_profile_edit);
        Button btn_profile_swap2 = findViewById(R.id.btn_profile_swap2);
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

        // Button: Upcoming events
        btn_profile_swap1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(Activity_Profile.this, "Showing upcoming events", Toast.LENGTH_SHORT).show();
                new Thread() {
                    public void run() {
                        try {
                            // Connect to database
                            URL url = new URL("https://lqhrxmdxtxyycnftttks.supabase.co/rest/v1/Event?select=*&Email=eq." + email);
                            HttpURLConnection hc = (HttpURLConnection) url.openConnection();
                            hc.setRequestProperty("apikey", getString(R.string.SUPABASE_KEY));
                            hc.setRequestProperty("Authorization", "Bearer "+getString(R.string.SUPABASE_KEY));

                            InputStream input = new BufferedInputStream((hc.getInputStream()));
                            String result = Activity_Profile_Tools.readStream(input);

                            // Parse JSON
                            JSONArray jarray = new JSONArray(result);
                            JSONArray filteredArray = new JSONArray();
                            for(int i = 0; i < jarray.length(); i++){
                                JSONObject jsonObject = jarray.getJSONObject(i);
                                String date = jsonObject.getString("Event_Date");
                                if(Activity_Profile_Tools.CompareTwoDates(date)){
                                    filteredArray.put(jsonObject);
                                }
                            }

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        ll.removeAllViews();    // Clear content

                                        for(int j = 0; j < filteredArray.length(); j++){
                                            LinearLayout layout = new LinearLayout(Activity_Profile.this);
                                            LinearLayout innerLayout = new LinearLayout(Activity_Profile.this);
                                            LinearLayout.LayoutParams inParams = Activity_Profile_Tools.formatLayout(layout, innerLayout);
                                            innerLayout.setLayoutParams(inParams);
/*                                            layout.setOrientation(LinearLayout.HORIZONTAL);
                                            LinearLayout.LayoutParams linearParams = new LinearLayout.LayoutParams(
                                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                                    LinearLayout.LayoutParams.MATCH_PARENT
                                            );
                                            linearParams.setMargins(0, 10,0,10);
                                            layout.setPadding(20, 20, 20, 20);
                                            layout.setLayoutParams(linearParams);
                                            layout.setBackgroundResource(R.drawable.border);

                                            LinearLayout.LayoutParams inParams = new LinearLayout.LayoutParams(
                                                    0,
                                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                            );
                                            inParams.weight = 2;*/

                                            String eventName = filteredArray.getJSONObject(j).getString("Event_Name");
                                            new Thread(){
                                                public void run(){
                                                    try{
                                                        URL url = new URL("https://lqhrxmdxtxyycnftttks.supabase.co/storage/v1/object/image/"+eventName+".jpg");
                                                        HttpURLConnection hc = null;
                                                        hc = (HttpURLConnection) url.openConnection();
                                                        hc.setRequestProperty("Authorization", "Bearer " + getString(R.string.SUPABASE_KEY));
                                                        hc.setRequestProperty("Content-Type", "image/jpeg");
                                                        InputStream input = new BufferedInputStream((hc.getInputStream()));
                                                        Bitmap bm = BitmapFactory.decodeStream(input);
                                                        handler.post(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(
                                                                        0,
                                                                        LinearLayout.LayoutParams.MATCH_PARENT
                                                                );
                                                                imgParams.weight = 1;
                                                                ImageView img = new ImageView(Activity_Profile.this);
                                                                img.setImageBitmap(bm);
                                                                img.setLayoutParams(imgParams);
                                                                layout.addView(img, 0);
                                                            }
                                                        });
                                                    }
                                                    catch(IOException e){
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }.start();


                                            TextView name = new TextView(Activity_Profile.this);
                                            name.setText(filteredArray.getJSONObject(j).getString("Event_Name"));
                                            name.setTextColor(Color.parseColor("#FFFFFF"));
                                            TextView desc = new TextView(Activity_Profile.this);
                                            desc.setText(filteredArray.getJSONObject(j).getString("Event_Venue"));
                                            desc.setTextColor(Color.parseColor("#FFFFFF"));

                                            innerLayout.addView(name);
                                            innerLayout.addView(desc);
                                            layout.addView(innerLayout);

                                            layout.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    String eventName = ((TextView)((ViewGroup)((ViewGroup)((ViewGroup) view).getChildAt(1))).getChildAt(0)).getText().toString();
                                                    Intent i = new Intent(Activity_Profile.this, EventAnalytic.class);
                                                    i.putExtra("EventName", eventName);
                                                    startActivity(i);
                                                }
                                            });

                                            ll.addView(layout);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
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

/*        @Override
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

                                            ConstraintLayout previousConstraintLayout = null; // To keep track of the previous ConstraintLayout

                                            // C8. jsonArray[i]
                                            for (int j = 0; j< jsonArray3.length(); j++) {
                                                JSONObject jsonObject3 = jsonArray3.getJSONObject(j);

                                                String eventName = jsonObject3.optString("Event_Name");
                                                String eventDate = jsonObject3.optString("Event_Date");
                                                String eventDescription = jsonObject3.optString("Event_Description");

                                                // Assuming you have a parent ConstraintLayout with id "layout_profile_content"
                                                ConstraintLayout parentLayout = findViewById(R.id.layout_profile_content);


                                                // Create a new ConstraintLayout
                                                ConstraintLayout constraintLayout = new ConstraintLayout(Activity_Profile.this);
                                                constraintLayout.setId(View.generateViewId());
                                                constraintLayout.setBackgroundColor(getResources().getColor(R.color.purple_200)); // Set the background color

                                                // Create an ImageView
                                                ImageView imageView = new ImageView(Activity_Profile.this);
                                                imageView.setId(View.generateViewId());
                                                imageView.setImageResource(R.drawable.img_profile_picture); // Set the image resource

                                                // Create TextView for event name
                                                TextView tvEventName = new TextView(Activity_Profile.this);
                                                tvEventName.setId(View.generateViewId());
                                                tvEventName.setText(eventName);
                                                tvEventName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20); // Set text size
                                                tvEventName.setTypeface(tvEventName.getTypeface(), Typeface.BOLD); // Set text style

                                                // Create TextView for event date
                                                TextView tvEventDate = new TextView(Activity_Profile.this);
                                                tvEventDate.setId(View.generateViewId());
                                                tvEventDate.setText(eventDate);

                                                // Create TextView for event description
                                                TextView tvEventDesc = new TextView(Activity_Profile.this);
                                                tvEventDesc.setId(View.generateViewId());
                                                tvEventDesc.setText(eventDescription);

                                                // Add the ImageView and TextViews to the ConstraintLayout
                                                constraintLayout.addView(imageView);
                                                constraintLayout.addView(tvEventName);
                                                constraintLayout.addView(tvEventDate);
                                                constraintLayout.addView(tvEventDesc);

                                                // Set constraints for the views within the ConstraintLayout
                                                ConstraintSet constraintSet = new ConstraintSet();
                                                constraintSet.clone(constraintLayout);


                                                // For the first ConstraintLayout, constrain it to the top of the parent ConstraintLayout
                                                if (previousConstraintLayout == null) {
                                                    constraintSet.connect(constraintLayout.getId(), ConstraintSet.TOP, parentLayout.getId(), ConstraintSet.TOP);

                                                } else {
                                                    // Constrain the top of the ConstraintLayout to the bottom of the previous ConstraintLayout (if it's not the first one)
                                                    constraintSet.connect(constraintLayout.getId(), ConstraintSet.TOP, previousConstraintLayout.getId(), ConstraintSet.BOTTOM);
                                                }

                                                constraintSet.connect(imageView.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
                                                constraintSet.connect(imageView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
                                                constraintSet.connect(imageView.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
                                                constraintSet.constrainWidth(imageView.getId(), ConstraintSet.WRAP_CONTENT);
                                                constraintSet.constrainHeight(imageView.getId(), ConstraintSet.WRAP_CONTENT);

                                                constraintSet.connect(tvEventName.getId(), ConstraintSet.START, imageView.getId(), ConstraintSet.END);
                                                constraintSet.connect(tvEventName.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
                                                constraintSet.connect(tvEventName.getId(), ConstraintSet.TOP, imageView.getId(), ConstraintSet.TOP);
                                                constraintSet.setHorizontalBias(tvEventName.getId(), 0.0f);
                                                constraintSet.constrainWidth(tvEventName.getId(), ConstraintSet.WRAP_CONTENT); // Match constraints
                                                constraintSet.constrainHeight(tvEventName.getId(), ConstraintSet.WRAP_CONTENT);

                                                constraintSet.connect(tvEventDate.getId(), ConstraintSet.START, imageView.getId(), ConstraintSet.END);
                                                constraintSet.connect(tvEventDate.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
                                                constraintSet.connect(tvEventDate.getId(), ConstraintSet.TOP, tvEventName.getId(), ConstraintSet.BOTTOM);
                                                constraintSet.setHorizontalBias(tvEventDate.getId(), 0.0f);
                                                constraintSet.constrainWidth(tvEventDate.getId(), ConstraintSet.WRAP_CONTENT); // Match constraints
                                                constraintSet.constrainHeight(tvEventDate.getId(), ConstraintSet.WRAP_CONTENT);

                                                constraintSet.connect(tvEventDesc.getId(), ConstraintSet.START, imageView.getId(), ConstraintSet.END);
                                                constraintSet.connect(tvEventDesc.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
                                                constraintSet.connect(tvEventDesc.getId(), ConstraintSet.TOP, tvEventDate.getId(), ConstraintSet.BOTTOM);
                                                constraintSet.connect(tvEventDesc.getId(), ConstraintSet.BOTTOM, imageView.getId(), ConstraintSet.BOTTOM);
                                                constraintSet.constrainWidth(tvEventDesc.getId(), 0); // Match constraints
                                                constraintSet.constrainHeight(tvEventDesc.getId(), 0); // Match constraints

                                                // Create layout params for the ConstraintLayout
                                                ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(
                                                        ConstraintLayout.LayoutParams.MATCH_PARENT, // Match the parent's width
                                                        ConstraintLayout.LayoutParams.WRAP_CONTENT // Adjust height as needed
                                                );

                                                int marginInPixels = (int) TypedValue.applyDimension(
                                                        TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()
                                                );

                                                layoutParams.setMargins(marginInPixels, marginInPixels, marginInPixels, marginInPixels);

                                                constraintLayout.setLayoutParams(layoutParams);

                                                int paddingInPixels = (int) TypedValue.applyDimension(
                                                        TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()
                                                );

                                                // Apply padding to the ConstraintLayout
                                                constraintLayout.setPadding(paddingInPixels, paddingInPixels, paddingInPixels, paddingInPixels);

                                                constraintSet.applyTo(constraintLayout);

                                                // Add the ConstraintLayout to the parent ConstraintLayout
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        parentLayout.addView(constraintLayout);
                                                    }
                                                });

                                                // Update the previous ConstraintLayout to the current one for the next iteration
                                                previousConstraintLayout = constraintLayout;

                                            }

*/
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
*//*

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
    }*/

    }
}