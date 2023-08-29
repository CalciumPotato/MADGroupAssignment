package com.example.berrydabest;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private GoogleSignInClient mGoogleSignInClient;
    Handler eHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        TextView signup = findViewById(R.id.sign);
        EditText email = findViewById(R.id.email);
        EditText password = findViewById(R.id.password);
        signup.setPaintFlags(signup.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        Animation floating = AnimationUtils.loadAnimation(this, R.anim.floating);
        ImageView google_btn = findViewById(R.id.google);
        google_btn.startAnimation(floating);

        Button sign_bt = findViewById(R.id.signup);
        sign_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyThread connectingThread = new MyThread(email.getText().toString(),password.getText().toString(),eHandler);
                connectingThread.start();
            }
        });

        google_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        Intent intent = new Intent(this, SignUp.class);
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(intent);
                overridePendingTransition(R.anim.right, R.anim.left);
            }
        });
    }

    private void signIn() {
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if(acct != null){
            signOut();
        }
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 1000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1000) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                task.getResult(ApiException.class);
                GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
                if(acct != null){
                    Google_thread connectingThread = new Google_thread(acct.getEmail(), acct.getDisplayName(),eHandler);
                    connectingThread.start();
                }


            } catch (ApiException e) {
                // Handle Google Sign-In failure.
                showMessage("Please check your Google Play Services!");
            }
        }
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

    private class Google_thread extends Thread{
        private String email;
        private String username;
        private Handler mHandler;

        public Google_thread(String email,String username, Handler handler){
            this.email=email;
            this.username=username;
            this.mHandler = handler;

        }

        public void run(){
            try {
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
                jsonObject.put("Password" , null);
                jsonObject.put("Phone" , null);
                jsonObject.put("Picture" , null);

                hc.setDoOutput(true);
                OutputStream output = hc.getOutputStream();
                output.write(jsonObject.toString().getBytes());
                output.flush();

                if(hc.getResponseCode() == 201){
                    mHandler.post(new Runnable() {
                        public void run() {
                            //Intent to next page

                            showMessage("                  Congratulation !"+"\nAccount has been created successfully !");
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

    private void signOut() {
        mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

            }
        });

    }
                    // You can perform additional actions here, such as updating UI or navigating to a different screen



}