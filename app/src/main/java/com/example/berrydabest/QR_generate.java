package com.example.berrydabest;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

public class QR_generate extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_generate);

        // Data you want to encode in the QR code
        String dataToEncode = "Diu";

        ImageView imageView = findViewById(R.id.code);
        Bitmap qrBitmap = generateQRCodeBitmap(dataToEncode, 300, 300); // Adjust width and height as needed
        imageView.setImageBitmap(qrBitmap);
    }

    private Bitmap generateQRCodeBitmap(String data, int width, int height) {
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(data, BarcodeFormat.QR_CODE, width, height);
            int bmWidth = bitMatrix.getWidth();
            int bmHeight = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(bmWidth, bmHeight, Bitmap.Config.ARGB_8888);

            for (int x = 0; x < bmWidth; x++) {
                for (int y = 0; y < bmHeight; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF); // Black or White
                }
            }
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }
}