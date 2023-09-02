package com.example.berrydabest;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Image extends AppCompatActivity {

    public static final int IMAGE_PICKER_REQUEST = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
    }

    // Handle the button click event
    public void onUploadButtonClick(View view) {
        // Start an intent to pick an image from the gallery
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICKER_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_PICKER_REQUEST && resultCode == RESULT_OK) {
            // Get the selected image URI
            Uri selectedImageUri = data.getData();

            // Get the actual file path from the URI (works for Android API level 19 and below)
            String imagePath = getFilenameFromURI(selectedImageUri);
            MyThread connectingThread = new MyThread(imagePath);
            connectingThread.start();
            // Use the imagePath as needed
            Log.d("Image Path", imagePath);
        }
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

    private class MyThread extends Thread {
        private String imageFilePath;

        public MyThread(String imageFilePath) {
            this.imageFilePath = imageFilePath;
        }

        public void run() {
            try {
                // URL to the Supabase Storage endpoint for uploading objects
                URL url = new URL("https://lqhrxmdxtxyycnftttks.supabase.co/storage/v1/object/image/1.jpg");

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
                    // Image uploaded successfully
                } else {
                    // Handle the error
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }
    private static byte[] readImageFile(String imagePath) throws IOException {
        FileInputStream fileInputStream = null;
        byte[] imageData = null;

        try {
            File file = new File(imagePath);
            fileInputStream = new FileInputStream(file);
            imageData = new byte[(int) file.length()];
            fileInputStream.read(imageData);
        } finally {
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        }

        return imageData;
    }
}
