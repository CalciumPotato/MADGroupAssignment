package com.example.berrydabest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
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

    private TextView nextEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        Handler handler = new Handler();

        String email = readPreference(this, "Email", "notFound");
        nextEvent = findViewById(R.id.textView3);
        BottomNavigationView navigationView = findViewById(R.id.navigation);

        ImageView setting = findViewById(R.id.img_setting);
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainPage.this, Activity_Profile.class));
            }
        });

        ImageView create = findViewById(R.id.img_create);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainPage.this, CreateEvent.class));
            }
        });

        ImageView add = findViewById(R.id.img_create);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainPage.this, CreateEvent.class));
            }
        });


        navigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_home:
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

        // thread for next event
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
                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            layout.setOrientation(LinearLayout.VERTICAL);
                            layout.setBackgroundColor(Color.parseColor("#3f4248"));
                            layout.setPadding(8, 8, 8, 8);
                            layoutParams.setMargins(16, 16, 16, 16);
                            layout.setLayoutParams(layoutParams);

                            new Thread(){
                                public void run(){
                                    try{
                                        String eventName = closest.getString("Event_Name");

                                        // Get event image
                                        HttpURLConnection hc = Activity_Profile_Tools.connectSupabaseImage(eventName, getString(R.string.SUPABASE_KEY));
                                        InputStream input = new BufferedInputStream((hc.getInputStream()));
                                        Bitmap bm = BitmapFactory.decodeStream(input);

                                        ImageView img = Activity_Profile_Tools.createImg(MainPage.this, bm);
                                        // Add event image
                                        handler.post(() -> layout.addView(img, 0));
                                    }
                                    catch(IOException | JSONException e){
                                        e.printStackTrace();
                                    }
                                }
                            }.start();

                            LinearLayout innerLayout = new LinearLayout(MainPage.this);
                            innerLayout.setOrientation(LinearLayout.VERTICAL);
                            innerLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT, // width
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    1  // weight
                            ));
                            innerLayout.setPadding(8, 8, 8, 8);

                            try {
                                TextView name = new TextView(MainPage.this);
                                name.setText(closest.getString("Event_Name"));
                                name.setTextColor(Color.parseColor("#FFFFFF"));
                                name.setTextSize(28);
                                name.setMaxLines(1);
                                name.setEllipsize(TextUtils.TruncateAt.END);
                                name.setLayoutParams(new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                ));
                                name.setTypeface(null, Typeface.BOLD);
                                name.setPadding(8, 0, 8, 0);

                                nextEvent.setText("Your Next Event: " + closest.getString("Event_Date"));

                                innerLayout.addView(name);

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
                            ll.addView(layout);
                        }
                    });
                } catch (IOException | JSONException | ParseException e) {
                    e.printStackTrace();
                }
            }
        }.start();

       // thread for other events
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
                                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                    layout.setOrientation(LinearLayout.VERTICAL);
                                    layout.setBackgroundColor(Color.parseColor("#3f4248"));
                                    layout.setPadding(8, 8, 8, 8);
                                    layoutParams.setMargins(16, 16, 16, 16);
                                    layout.setLayoutParams(layoutParams);

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
                                                        ImageView img = new ImageView(MainPage.this);
                                                        img.setPadding(8, 8, 8, 8);
                                                        img.setAdjustViewBounds(true);
                                                        img.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                                        img.setLayoutParams(new LinearLayout.LayoutParams(
                                                                LinearLayout.LayoutParams.MATCH_PARENT, // width
                                                                350
                                                        ));
                                                        img.setImageBitmap(bm);
                                                        // img.setLayoutParams(imgParams);
                                                        layout.addView(img, 0);
                                                    }
                                                });
                                            }
                                            catch(IOException e){
                                                e.printStackTrace();
                                            }
                                        }
                                    }.start();


                                    LinearLayout innerLayout = new LinearLayout(MainPage.this);
                                    innerLayout.setOrientation(LinearLayout.VERTICAL);
                                    innerLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.MATCH_PARENT, // width
                                            LinearLayout.LayoutParams.MATCH_PARENT,
                                            1  // weight
                                    ));
                                    innerLayout.setPadding(8, 8, 8, 8);

                                    TextView name = Activity_Profile_Tools.createEventName(MainPage.this, filteredArray, i);

                                    TextView desc = new TextView(MainPage.this);
                                    desc.setText(filteredArray.getJSONObject(i).getString("Event_Description"));
                                    desc.setTextColor(Color.parseColor("#FFFFFF"));
                                    desc.setTextSize(18);
                                    desc.setPadding(8, 0, 8, 0);
                                    desc.setMaxLines(4);
                                    desc.setEllipsize(TextUtils.TruncateAt.END);
                                    desc.setLayoutParams(new LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.MATCH_PARENT,
                                            LinearLayout.LayoutParams.WRAP_CONTENT
                                    ));

                                    TextView date = new TextView(MainPage.this);
                                    date.setText(filteredArray.getJSONObject(i).getString("Event_Date"));
                                    date.setTextColor(Color.parseColor("#FFFFFF"));
                                    date.setTextSize(20);
                                    date.setLayoutParams(new LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.MATCH_PARENT,
                                            LinearLayout.LayoutParams.WRAP_CONTENT
                                    ));
                                    date.setPadding(8, 0, 8, 0);

                                    innerLayout.addView(name);
                                    innerLayout.addView(date);
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

    // function to clearPreference
    public static void clearPreference(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("Secret", Context.MODE_PRIVATE);
        sharedPreferences.edit().clear().commit();
        return;
    }
}