package com.example.berrydabest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class EventDetail extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        // Change status bar colour
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.darker_grey));
        }

        String eventName = this.getIntent().getStringExtra("EventName");

        String email = readPreference(this, "Email", "notFound");

        // back button
        ImageView backBtn = findViewById(R.id.img_back);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        BottomNavigationView navigationView = findViewById(R.id.navigation);

        navigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    return true;
                case R.id.navigation_calendar:
                    // Handle dashboard navigation
                    startActivity(new Intent(EventDetail.this, CalendarActivity.class));
                    return true;
                case R.id.navigation_qrScanner:
                    // Handle notifications navigation
                    Intent intent = new Intent(this, QR_Scan.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.right, R.anim.left);
                    Toast.makeText(EventDetail.this, "QR Scanner", Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.navigation_myEvent:
                    // Handle notifications navigation
                    startActivity(new Intent(EventDetail.this, MyEvent.class));
                    return true;
            }
            return false;
        });


        Handler handler = new Handler();

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(){
                    public void run(){
                        try {
                            URL url = new URL("https://lqhrxmdxtxyycnftttks.supabase.co/rest/v1/Participation");
                            HttpURLConnection hc = null;
                            hc = (HttpURLConnection) url.openConnection();
                            hc.setRequestProperty("apikey", getString(R.string.SUPABASE_KEY));
                            hc.setRequestProperty("Authorization", "Bearer "+getString(R.string.SUPABASE_KEY));
                            hc.setRequestProperty("Content-Type", "application/json");
                            hc.setRequestProperty("Prefer", "return=minimal");
                            hc.setRequestMethod("POST");
                            hc.setDoOutput(true);

                            OutputStream output = hc.getOutputStream();

                            TextView name = findViewById(R.id.title);

                            JSONObject json = new JSONObject();
                            json.put("Event_Id", name.getText());
                            json.put("User_Email", email);
                            json.put("Attendance", "FALSE");
                            output.write(json.toString().getBytes());
                            output.flush();

                            InputStream input = hc.getInputStream();
                            String result = readStream(input);

                            if(hc.getResponseCode() == 201){
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), "Registration Succeed!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(EventDetail.this, MainPage.class));
                                    }
                                });
                            }
                            else{
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), "Registration Failed!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }.start();
            }
        };

        new Thread(){
            public void run(){
                try{
                    URL url = new URL("https://lqhrxmdxtxyycnftttks.supabase.co/rest/v1/Participation?select=*&Event_Id=eq."+eventName+"&User_Email=eq."+email);
                    HttpURLConnection hc = null;
                    hc = (HttpURLConnection) url.openConnection();
                    hc.setRequestProperty("apikey", getString(R.string.SUPABASE_KEY));
                    hc.setRequestProperty("Authorization", "Bearer "+getString(R.string.SUPABASE_KEY));

                    InputStream input = new BufferedInputStream((hc.getInputStream()));
                    String result = readStream(input);

                    JSONArray jarray = new JSONArray(result);
                    // if not participate
                    if(jarray.length() == 0){
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                LinearLayout ll = findViewById(R.id.eventDetail);
                                Button btn = new Button(EventDetail.this);
                                btn.setText("Register");
                                LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                );
                                btn.setLayoutParams(btnParams);
                                ll.addView(btn, -1);
                                btn.setOnClickListener(listener);
                            }
                        });
                    }
                    else{
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                LinearLayout ll = findViewById(R.id.eventDetail);
                                TextView text = new TextView(EventDetail.this);
                                text.setText("Scan QR to Check In");
                                text.setTextColor(Color.parseColor("#FFFFFF"));
                                text.setTextSize(30);
                                text.setGravity(Gravity.CENTER);
                                text.setPadding(10,10 ,10 ,10);
                                ll.addView(text, -1);
                            }
                        });
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }.start();
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
                            ImageView img = findViewById(R.id.imageView3);
                            img.setImageBitmap(bm);
                        }
                    });
                }
                catch(IOException e){
                    e.printStackTrace();
                }
            }
        }.start();

        new Thread(){
            public void run(){
                try {
                    URL url = new URL("https://lqhrxmdxtxyycnftttks.supabase.co/rest/v1/Event?select=*&Event_Name=eq."+eventName);
                    HttpURLConnection hc = null;
                    hc = (HttpURLConnection) url.openConnection();
                    hc.setRequestProperty("apikey", getString(R.string.SUPABASE_KEY));
                    hc.setRequestProperty("Authorization", "Bearer "+getString(R.string.SUPABASE_KEY));

                    InputStream input = new BufferedInputStream((hc.getInputStream()));
                    String result = readStream(input);

                    JSONObject json = (new JSONArray(result)).getJSONObject(0);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                TextView name = findViewById(R.id.title);
                                name.setText((json.getString("Event_Name")));
                                TextView desc = findViewById(R.id.desc);
                                desc.setText((json.getString("Event_Description")));
                                TextView date = findViewById(R.id.date);
                                date.setText((json.getString("Event_Date")));
                                TextView venue = findViewById(R.id.venue);
                                venue.setText((json.getString("Event_Venue")));
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

        new Thread(){
            public void run(){

            }
        }.start();

    }

    private String readStream(InputStream is) {
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

    // read email from preference file
    public static String readPreference(Context context, String key, String defaultValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("Secret", Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, defaultValue);
    }
}