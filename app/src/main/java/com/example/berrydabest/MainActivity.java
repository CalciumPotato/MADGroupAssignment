package com.example.berrydabest;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        Handler eHandler = new Handler();

        TextView signup = findViewById(R.id.sign);
        EditText email = findViewById(R.id.email);
        EditText password = findViewById(R.id.password);
        signup.setPaintFlags(signup.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        Button sign_bt = findViewById(R.id.signup);
        sign_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyThread connectingThread = new MyThread(email.getText().toString(),password.getText().toString(),eHandler);
                connectingThread.start();
            }
        });


        Intent intent = new Intent(this, SignUp.class);
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(intent);
                overridePendingTransition(R.anim.right, R.anim.left);
            }
        });
    }

    private class MyThread extends Thread{
        private String email;
        private String password;
        private Handler mHandler;

        public MyThread(String email, String password,Handler handler){
            this.email = email;
            this.password = password;
            this.mHandler = handler;

        }

        public void run(){
            try {

                URL url = new URL("https://lqhrxmdxtxyycnftttks.supabase.co/rest/v1/User?" + "Email=eq." + email);


                HttpURLConnection hc = (HttpURLConnection) url.openConnection();
                hc.setRequestProperty("apikey" , getString(R.string.SUPABASE_KEY));
                hc.setRequestProperty("Authorization" ,"Bearer " + getString(R.string.SUPABASE_KEY));



                InputStream input = hc.getInputStream();
                String result = readStream(input);
                JSONArray jsonArray = new JSONArray(result);
                if(jsonArray.length() == 0){
                    mHandler.post(new Runnable() {
                        public void run() {
                            showMessage("Email or Password Invalid!");
                        }
                    });
                    return;
                }

                if(hc.getResponseCode() == 200) {
                    Log.i("MainActivity", "Response: " + result);

                    if(jsonArray.getJSONObject(0).getString("Password").equalsIgnoreCase(password)) {
                        mHandler.post(new Runnable() {
                            public void run() {
                                showMessage("Success!");
                            }
                        });
                    }
                    else{
                        mHandler.post(new Runnable() {
                            public void run() {
                                showMessage("Email or Password Invalid!");
                            }
                        });
                    }

                }
                else{
                    Log.i("MainActivity" , "Response code: " + hc.getResponseCode());

                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }

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

    private void showMessage(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }

}