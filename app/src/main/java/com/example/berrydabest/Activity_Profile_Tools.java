package com.example.berrydabest;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.LinearLayout;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Activity_Profile_Tools {

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

    static LinearLayout.LayoutParams formatLayout(LinearLayout layout, LinearLayout innerLayout)
    {
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
        innerLayout.setOrientation(LinearLayout.VERTICAL);

        return inParams;
    }
}
