package com.example.berrydabest;

import static com.example.berrydabest.Activity_Profile_Tools.readPreference;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

public class EventAnalytic extends AppCompatActivity {

    private String Nameevent;
    private String email;
    private static final int REQUEST_CODE_WRITE_STORAGE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_analytic);

        String eventName = this.getIntent().getStringExtra("EventName");
        Nameevent = eventName;
        final Handler handler = new Handler();

        String Email = readPreference(this, "Email", "");
        email = Email;
        TextView name = findViewById(R.id.textView2);
        name.setText(eventName);

        //Back Button
        ImageView backBtn = findViewById(R.id.img_back);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //Navigation Bar
        BottomNavigationView navigationView = findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    startActivity(new Intent(EventAnalytic.this, MainPage.class));
                    Toast.makeText(EventAnalytic.this, "Home", Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.navigation_calendar:
                    // Handle dashboard navigation
                    startActivity(new Intent(EventAnalytic.this, CalendarActivity.class));
                    Toast.makeText(EventAnalytic.this, "Calendar", Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.navigation_qrScanner:
                    // Handle notifications navigation
                    Intent intent = new Intent(this, QR_Scan.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.right, R.anim.left);
                    Toast.makeText(EventAnalytic.this, "QR Scanner", Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.navigation_myEvent:
                    // Handle notifications navigation
                    startActivity(new Intent(EventAnalytic.this, MyEvent.class));
                    Toast.makeText(EventAnalytic.this, "My Event", Toast.LENGTH_SHORT).show();
                    return true;
            }
            return false;
        });

        ShowImage imageThread = new ShowImage("yikhengl@gmail.com", handler);
        imageThread.start();

        //Press on List Image to show Participant List
        ImageView participantListImageView = findViewById(R.id.ParticipantList);
        participantListImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyThread connectThread = new MyThread(email, handler);
                connectThread.start();
            }
        });


        //Press on QR Image to show QR
        ImageView AttendanceQR = findViewById(R.id.AttendanceQR);
        AttendanceQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DisplayQR connectThread = new DisplayQR(email, handler);
                connectThread.start();
            }

        });
    }


    // Display the QR code that is stored in supabase
    private class DisplayQR extends Thread {

        private String email;
        private String username, phone;
        private Handler handler;

        HttpURLConnection urlConnection = null;

        // 2.3 Constructor
        public DisplayQR(String email, Handler handler) {
            this.email = email;
            this.handler = handler;
        }

        @Override
        public void run() {
            try {

                String encodedEventString = Nameevent.replace(" ", "%20");
                // 1. Access to the Supabase URL
                email = "yikhengl@gmail.com";
                String urlString = "https://lqhrxmdxtxyycnftttks.supabase.co/storage/v1/object/QR/"+ encodedEventString +".jpeg";

                URL url = new URL(urlString);

                Log.i("##### DEBUG #####", "url: " + url.toString());

                urlConnection = (HttpURLConnection) url.openConnection();

                // 2. API: Follow the format in Supabase (API Docs, Project Settings)
                urlConnection.setRequestProperty("apiKey", getString(R.string.SUPABASE_KEY));
                urlConnection.setRequestProperty("Authorization", "Bearer " + getString(R.string.SUPABASE_KEY));


                // 3. Obtain the status of connection
                int responseCode = urlConnection.getResponseCode();
                Log.i("##### DEBUG #####", "code: " + responseCode);

                if (responseCode == 200) {
                    InputStream inputStream = urlConnection.getInputStream();

                    // Create a file to save the image locally
                    String fileName = Nameevent + ".jpg";
                    File imageFile = new File(getFilesDir(), fileName);

                    FileOutputStream outputStream = new FileOutputStream(imageFile);
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    outputStream.close();
                    inputStream.close();

                    // Pass the image file path to the next activity using an intent
                    Intent intent = new Intent(EventAnalytic.this, DisplayQRCode.class);
                    intent.putExtra("imagePath", imageFile.getAbsolutePath());
                    startActivity(intent);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }
    }

    // Participant List, Retreive data from supabase
    private class MyThread extends Thread {

        private String email;
        private String username, phone;
        private Handler handler;
        private int total = 0;
        private int attend = 0;

        HttpURLConnection urlConnection = null;



        public MyThread(String email, Handler handler) {
            this.email = email;
            this.handler = handler;
        }

        @Override
        public void run() {
            try {

                String encodedEventString = Nameevent.replace(" ", "%20");
                // 1. Access to the Supabase URL
                email = "yikhengl@gmail.com";
                String tableUrl = "https://lqhrxmdxtxyycnftttks.supabase.co/rest/v1/";
                String tableName = "Participation";
                String tableFilter = "select=Attendance,User_Email,User(Username)&Event_Id=eq." + encodedEventString;
                String urlString = tableUrl + tableName + "?" + tableFilter;

                URL url = new URL(urlString);

                Log.i("##### DEBUG #####", "url: " + url.toString());

                urlConnection = (HttpURLConnection) url.openConnection();

                // 2. API: Follow the format in Supabase (API Docs, Project Settings)
                urlConnection.setRequestProperty("apiKey", getString(R.string.SUPABASE_KEY));
                urlConnection.setRequestProperty("Authorization", "Bearer " + getString(R.string.SUPABASE_KEY));


                // 3. Obtain the status of connection
                int responseCode = urlConnection.getResponseCode();
                Log.i("##### DEBUG #####", "code: " + responseCode);



                if (responseCode == 200) {

                    BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    String jsonResponse = response.toString();
                    Log.i("##### DEBUG #####", "API Response: " + jsonResponse);


                    try {
                        // Create an AlertDialog.Builder
                        AlertDialog.Builder builder = new AlertDialog.Builder(EventAnalytic.this);
                        JSONArray jsonArray = new JSONArray(jsonResponse);


                        LinearLayout containerLayout = new LinearLayout(EventAnalytic.this);
                        containerLayout.setOrientation(LinearLayout.VERTICAL);
                        containerLayout.setGravity(Gravity.CENTER);

                        LayoutInflater inflater = LayoutInflater.from(EventAnalytic.this);
                        View headerLayout = inflater.inflate(R.layout.header_layout, null);
                        containerLayout.addView(headerLayout);

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            JSONObject userObject = jsonObject.optJSONObject("User");


                            if (jsonArray.length() > 0) {
                                total++;
                                String Name = userObject.optString("Username");
                                String Attendance = jsonObject.optString("Attendance");
                                if (Attendance == "true"){
                                    attend++;
                                }

                                View customLayout = inflater.inflate(R.layout.participant_list, null);
                                // Find TextViews in the custom layout
                                TextView nameTextView = customLayout.findViewById(R.id.participant_name);
                                TextView attendanceTextView = customLayout.findViewById(R.id.participant_attendance);

                                // Set the participant information
                                nameTextView.setText(Name);
                                attendanceTextView.setText(Attendance);

                                // Add the custom layout to the AlertDialog
                                containerLayout.addView(customLayout);
                            }
                        }

                        TextView attended = headerLayout.findViewById(R.id.AttendaceNo);
                        attended.setText("Attendance : " + attend + " / " + total);

                        ScrollView scrollView = new ScrollView(EventAnalytic.this);
                        scrollView.addView(containerLayout);

                        builder.setView(scrollView);
                        builder.setPositiveButton("OK", null);

                        Log.d("Alert", "Before AlertDialog creation");
                        // Show the AlertDialog on the main UI thread
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog alertDialog = builder.create();
                                alertDialog.show();
                            }
                        });
                        Log.d("Alert", "After AlertDialog creation");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // Disconnect
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }
    }

    // Show the relative event image for the interface
    private class ShowImage extends Thread {

        private String email;
        private String username, phone;
        private Handler handler;

        HttpURLConnection urlConnection = null;

        // 2.3 Constructor
        public ShowImage(String email, Handler handler) {
            this.email = email;
            this.handler = handler;
        }

        @Override
        public void run() {
            try {

                String encodedEventString = Nameevent.replace(" ", "%20");
                // 1. Access to the Supabase URL
                email = "yikhengl@gmail.com";
                String urlString = "https://lqhrxmdxtxyycnftttks.supabase.co/storage/v1/object/image/"+Nameevent+".jpg";
                URL url = new URL(urlString);

                Log.i("##### DEBUG #####", "url: " + url.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty("apiKey", getString(R.string.SUPABASE_KEY));
                urlConnection.setRequestProperty("Authorization", "Bearer " + getString(R.string.SUPABASE_KEY));


                // 3. Obtain the status of connection
                int responseCode = urlConnection.getResponseCode();
                Log.i("##### DEBUG #####", "code: " + responseCode);

                if (responseCode == 200) {
                    InputStream inputStream = urlConnection.getInputStream();

                    // Create a file to save the image locally
                    String fileName = Nameevent + ".jpg";
                    File imageFile = new File(getFilesDir(), fileName);

                    FileOutputStream outputStream = new FileOutputStream(imageFile);
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    outputStream.close();
                    inputStream.close();

                    ImageView EventPic = findViewById(R.id.Image1);

                    if (imageFile.exists()) {
                        Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                EventPic.setImageBitmap(bitmap);
                            }
                        });
                    }



                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
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


