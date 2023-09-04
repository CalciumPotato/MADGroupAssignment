package com.example.berrydabest;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
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

        ImageView ExportCSV = findViewById(R.id.ExportCSV);
        ExportCSV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Code to execute when the ImageView is clicked
                // For example, you can start a new activity or show a dialog
                // Example: Starting a new activity
                if (ContextCompat.checkSelfPermission(EventAnalytic.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    // Permission is granted, proceed with other initialization or your thread
                    MyThread2 connectThread = new MyThread2("yikhengl@gmail.com", handler);
                    connectThread.start();

                } else {
                    // Permission is not granted, request it
                    ActivityCompat.requestPermissions(EventAnalytic.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_WRITE_STORAGE);
                }



            }
        });

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
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog alertDialog = builder.create();
                                alertDialog.show();
                                alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialogInterface) {
                                        Log.d("Alert", "AlertDialog dismissed");
                                    }
                                });
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


