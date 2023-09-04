package com.example.berrydabest;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class QR_Scan extends AppCompatActivity {

    private Button selectFromGalleryButton;
    Handler eHandler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scan);

        showAlertDialog(R.layout.activity_qr_scan);

    }

    public void showAlertDialog(int view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose the way to scan QR")
                .setMessage("Select one of the following options:")
                .setPositiveButton("QR Scanner", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Handle Option 1 click
                        startQRCodeScanner();
                    }
                })
                .setNegativeButton("QR from Gallery", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Intent.ACTION_PICK);
                        intent.setType("image/*");
                        startActivityForResult(intent, 123); // Use a unique request code
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert) // Optional: Set an icon
                .show();
    }

    private void startQRCodeScanner() {
        // Code for starting the QR code scanner
        // This is the same code as in the previous example
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Scan a QR Code");

        integrator.setCameraId(0); // Use the rear camera
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 123) {
            if (resultCode == RESULT_OK) {
                // Handle the selected image from the gallery
                Uri selectedImageUri = data.getData();

                // Now you can use selectedImageUri to read the image and perform QR code scanning
                // Add your QR code scanning logic here
                try {
                    InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                    // Convert the Bitmap to a binary bitmap for QR code scanning
                    int[] intArray = new int[bitmap.getWidth() * bitmap.getHeight()];
                    bitmap.getPixels(intArray, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
                    LuminanceSource source = new RGBLuminanceSource(bitmap.getWidth(), bitmap.getHeight(), intArray);
                    BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));

                    // Initialize the QR code reader
                    Reader reader = new MultiFormatReader();
                    Result result = reader.decode(binaryBitmap);

                    // Handle the QR code scan result
                    String scannedData = result.getText();
                    QR_thread connectingThread = new QR_thread(scannedData,eHandler);
                    connectingThread.start();
                    finish();

                } catch (Exception e) {
                    e.printStackTrace();
                    showToast("Error decoding QR code");
                }
            } else {
                // The user canceled the gallery picker
                showToast("Gallery selection canceled");
            }
        }
        else if (requestCode == IntentIntegrator.REQUEST_CODE) {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result != null) {
                if (result.getContents() == null) {
                    // Handle a canceled scan
                    Toast.makeText(this, "Scan canceled", Toast.LENGTH_SHORT).show();
                } else {
                    // Handle the scanned data
                    String scannedData = result.getContents();


                    // Now you can use selectedImageUri to read the image and perform QR code scanning
                    try {


                        QR_thread connectingThread = new QR_thread(scannedData,eHandler);
                        connectingThread.start();
                        finish();


                    } catch (Exception e) {
                        e.printStackTrace();
                        showToast("Error decoding QR code");
                    }

                }
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }



    }


    private class QR_thread extends Thread{
        private String code;
        private String email ="yikhengl@gmail.com";//Set to get preference from file
        private String eventName;
        private Handler mHandler;
        private boolean checkEvent = false;

        public QR_thread(String code,Handler handler){
            this.code = code;
            this.mHandler = handler;

        }

        public void run(){
            try {

                URL url = new URL("https://lqhrxmdxtxyycnftttks.supabase.co/rest/v1/Event?" + "Event_Code=eq." + code);


                HttpURLConnection hc = (HttpURLConnection) url.openConnection();
                hc.setRequestProperty("apikey" , getString(R.string.SUPABASE_KEY));
                hc.setRequestProperty("Authorization" ,"Bearer " + getString(R.string.SUPABASE_KEY));



                InputStream input = hc.getInputStream();
                String result = readStream(input);
                JSONArray jsonArray = new JSONArray(result);
                if(jsonArray.length() == 0){
                    mHandler.post(new Runnable() {
                        public void run() {
                            showToast("No event!");
                        }
                    });
                    return;
                }
                else{
                    eventName = jsonArray.getJSONObject(0).getString("Event_Name");
                    Log.i("Event Name",eventName);
                }
                hc.disconnect();

                URL url2 = new URL("https://lqhrxmdxtxyycnftttks.supabase.co/rest/v1/Participation?" + "User_Email=eq." + email);


                HttpURLConnection hc2 = (HttpURLConnection) url2.openConnection();
                hc2.setRequestProperty("apikey" , getString(R.string.SUPABASE_KEY));
                hc2.setRequestProperty("Authorization" ,"Bearer " + getString(R.string.SUPABASE_KEY));



                InputStream input2 = hc2.getInputStream();
                String result2 = readStream(input2);
                JSONArray jsonArray2 = new JSONArray(result2);
                if(jsonArray2.length() == 0){
                    mHandler.post(new Runnable() {
                        public void run() {
                            showToast(" You didn't join any event! ");
                        }
                    });
                    return;
                }
                else{
                    for(int i = 0; i< jsonArray2.length();i++) {

                        if(jsonArray2.getJSONObject(i).getString("Event_Id").equals(eventName)){
                            checkEvent = true;
                            break;
                        }

                    }
                }
                hc2.disconnect();


                URL url1 = new URL("https://lqhrxmdxtxyycnftttks.supabase.co/rest/v1/Participation?User_Email=eq."+email+"&"+"Event_Id=eq." + eventName);
                HttpURLConnection hc1 = (HttpURLConnection) url1.openConnection();
                hc1.setRequestMethod("PATCH");
                hc1.setRequestProperty("apikey" , getString(R.string.SUPABASE_KEY));
                hc1.setRequestProperty("Authorization" ,"Bearer " + getString(R.string.SUPABASE_KEY));
                hc1.setRequestProperty("Content-Type" , "application/json");
                hc1.setRequestProperty("Prefer" , "Prefer");

                //For HTTP POST
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("Attendance" , checkEvent);
                hc1.setDoOutput(true);
                OutputStream output = hc1.getOutputStream();
                output.write(jsonObject.toString().getBytes());
                output.flush();
                if(hc1.getResponseCode() == 204){
                    mHandler.post(new Runnable() {
                        public void run() {
                            //Intent to next page

                            showToast("Scan Successfully");
                        }
                    });
                }
                else{
                    mHandler.post(new Runnable() {
                        public void run() {
                            //Intent to next page

                            showToast("I dunno why :)");
                        }
                    });
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
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public static String readPreference(Context context, String key, String defaultValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("Secret", Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, defaultValue);
    }
}
