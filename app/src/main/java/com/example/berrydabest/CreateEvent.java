package com.example.berrydabest;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Random;


public class CreateEvent extends AppCompatActivity {

    private Button buttonDatePicker;
    private TextView selectedDateTextView;
    private static String URILink;
    private static String eventDate;
    private static final int IMAGE_SELECTION_REQUEST = 1;
    private static String EventCode;
    private static String EventName;
    private static String ImagePath;
    private static final int QR_CODE_SIZE = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        // Change status bar colour
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.darker_grey));
        }

        buttonDatePicker = findViewById(R.id.datePickerButton);
        selectedDateTextView = findViewById(R.id.editTextEventDate);
        BottomNavigationView navigationView = findViewById(R.id.navigation);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 123);
        }

        ImageView backBtn = findViewById(R.id.img_back);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // Bottom navigation bar
        navigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    startActivity(new Intent(CreateEvent.this, Activity_Profile.class));
                    Toast.makeText(CreateEvent.this, "Home", Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.navigation_calendar:
                    // Handle dashboard navigation
                    startActivity(new Intent(CreateEvent.this, CalendarActivity.class));
                    Toast.makeText(CreateEvent.this, "Calendar", Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.navigation_qrScanner:
                    // Handle notifications navigation
                    Intent intent = new Intent(this, QR_Scan.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.right, R.anim.left);
                    Toast.makeText(CreateEvent.this, "QR Scanner", Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.navigation_myEvent:
                    // Handle notifications navigation
                    startActivity(new Intent(CreateEvent.this, MyEvent.class));
                    Toast.makeText(CreateEvent.this, "My Event", Toast.LENGTH_SHORT).show();
                    return true;
            }
            return false;
        });

    }

    public void showDatePickerDialog(View view) {

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Create a DatePicker dialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int selectedYear, int selectedMonth, int selectedDay) {
                        // This method is called when the user selects a date.
                        // You can update your UI with the selected date here.
                        String selectedDate = selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay;
                        selectedDateTextView.setText(selectedDate);
                        eventDate = selectedDate;
                    }
                },
                year, // Default year
                month, // Default month
                day // Default day
        );

        // Show the DatePicker dialog
        datePickerDialog.show();
    }

    public void onSelectImagesButtonClick(View view) {
        // Create an intent to select images from the device's gallery
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_SELECTION_REQUEST);
    }

    // Selecting images
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_SELECTION_REQUEST && resultCode == RESULT_OK) {
            // Handle the selected image(s) here
            // You can retrieve the selected image(s) using data.getData()
            Uri selectedImageUri = data.getData();

            String imagePath = getFilenameFromURI(selectedImageUri);
            URILink = imagePath;
            // You can then display the selected image(s) or upload them to a server
            // Example: display the selected image in an ImageView
            ImageView imageView = findViewById(R.id.ImageView);
            imageView.setImageURI(selectedImageUri);

            imageView.setVisibility(View.VISIBLE);
        }
    }

    Handler mHandler = new Handler();

    // Submit Button
    public void onCreateEventButtonClick(View view) {

        EditText eventNameText = findViewById(R.id.editTextEventName);
        String eventName = eventNameText.getText().toString();

        EditText eventVenueText = findViewById(R.id.editTextEventVenue);
        String eventVenue = eventVenueText.getText().toString();

        EditText eventDescriptionText = findViewById(R.id.editTextEventDescription);
        String eventDescription = eventDescriptionText.getText().toString();

        EditText eventFeeText = findViewById(R.id.editTextEventFees);
        String eventFee = eventFeeText.getText().toString();

        MyThread connectingThread = new MyThread(eventName, eventDate, eventVenue, eventDescription, eventFee, mHandler);
        MyThread1 connectingThread1 = new MyThread1();

        connectingThread.start();
        connectingThread1.start();

        Random random = new Random();
        int randomNumber = random.nextInt(99) + 1;

        String eventCode = eventName + randomNumber;
        EventCode = eventCode;
        EventName = eventName;

        String imagePath = getQRCodeImagePath(eventName +".jpeg");

        MyThread2 connectingThread2 = new MyThread2(imagePath,eventName +".jpeg");
        connectingThread2.start();

        Toast.makeText(CreateEvent.this, "Event Has been created!", Toast.LENGTH_SHORT).show();


//        Intent intent = new Intent(this, QR_Generator.class);
//        intent.putExtra("eventName", eventName);
//        intent.putExtra("eventCode", EventCode); // Assuming you have eventCode defined somewhere
//        startActivity(intent);
    }

    // Numeric checking function
    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private class MyThread extends Thread{
        private String mName;
        private String mDate;
        private String mVenue;
        private String mDesc;
        private String mFee;
        private String email;
        private Handler mHandler;

        public MyThread(String name, String date, String venue, String desc, String fee, Handler handler){
            mName = name;
            mDate = date;
            mVenue = venue;
            mDesc = desc;
            mFee = fee;
            mHandler = handler;
        }
        public void run(){
            //thread to run to make connection
            try {
                URL url = new URL("https://lqhrxmdxtxyycnftttks.supabase.co/rest/v1/Event?");
                HttpURLConnection hc = (HttpURLConnection) url.openConnection();
                //set the api key, can retrieve the info (JSON array returned) and print out ; if dun have the api key / supabase key then cannot retrieve the data from supabase
                hc.setRequestMethod("POST");
                hc.setRequestProperty("apikey",getString(R.string.SUPABASE_KEY));
                hc.setRequestProperty("Authorization" ,"Bearer " + getString(R.string.SUPABASE_KEY));
                //data in json form
                hc.setRequestProperty("Content-Type","application/json");
                hc.setRequestProperty("Prefer","Prefer");


                Random random = new Random();
                int randomNumber = random.nextInt(99) + 1;

                String eventCode = mName + randomNumber;

                EventName = mName;
//                // put passing variable to the functions
//                SendingClass receivingObj = new SendingClass();
//                PassVarInterface passVarMethod = receivingObj;

//                passVarMethod.PassVar(mName, eventCode);

                email = "yikhengl@gmail.com";
                //create Json object to put data (insert based on the column name) :be careful
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("Event_Name" , mName);
                jsonObject.put("Event_Date" , mDate);
                jsonObject.put("Event_Venue" , mVenue);
                jsonObject.put("Event_Description" , mDesc);
                jsonObject.put("Event_Fee" , mFee);
                jsonObject.put("Email", email);
                jsonObject.put("Event_Code", eventCode);
                hc.setDoOutput(true);
                OutputStream output = hc.getOutputStream();
                output.write(jsonObject.toString().getBytes());
                output.flush();

                //read data coming from connection
                InputStream input = hc.getInputStream();
                //call readStream() function to read
                String result = readStream(input);

                //check the data from web server to see whether request get success or not
                if(hc.getResponseCode() == 200){ // check if success HTTP request, successfully accessed a web API, successfully read from the webpage
                    //OK response code
                    //result:response come from web server
                    // Intent intent = new Intent(CreateEvent.this, LinkDBActivity.class);
                    // intent.putExtra("response",result);
                    // startActivity(intent);

                }else if (hc.getResponseCode()==201){
                    Log.i("MainActivity","You have successfully inserted an entry to Supabase.");
                }
                else{
                    Log.i("MainActivity","Response Code:" + hc.getResponseCode());
                }
                input.close();

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }
    }

    // Store event images
    private class MyThread1 extends Thread {

        public void run() {
            try {

                EditText eventNameText = findViewById(R.id.editTextEventName);
                String eventName = eventNameText.getText().toString();

                // URL to the Supabase Storage endpoint for uploading objects
                URL url = new URL("https://lqhrxmdxtxyycnftttks.supabase.co/storage/v1/object/image/" + eventName + ".jpg");

                // Open a connection to the URL
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");

                // Set the required authorization header
                connection.setRequestProperty("Authorization", "Bearer " + getString(R.string.SUPABASE_KEY));
                connection.setRequestProperty("Content-Type", "image/jpeg");

                // Create the request body with the image data
                OutputStream outputStream = connection.getOutputStream();
                byte[] imageData = readImageFile(URILink);

                // Write the image data to the request's output stream
                OutputStream os = connection.getOutputStream();
                os.write(imageData);
                os.close();
                Log.i("ASD", String.valueOf(connection.getResponseCode()));
                // Handle the response code to check if the upload was successful
                if (connection.getResponseCode() == 200) {
                    Log.i("MainActivity","You have successfully images an entry to Supabase.");
                } else {
                    // Handle the error
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


    }

    // Post Qr in storage
    public class MyThread2 extends Thread {
        private String imageFilePath;
        private String imageName;

        public MyThread2(String imageFilePath,String imageName) {
            this.imageFilePath = imageFilePath;
            this.imageName=imageName;
        }

        public void run() {
            try {
                generateQRCode(EventCode);

                // URL to the Supabase Storage endpoint for uploading objects
                URL url = new URL("https://lqhrxmdxtxyycnftttks.supabase.co/storage/v1/object/QR/"+imageName);

                // Open a connection to the URL
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");

                // Set the required authorization header
                connection.setRequestProperty("Authorization", "Bearer " + getString(R.string.SUPABASE_KEY));
                connection.setRequestProperty("Content-Type", "image/jpeg");

                // Create the request body with the image data
                OutputStream outputStream = connection.getOutputStream();
                byte[] imageData = readImageFile(imageFilePath);

                // Write the image data to the request's output stream
                OutputStream os = connection.getOutputStream();
                os.write(imageData);
                os.close();

                // Handle the response code to check if the upload was successful
                if (connection.getResponseCode() == 200) {
                    Log.i("MainActivity","You have successfully images an QR to Supabase.");

                } else {
                    Log.i("MainActivity","NO QR!" + connection.getResponseCode());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    private String readStream(InputStream is) {
        try {
            ByteArrayOutputStream bo = new
                    ByteArrayOutputStream();
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

    // Encode a Bitmap image to base64 string
    private String encodeToBase64(Bitmap imageBitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] byteArray = baos.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private static byte[] readImageFile(String imagePath) throws IOException {
        FileInputStream fileInputStream = null;
        byte[] imageData = null;

        try {
            File file = new File(imagePath);
            fileInputStream = new FileInputStream(file);
            imageData = new byte[(int) file.length()];
            fileInputStream.read(imageData);
            Log.i("MainActivity","Yes!");
        } finally {
            if (fileInputStream != null) {
                fileInputStream.close();
                Log.i("MainActivity","No!");
            }
        }

        return imageData;
    }


    private String getFilenameFromURI(Uri contentUri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, projection, null, null, null);

        if (cursor == null) {
            return null;
        }

        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String filePath = cursor.getString(column_index);
        cursor.close();

        return filePath;
    }

    // QR

    public void generateQRCode(String data) {
        try {
            // Encode the data into a QR Code
            BitMatrix bitMatrix = new MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            // Fill the QR code bitmap with black and white pixels
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            saveBitmapToStorage(bmp);
            Log.i("MainActivity", "QR code generated successfully");

        } catch (WriterException e) {
            e.printStackTrace();
            Log.e("MainActivity", "Error generating QR code: " + e.getMessage());
        }
    }

    public void saveBitmapToStorage(Bitmap bitmap) {
        String filename = EventName +".jpeg"; // Choose a filename and extension
        try {
            // Open a FileOutputStream to save the bitmap
            FileOutputStream stream = this.openFileOutput(filename, Context.MODE_PRIVATE);

            // Compress the bitmap to PNG format
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);

            // Close the stream to complete the save operation
            stream.close();
            Log.i("MainActivity", "QR code image saved");
            // Inform the user that the image has been saved
            // Toast.makeText(this, "QR code image saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("MainActivity", "Error saving QR code image: " + e.getMessage());
        }
    }

    public String getQRCodeImagePath(String filename) {
        File file = new File(getFilesDir(), filename);
        return file.getAbsolutePath();
    }





    public interface PassVarInterface {
        void PassVar(String mName, String eventCode);
    }

//    public class SendingClass implements PassVarInterface {
//
//            public void PassVar (String mName, String eventCode){
//                QR_Generator.ReceivingClass receivingObj = new QR_Generator.ReceivingClass();
//                receivingObj.Name(mName);
//                receivingObj.Code(eventCode);
//
//        }
//    }

}

