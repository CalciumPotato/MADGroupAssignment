package com.example.berrydabest;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;

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

    static HttpURLConnection updateSupabaseUser(String old_email, String apiKey, String Authorization) throws IOException {
        HttpURLConnection urlConnection4;

        // 1. Access to the Supabase URL
        // C1.1 Table: Event
        String tableUrl4 = "https://lqhrxmdxtxyycnftttks.supabase.co/rest/v1/";
        String tableName4 = "User";
        String tableFilter4 = "Email=eq." + old_email;
        String urlString4 = tableUrl4 + tableName4 + "?" + tableFilter4;

        URL url4 = new URL(urlString4);

        Log.i("##### DEBUG #####", "url 4: " + url4.toString());

        urlConnection4 = (HttpURLConnection) url4.openConnection();

        // 2. API: Follow the format in Supabase (API Docs, Project Settings)
        urlConnection4.setRequestProperty("apiKey", apiKey);
        urlConnection4.setRequestProperty("Authorization", "Bearer " + Authorization);
        urlConnection4.setRequestProperty("Content-Type", "application/json");
        urlConnection4.setRequestProperty("Prefer", "return=minimal");
        urlConnection4.setRequestMethod("PATCH");
        urlConnection4.setDoOutput(true);

        return urlConnection4;
    }

    static HttpURLConnection connectSupabaseImage(String eventName, String Authorization) throws IOException {
        HttpURLConnection urlConnection5;

        // 1. Access to the Supabase URL
        // C1.1 Table: Event
        String tableUrl5 = "https://lqhrxmdxtxyycnftttks.supabase.co/storage/v1/object/image/";
        String tableFilter5 = eventName + ".jpg";
        String urlString5 = tableUrl5 + tableFilter5;

        URL url5 = new URL(urlString5);

        Log.i("##### DEBUG #####", "url 5: " + url5.toString());

        urlConnection5 = (HttpURLConnection) url5.openConnection();

        // 2. API: Follow the format in Supabase (API Docs, Project Settings)
        urlConnection5.setRequestProperty("Content-Type", "image/jpeg");
        urlConnection5.setRequestProperty("Authorization", "Bearer " + Authorization);

        return urlConnection5;
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

    // GUI
    static LinearLayout createEventCard(Context context) {
        LinearLayout eventCard = new LinearLayout(context);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        eventCard.setOrientation(LinearLayout.VERTICAL);
        eventCard.setBackgroundColor(Color.parseColor("#3f4248"));
        eventCard.setPadding(8, 8, 8, 8);
        layoutParams.setMargins(16, 16, 16, 16);
        eventCard.setLayoutParams(layoutParams);

        return eventCard;
    }

    static LinearLayout createEventDetails(Context context) {
        LinearLayout eventDetails = new LinearLayout(context);
        eventDetails.setOrientation(LinearLayout.VERTICAL);
        eventDetails.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, // width
                LinearLayout.LayoutParams.MATCH_PARENT,
                1  // weight
        ));
        eventDetails.setPadding(8, 8, 8, 8);

        return eventDetails;
    }

    static ImageView createImg(Context context, Bitmap bm) {
        ImageView img = new ImageView(context);
        img.setPadding(8, 8, 8, 8);
        img.setAdjustViewBounds(true);
        img.setScaleType(ImageView.ScaleType.CENTER_CROP);
        img.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, // width
                400
        ));
        img.setImageBitmap(bm);

        return img;
    }

    static TextView createEventName(Context context, JSONArray filteredArray, int k) throws JSONException {
        TextView event_name = new TextView(context);
        event_name.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        event_name.setText(filteredArray.getJSONObject(k).getString("Event_Name"));
        event_name.setTextColor(Color.parseColor("#FFFFFF"));
        event_name.setTextSize(28);
        event_name.setTypeface(null, Typeface.BOLD);
        event_name.setPadding(8, 0, 8, 0);
        event_name.setMaxLines(1);
        event_name.setEllipsize(TextUtils.TruncateAt.END);

        return event_name;
    }

    static TextView createEventDate(Context context, JSONArray filteredArray, int k) throws JSONException {
        TextView event_date = new TextView(context);
        event_date.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        event_date.setText(filteredArray.getJSONObject(k).getString("Event_Date"));
        event_date.setTextColor(Color.parseColor("#FFFFFF"));
        event_date.setPadding(8, 0, 8, 0);

        return event_date;
    }

    static TextView createEventDesc(Context context, JSONArray filteredArray, int k) throws JSONException {
        TextView event_desc = new TextView(context);
        event_desc.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        event_desc.setText(filteredArray.getJSONObject(k).getString("Event_Description"));
        event_desc.setTextColor(Color.parseColor("#FFFFFF"));
        event_desc.setTextSize(18);
        event_desc.setPadding(8, 0, 8, 0);
        event_desc.setMaxLines(4);
        event_desc.setEllipsize(TextUtils.TruncateAt.END);

        return  event_desc;
    }

    // function to clearPreference
    static void clearPreference(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("Secret", Context.MODE_PRIVATE);
        sharedPreferences.edit().clear().commit();
        return;
    }
}
