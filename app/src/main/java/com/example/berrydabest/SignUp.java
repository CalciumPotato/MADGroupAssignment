package com.example.berrydabest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class SignUp extends AppCompatActivity {




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_sign_up);
        Handler eHandler = new Handler();



        EditText email = findViewById(R.id.email);
        EditText password = findViewById(R.id.password);
        EditText password2 = findViewById(R.id.password2);
        EditText username = findViewById(R.id.username);
        EditText phone = findViewById(R.id.phone);

        Button signup_bt = findViewById(R.id.signup);
        signup_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(password.getText().toString().equalsIgnoreCase(password2.getText().toString()) == false){
                    showMessage("Password is not the same!");
                    return;
                }

                if(isNumeric(phone.getText().toString())==false){
                    showMessage("Invalid Phone Number!");
                    return;
                }

                if(isValidEmail(email.getText().toString())) {


                    MyThread connectingThread = new MyThread(email.getText().toString(),
                            username.getText().toString(), password.getText().toString(),
                            phone.getText().toString(), eHandler);


                    connectingThread.start();
                }

                else{
                    showMessage("Email format is invalid!");
                }
            }
        });


        Intent back_intent = new Intent(this, MainActivity.class);
        ImageView back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(back_intent);
                overridePendingTransition(R.anim.left1, R.anim.right1);
            }
        });






    }


    private class MyThread extends Thread{
        private String email;
        private String username;
        private String password;
        private String phone;

        private Handler mHandler;

        public MyThread(String email,String username,String password,String phone, Handler handler){
            this.email=email;
            this.username=username;
            this.password = password;
            this.phone = phone;
            this.mHandler = handler;

        }

        public void run(){
            try {
                URL url1 = new URL("https://lqhrxmdxtxyycnftttks.supabase.co/rest/v1/User?" + "Email=eq." + email);


                HttpURLConnection hc1 = (HttpURLConnection) url1.openConnection();
                hc1.setRequestProperty("apikey" , getString(R.string.SUPABASE_KEY));
                hc1.setRequestProperty("Authorization" ,"Bearer " + getString(R.string.SUPABASE_KEY));



                InputStream input1 = hc1.getInputStream();
                String result1 = readStream(input1);
                JSONArray jsonArray = new JSONArray(result1);
                if(jsonArray.length() != 0){
                    mHandler.post(new Runnable() {
                        public void run() {
                            showMessage("The email is existed!");
                        }
                    });
                    return;
                }

                URL url = new URL("https://lqhrxmdxtxyycnftttks.supabase.co/rest/v1/User?");
                HttpURLConnection hc = (HttpURLConnection) url.openConnection();
                hc.setRequestMethod("POST");
                hc.setRequestProperty("apikey" , getString(R.string.SUPABASE_KEY));
                hc.setRequestProperty("Authorization" ,"Bearer " + getString(R.string.SUPABASE_KEY));
                hc.setRequestProperty("Content-Type" , "application/json");
                hc.setRequestProperty("Prefer" , "Prefer");

                //For HTTP POST
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("Email" , email);
                jsonObject.put("Username" , username);
                jsonObject.put("Password" , password);
                jsonObject.put("Phone" , phone);
                jsonObject.put("Picture" , null);
                hc.setDoOutput(true);
                OutputStream output = hc.getOutputStream();
                output.write(jsonObject.toString().getBytes());
                output.flush();


                if(hc.getResponseCode() == 201){
                    mHandler.post(new Runnable() {
                        public void run() {
                            showMessage("                  Congratulation !"+"\nAccount has been created successfully !");
                            Intent back_intent = new Intent(SignUp.this, MainActivity.class);
                            startActivity(back_intent);
                            overridePendingTransition(R.anim.left1, R.anim.right1);
                        }
                    });
                }
                else{
                    Log.i("SignUp" , "Response code: " + hc.getResponseCode());
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
    private boolean isValidEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        return email.matches(emailPattern);
    }
    private boolean isNumeric(String str) {
        return str.matches("\\d+");
    }
}