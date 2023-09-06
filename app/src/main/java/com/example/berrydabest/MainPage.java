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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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

public class MainPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        Handler handler = new Handler();

        String email = readPreference(this, "Email", "notFound");
        BottomNavigationView navigationView = findViewById(R.id.navigation);

        navigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    startActivity(new Intent(MainPage.this, Activity_Profile.class));
                    return true;
                case R.id.navigation_calendar:
                    // Handle dashboard navigation
                    startActivity(new Intent(MainPage.this, CalendarActivity.class));
                    return true;
                case R.id.navigation_qrScanner:
                    // Handle notifications navigation
                    Intent intent = new Intent(this, QR_Scan.class);
                    startActivity(intent);
                    return true;
                case R.id.navigation_myEvent:
                    // Handle notifications navigation
                    startActivity(new Intent(MainPage.this, MyEvent.class));
                    return true;
            }
            return false;
        });

       new Thread(){
            public void run(){
                try {
                    URL url = new URL("https://lqhrxmdxtxyycnftttks.supabase.co/rest/v1/Participation?select=Event_Id,Event(Event_Name,Event_Description,Event_Date)&User_Email=eq."+email);
                    HttpURLConnection hc = null;
                    hc = (HttpURLConnection) url.openConnection();
                    hc.setRequestProperty("apikey", getString(R.string.SUPABASE_KEY));
                    hc.setRequestProperty("Authorization", "Bearer "+getString(R.string.SUPABASE_KEY));
                    InputStream input = new BufferedInputStream((hc.getInputStream()));

                    String result = readStream(input);

                    JSONArray jarray = new JSONArray(result);
                    JSONArray filteredArray = new JSONArray();
                    for(int i = 0 ; i < jarray.length(); i++){
                        JSONObject json = jarray.getJSONObject(i).getJSONObject("Event");
                        if(CompareTwoDates(json.getString("Event_Date"))){
                            filteredArray.put(jarray.getJSONObject(i));
                        }
                    }
                    int maxIdx = 0;
                    Date today = new Date();
                    SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd");
                    long diff = sdformat.parse(jarray.getJSONObject(0).getJSONObject("Event").getString("Event_Date")).getTime() - today.getTime();
                    for(int i = 1; i < filteredArray.length(); i++){
                        JSONObject json = jarray.getJSONObject(i).getJSONObject("Event");
                        Date d1 = sdformat.parse(json.getString("Event_Date"));
                        if((d1.getTime() - today.getTime()) < diff){
                            diff = d1.getTime() - today.getTime();
                            maxIdx = i;
                        }
                    }
                    JSONObject closest = filteredArray.getJSONObject(maxIdx).getJSONObject("Event");
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            LinearLayout ll = findViewById(R.id.closest);
                            LinearLayout layout = new LinearLayout(MainPage.this);
                            layout.setOrientation(LinearLayout.HORIZONTAL);
                            LinearLayout.LayoutParams linearParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.MATCH_PARENT
                            );
                            linearParams.setMargins(10, 10,10,10);
                            layout.setPadding(20, 20, 20, 20);
                            layout.setLayoutParams(linearParams);
                            layout.setBackgroundResource(R.drawable.border);
                            new Thread(){
                                public void run(){
                                    try{
                                        String eventName = closest.getString("Event_Name");
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
                                                ImageView img = new ImageView(MainPage.this);
                                                img.setImageBitmap(bm);
                                                img.setLayoutParams(imgParams);
                                                layout.addView(img, 0);
                                            }
                                        });
                                    }
                                    catch(IOException | JSONException e){
                                        e.printStackTrace();
                                    }
                                }
                            }.start();


                            LinearLayout.LayoutParams inParams = new LinearLayout.LayoutParams(
                                    0,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );
                            inParams.weight = 2;
                            LinearLayout innerLayout = new LinearLayout(MainPage.this);
                            innerLayout.setOrientation(LinearLayout.VERTICAL);
                            innerLayout.setLayoutParams(inParams);
                            try {
                                TextView name = new TextView(MainPage.this);
                                name.setText(closest.getString("Event_Name"));
                                name.setTextColor(Color.parseColor("#FFFFFF"));
                                name.setTextSize(20);
                                TextView desc = new TextView(MainPage.this);
                                desc.setText(closest.getString("Event_Description"));
                                desc.setTextColor(Color.parseColor("#FFFFFF"));
                                desc.setTextSize(20);
                                innerLayout.addView(name);
                                innerLayout.addView(desc);

                                layout.addView(innerLayout);
                                layout.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        String eventName = ((TextView)((ViewGroup)((ViewGroup)((ViewGroup) view).getChildAt(1))).getChildAt(0)).getText().toString();
                                        Intent i = new Intent(MainPage.this, EventDetail.class);
                                        i.putExtra("EventName", eventName);
                                        startActivity(i);
                                    }
                                });
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            ll.addView(layout, 1);
                        }
                    });
                } catch (IOException | JSONException | ParseException e) {
                    e.printStackTrace();
                }
            }
        }.start();

        new Thread() {
            public void run() {
                try {
                    URL url = new URL("https://lqhrxmdxtxyycnftttks.supabase.co/rest/v1/Event?select=*");
                    HttpURLConnection hc = (HttpURLConnection) url.openConnection();
                    hc.setRequestProperty("apikey", getString(R.string.SUPABASE_KEY));
                    hc.setRequestProperty("Authorization", "Bearer "+getString(R.string.SUPABASE_KEY));

                    InputStream input = new BufferedInputStream((hc.getInputStream()));
                    String result = readStream(input);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                LinearLayout ll = findViewById(R.id.eventList);
                                JSONArray jarray = new JSONArray(result);
                                JSONArray filteredArray = new JSONArray();
                                for(int i = 0 ; i < jarray.length(); i++){
                                    JSONObject json = jarray.getJSONObject(i);
                                    if(CompareTwoDates(json.getString("Event_Date"))){
                                        filteredArray.put(jarray.getJSONObject(i));
                                    }
                                }
                                for(int i = 0; i < filteredArray.length(); i++){
                                    LinearLayout layout = new LinearLayout(MainPage.this);
                                    layout.setOrientation(LinearLayout.HORIZONTAL);
                                    LinearLayout.LayoutParams linearParams = new LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.MATCH_PARENT,
                                            LinearLayout.LayoutParams.MATCH_PARENT
                                    );
                                    linearParams.setMargins(0, 10,0,10);
                                    layout.setPadding(20, 20, 20, 20);
                                    layout.setLayoutParams(linearParams);
                                    layout.setBackgroundResource(R.drawable.border);
                                    String eventName = filteredArray.getJSONObject(i).getString("Event_Name");
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
                                                        ImageView img = new ImageView(MainPage.this);
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


                                    LinearLayout.LayoutParams inParams = new LinearLayout.LayoutParams(
                                            0,
                                            LinearLayout.LayoutParams.WRAP_CONTENT
                                    );
                                    inParams.weight = 2;
                                    LinearLayout innerLayout = new LinearLayout(MainPage.this);
                                    innerLayout.setOrientation(LinearLayout.VERTICAL);
                                    innerLayout.setLayoutParams(inParams);


                                    TextView name = new TextView(MainPage.this);
                                    name.setText(filteredArray.getJSONObject(i).getString("Event_Name"));
                                    name.setTextColor(Color.parseColor("#FFFFFF"));
                                    TextView desc = new TextView(MainPage.this);
                                    desc.setText(filteredArray.getJSONObject(i).getString("Event_Description"));
                                    desc.setTextColor(Color.parseColor("#FFFFFF"));

                                    innerLayout.addView(name);
                                    innerLayout.addView(desc);

                                    layout.addView(innerLayout);
                                    layout.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            String eventName = ((TextView)((ViewGroup)((ViewGroup)((ViewGroup) view).getChildAt(1))).getChildAt(0)).getText().toString();
                                            Intent i = new Intent(MainPage.this, EventDetail.class);
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
                } catch (IOException e) {
                    e.printStackTrace();
                }
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

    // read email from preference file
    public static String readPreference(Context context, String key, String defaultValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("Secret", Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, defaultValue);
    }
}