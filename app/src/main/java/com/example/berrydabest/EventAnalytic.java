package com.example.berrydabest;

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

    private static final int REQUEST_CODE_WRITE_STORAGE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_analytic);
        final Handler handler = new Handler();
        String email = "yikhengl@gmail.com";

        BottomNavigationView navigationView = findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Toast.makeText(EventAnalytic.this, "Home", Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.navigation_calendar:
                    // Handle dashboard navigation
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
                    Toast.makeText(EventAnalytic.this, "My Event", Toast.LENGTH_SHORT).show();
                    return true;
            }
            return false;
        });

        ShowImage imageThread = new ShowImage("yikhengl@gmail.com", handler);
        imageThread.start();

        ImageView participantListImageView = findViewById(R.id.ParticipantList);
        participantListImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Code to execute when the ImageView is clicked
                // For example, you can start a new activity or show a dialog
                // Example: Starting a new activity
                MyThread connectThread = new MyThread("yikhengl@gmail.com", handler);
                connectThread.start();
            }
        });

        ImageView AttendanceQR = findViewById(R.id.AttendanceQR);
        AttendanceQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Code to execute when the ImageView is clicked
                // For example, you can start a new activity or show a dialog
                // Example: Starting a new activity
                DisplayQR connectThread = new DisplayQR("yikhengl@gmail.com", handler);
                connectThread.start();
            }

        });

        //        ImageView ExportCSV = findViewById(R.id.ExportCSV);
//        ExportCSV.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Code to execute when the ImageView is clicked
//                // For example, you can start a new activity or show a dialog
//                // Example: Starting a new activity
//                if (ContextCompat.checkSelfPermission(EventAnalytic.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//                    // Permission is granted, proceed with other initialization or your thread
//                    MyThread2 connectThread = new MyThread2("yikhengl@gmail.com", handler);
//                    connectThread.start();
//
//                } else {
//                    // Permission is not granted, request it
//                    ActivityCompat.requestPermissions(EventAnalytic.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_WRITE_STORAGE);
//                }
//
//
//
//            }
//        });
    }



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

                String eventString = "2222";
                String encodedEventString = eventString.replace(" ", "%20");
                // 1. Access to the Supabase URL
                email = "yikhengl@gmail.com";
                String urlString = "https://lqhrxmdxtxyycnftttks.supabase.co/storage/v1/object/QR/"+ eventString +".jpeg";

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
                    String fileName = eventString + ".jpg";
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


    private class MyThread extends Thread {

        private String email;
        private String username, phone;
        private Handler handler;

        HttpURLConnection urlConnection = null;

        // 2.3 Constructor
        public MyThread(String email, Handler handler) {
            this.email = email;
            this.handler = handler;
        }

        @Override
        public void run() {
            try {

                String eventString = "Mooncake Festival";
                String encodedEventString = eventString.replace(" ", "%20");
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
                        builder.setTitle("Participants");
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

                                String Name = userObject.optString("Username");
                                String Attendance = jsonObject.optString("Attendance");

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

                        builder.setView(containerLayout);
                        builder.setPositiveButton("OK", null);

                        Log.d("Alert", "Before AlertDialog creation");
                        // Show the AlertDialog on the main UI thread
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

                String eventString = "Mooncake Festival";
                String encodedEventString = eventString.replace(" ", "%20");
                // 1. Access to the Supabase URL
                email = "yikhengl@gmail.com";
                String urlString = "https://lqhrxmdxtxyycnftttks.supabase.co/storage/v1/object/image/"+eventString+".jpg";
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
                    String fileName = eventString + ".jpg";
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











    // csv file function
    private class MyThread2 extends Thread {

        private String email;
        private String username, phone;
        private Handler handler;

        HttpURLConnection urlConnection = null;

        // 2.3 Constructor
        public MyThread2(String email, Handler handler) {
            this.email = email;
            this.handler = handler;
        }

        @Override
        public void run() {

            try {

                String eventString = "Mooncake Festival";
                String encodedEventString = eventString.replace(" ", "%20");
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
                        JSONArray jsonArray = new JSONArray(jsonResponse);

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            JSONObject userObject = jsonObject.optJSONObject("User");


                            if (jsonArray.length() > 0) {

                                String Name = userObject.optString("Username");
                                String Attendance = jsonObject.optString("Attendance");
                                FileWriter fileWriter = null;

                                File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                                if (!downloadDir.exists()) {
                                    if (!downloadDir.mkdirs()) {
                                        Log.e("CSV", "Failed to create download directory");
                                        return;
                                    }
                                }

                                File csvFile = new File(downloadDir, "Attendance.csv");

//                                File folder = new File(Environment.getExternalStorageDirectory() + "/Folder");
//
//                                boolean var = false;
//                                if (!folder.exists())
//                                    var = folder.mkdir();
//
//                                final String csvFile = folder.toString() + "/" + "Attendance.csv";
//
                                try {

                                    fileWriter = new FileWriter(csvFile);

                                    // Write the header row
                                    JSONObject firstObject = jsonArray.getJSONObject(0);
                                    Iterator<String> keys = firstObject.keys();
                                    while (keys.hasNext()) {
                                        String key = keys.next();
                                        fileWriter.append(key);
                                        if (keys.hasNext()) {
                                            fileWriter.append(",");
                                        }
                                    }
                                    fileWriter.append("\n");

                                    // Write the data rows
                                    for (int j = 0; j < jsonArray.length(); j++) {
                                        JSONObject jsonCSV = jsonArray.getJSONObject(j);
                                        Iterator<String> dataKeys = jsonCSV.keys();
                                        while (dataKeys.hasNext()) {
                                            String key = dataKeys.next();
                                            String value = jsonCSV.getString(key);
                                            fileWriter.append(value);
                                            if (dataKeys.hasNext()) {
                                                fileWriter.append(",");
                                            }
                                        }
                                        fileWriter.append("\n");
                                    }

                                    fileWriter.flush();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                } finally {
                                    try {
                                        if (fileWriter != null) {
                                            fileWriter.close();

                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(EventAnalytic.this, "CSV file generated successfully", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }

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


