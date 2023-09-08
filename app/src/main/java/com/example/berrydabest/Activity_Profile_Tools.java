package com.example.berrydabest;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Activity_Profile_Tools extends AppCompatActivity {

    static boolean CompareTwoDates(String date) {
        SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date eventDate = sdformat.parse(date);
            Date todayDate = new Date();
            if (eventDate.compareTo(todayDate) > 0) {
                return true;
            } else {
                return false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    static String readStream(InputStream is) {
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

    public static String readPreference(Context context, String key, String defaultValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("Secret", Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, defaultValue);
    }

    static HttpURLConnection connectSupabaseUser(String email, String apiKey, String Authorization) throws IOException {
        HttpURLConnection urlConnection = null;

        // 1. Access to the Supabase URL
        // A1.1 Table: User
        String tableUrl = "https://lqhrxmdxtxyycnftttks.supabase.co/rest/v1/";
        String tableName = "User";
        String tableFilter = "Email=eq." + email;
        String urlString = tableUrl + tableName + "?" + tableFilter;

        URL url = new URL(urlString);

        Log.i("##### DEBUG #####", "url 1: " + url.toString());

        urlConnection = (HttpURLConnection) url.openConnection();

        // 2. API: Follow the format in Supabase (API Docs, Project Settings)
        urlConnection.setRequestProperty("apiKey", apiKey);
        urlConnection.setRequestProperty("Authorization", "Bearer " + Authorization);

        return urlConnection;
    }

    static HttpURLConnection connectSupabaseParticipation(String email, String apiKey, String Authorization) throws IOException {
        HttpURLConnection urlConnection2;

        // 1. Access to the Supabase URL
        // B1.1 Table: Participation
        String tableUrl2 = "https://lqhrxmdxtxyycnftttks.supabase.co/rest/v1/";
        String tableName2 = "Participation";
        String tableFilter2 = "User_Email=eq." + email;
        String urlString2 = tableUrl2 + tableName2 + "?" + tableFilter2;

        URL url2 = new URL(urlString2);

        Log.i("##### DEBUG #####", "url 2: " + url2.toString());

        urlConnection2 = (HttpURLConnection) url2.openConnection();

        // 2. API: Follow the format in Supabase (API Docs, Project Settings)
        urlConnection2.setRequestProperty("apiKey", apiKey);
        urlConnection2.setRequestProperty("Authorization", "Bearer " + Authorization);

        return urlConnection2;
    }

    static HttpURLConnection connectSupabaseEvent(String email, String eventID, String apiKey, String Authorization) throws IOException {
        HttpURLConnection urlConnection3;

        // 1. Access to the Supabase URL
        // C1.1 Table: Event
        String tableUrl3 = "https://lqhrxmdxtxyycnftttks.supabase.co/rest/v1/";
        String tableName3 = "Event";
        String tableFilter3 = "Event_Name=eq." + eventID;
        String urlString3 = tableUrl3 + tableName3 + "?" + tableFilter3;

        URL url3 = new URL(urlString3);

        Log.i("##### DEBUG #####", "url 3: " + url3.toString());

        urlConnection3 = (HttpURLConnection) url3.openConnection();

        // 2. API: Follow the format in Supabase (API Docs, Project Settings)
        urlConnection3.setRequestProperty("apiKey", apiKey);
        urlConnection3.setRequestProperty("Authorization", "Bearer " + Authorization);

        return urlConnection3;
    }

    static StringBuilder getResponse(HttpURLConnection urlConnection) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        StringBuilder response = new StringBuilder();

        String inputLine;

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }

        in.close();

        return response;
    }

}
