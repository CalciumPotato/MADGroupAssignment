package com.example.berrydabest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_success);

        Intent intent_receive = getIntent();
        TextView tv_display = new TextView(this);

        try {
            // get StringExtra "result" from WebserviceActivity.java
            // Supabase returns in JSONArray format
            JSONArray jsonArray = new JSONArray(intent_receive.getStringExtra("result"));

            StringBuilder resultBuilder = new StringBuilder("USERNAME EMAIL PHONE\n");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                resultBuilder.append(jsonObject.optString("Username")).append("\n")
                        .append(jsonObject.optString("Email")).append("\n")
                        .append(jsonObject.optString("Phone")).append("\n\n");
            }

            String result = resultBuilder.toString();

            tv_display.setTextSize(20.0f);
            tv_display.setText(result);

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        setContentView(tv_display);
    }
}
