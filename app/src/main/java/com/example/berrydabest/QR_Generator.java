package com.example.berrydabest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class QR_Generator extends AppCompatActivity {

    private ImageView qrCodeImageView;
    private static final int QR_CODE_SIZE = 500;
    private String qr = "ong12";//change to event name

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_generator);

        qrCodeImageView = findViewById(R.id.qrCodeImageView);

        // Generate and display a QR code
        generateQRCode("Marathon_202384");//get code from supabase or other way
    }

    private void generateQRCode(String data) {
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
            String imagePath = getQRCodeImagePath(qr+".jpeg");
            MyThread connectingThread = new MyThread(imagePath,qr+".jpeg");
            connectingThread.start();
            // Display the QR code in the ImageView
            qrCodeImageView.setImageBitmap(bmp);

        } catch (WriterException e) {
            e.printStackTrace();
        }
    }
    private void saveBitmapToStorage(Bitmap bitmap) {
        String filename = qr+".jpeg"; // Choose a filename and extension
        try {
            // Open a FileOutputStream to save the bitmap
            FileOutputStream stream = this.openFileOutput(filename, Context.MODE_PRIVATE);

            // Compress the bitmap to PNG format
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);

            // Close the stream to complete the save operation
            stream.close();

            // Inform the user that the image has been saved
            Toast.makeText(this, "QR code image saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getQRCodeImagePath(String filename) {
        File file = new File(getFilesDir(), filename);
        return file.getAbsolutePath();
    }

    private class MyThread extends Thread {
        private String imageFilePath;
        private String imageName;

        public MyThread(String imageFilePath,String imageName) {
            this.imageFilePath = imageFilePath;
            this.imageName=imageName;
        }

        public void run() {
            try {
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
