package com.example.berrydabest;

import android.content.Intent;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//display the result in form of JSON from MainActivity2.java
public class LinkDBActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.linik_db);

        Intent intent = getIntent();
        TextView tv = new TextView(this);

        try {
            //Q2
           /* JSONObject jsonObject = new JSONObject(intent.getStringExtra("response"));
            String name = jsonObject.getString("name"); //get value from json form with the key of "name"
            int age = jsonObject.getInt("age");*/

            //Q3
            //JSON array has many object
            JSONArray jsonArray = new JSONArray(intent.getStringExtra("response"));
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            String result = "Name Age Programme\n";
            result = result + jsonObject.getString("Name") + " "
                    +jsonObject.getString("Age") + " "
                    +jsonObject.getString("Programme") + "\n";
           /* tv.setText("You have successfully accessed a web API!\n"
                    + name + "'s age is" + age);*/
            tv.setText(result);
            tv.setTextSize(30);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        setContentView(tv);

    }
}