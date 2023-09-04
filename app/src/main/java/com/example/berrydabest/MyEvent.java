package com.example.berrydabest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MyEvent extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_event);

        Handler handler = new Handler();

        String email = readPreference(this, "Email", "notFound");

        LinearLayout ll = findViewById(R.id.eventLayout);
        Button upbtn = findViewById(R.id.upcoming);
        Button pasbtn = findViewById(R.id.past);
        BottomNavigationView navigationView = findViewById(R.id.navigation);

        // Bottom navigation bar
        navigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    startActivity(new Intent(MyEvent.this, MainPage.class));
                    return true;
                case R.id.navigation_calendar:
                    // Handle dashboard navigation
                    startActivity(new Intent(MyEvent.this, CalendarActivity.class));
                    return true;
                case R.id.navigation_qrScanner:
                    // Handle notifications navigation
                    Intent intent = new Intent(this, QR_Scan.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.right, R.anim.left);
                    Toast.makeText(MyEvent.this, "QR Scanner", Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.navigation_myEvent:
                    // Handle notifications navigation
                    return true;
            }
            return false;
        });

        upbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread() {
                    public void run() {
                        try {
                            URL url = new URL("https://lqhrxmdxtxyycnftttks.supabase.co/rest/v1/Event?select=*&Email=eq."+email);
                            HttpURLConnection hc = (HttpURLConnection) url.openConnection();
                            hc.setRequestProperty("apikey", getString(R.string.SUPABASE_KEY));
                            hc.setRequestProperty("Authorization", "Bearer "+getString(R.string.SUPABASE_KEY));

                            InputStream input = new BufferedInputStream((hc.getInputStream()));
                            String result = readStream(input);

                            JSONArray jarray = new JSONArray(result);
                            JSONArray filteredArray = new JSONArray();
                            for(int i = 0; i < jarray.length(); i++){
                                JSONObject jsonObject = jarray.getJSONObject(i);
                                String date = jsonObject.getString("Event_Date");
                                if(CompareTwoDates(date)){
                                    filteredArray.put(jsonObject);
                                }
                            }
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        ll.removeAllViews();
                                        for(int j = 0; j < filteredArray.length(); j++){
                                            LinearLayout layout = new LinearLayout(MyEvent.this);
                                            layout.setOrientation(LinearLayout.HORIZONTAL);
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
                                            inParams.weight = 2;
                                            LinearLayout innerLayout = new LinearLayout(MyEvent.this);
                                            innerLayout.setOrientation(LinearLayout.VERTICAL);
                                            innerLayout.setLayoutParams(inParams);


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
                                                                ImageView img = new ImageView(MyEvent.this);
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


                                            TextView name = new TextView(MyEvent.this);
                                            name.setText(filteredArray.getJSONObject(j).getString("Event_Name"));
                                            name.setTextColor(Color.parseColor("#FFFFFF"));
                                            TextView desc = new TextView(MyEvent.this);
                                            desc.setText(filteredArray.getJSONObject(j).getString("Event_Venue"));
                                            desc.setTextColor(Color.parseColor("#FFFFFF"));

                                            innerLayout.addView(name);
                                            innerLayout.addView(desc);
                                            layout.addView(innerLayout);

                                            layout.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    String eventName = ((TextView)((ViewGroup)((ViewGroup)((ViewGroup) view).getChildAt(1))).getChildAt(0)).getText().toString();
                                                    Intent i = new Intent(MyEvent.this, EventAnalytic.class);
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
        pasbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread() {
                    public void run() {
                        try {
                            URL url = new URL("https://lqhrxmdxtxyycnftttks.supabase.co/rest/v1/Event?select=*&Email=eq."+email);
                            HttpURLConnection hc = (HttpURLConnection) url.openConnection();
                            hc.setRequestProperty("apikey", getString(R.string.SUPABASE_KEY));
                            hc.setRequestProperty("Authorization", "Bearer "+getString(R.string.SUPABASE_KEY));

                            InputStream input = new BufferedInputStream((hc.getInputStream()));
                            String result = readStream(input);

                            JSONArray jarray = new JSONArray(result);
                            JSONArray filteredArray = new JSONArray();
                            for(int i = 0; i < jarray.length(); i++){
                                JSONObject jsonObject = jarray.getJSONObject(i);
                                String date = jsonObject.getString("Event_Date");
                                if(!CompareTwoDates(date)){
                                    filteredArray.put(jsonObject);
                                }
                            }
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        ll.removeAllViews();
                                        for(int j = 0; j < filteredArray.length(); j++){
                                            LinearLayout layout = new LinearLayout(MyEvent.this);
                                            layout.setOrientation(LinearLayout.HORIZONTAL);
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
                                            inParams.weight = 2;
                                            LinearLayout innerLayout = new LinearLayout(MyEvent.this);
                                            innerLayout.setOrientation(LinearLayout.VERTICAL);
                                            innerLayout.setLayoutParams(inParams);

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
                                                                ImageView img = new ImageView(MyEvent.this);
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


                                            TextView name = new TextView(MyEvent.this);
                                            name.setText(filteredArray.getJSONObject(j).getString("Event_Name"));
                                            name.setTextColor(Color.parseColor("#FFFFFF"));
                                            TextView desc = new TextView(MyEvent.this);
                                            desc.setText(filteredArray.getJSONObject(j).getString("Event_Venue"));
                                            desc.setTextColor(Color.parseColor("#FFFFFF"));

                                            innerLayout.addView(name);
                                            innerLayout.addView(desc);
                                            layout.addView(innerLayout);

                                            layout.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    String eventName = ((TextView)((ViewGroup)((ViewGroup)((ViewGroup) view).getChildAt(1))).getChildAt(0)).getText().toString();
                                                    Intent i = new Intent(MyEvent.this, EventAnalytic.class);
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

        upbtn.performClick();
    }

    // pass in event date, return true if the event is upcoming
    private boolean CompareTwoDates(String date){
        SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date eventDate = sdformat.parse(date);
            Date todayDate = new Date();
            if(eventDate.compareTo(todayDate) > 0){
                return true;
            }
            else{
                return false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
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