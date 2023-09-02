package com.example.berrydabest;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.google.zxing.Result;
import com.google.zxing.integration.android.IntentIntegrator;
import com.journeyapps.barcodescanner.CaptureActivity;

public class Qr_Scan extends CaptureActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Start the QR code scanner
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(Qr_Scan.class);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Scan a QR Code");
        integrator.setCameraId(0);  // Use the rear camera (0) by default
        integrator.setBeepEnabled(true);
        integrator.initiateScan();
    }


    public void handleDecode(Result rawResult, Bundle bundle) {
        if (rawResult != null) {
            String scannedResult = rawResult.getText();

            // You can do something with the scanned result, e.g., display it or process it
            // For this example, we'll just display it in a toast
            Toast.makeText(this, "Scanned: " + scannedResult, Toast.LENGTH_SHORT).show();

            // To continue scanning, you can initiate a new scan after a short delay
            new Handler().postDelayed(() -> {
                // Restart the scanner activity
                IntentIntegrator integrator = new IntentIntegrator(this);
                integrator.setCaptureActivity(Qr_Scan.class);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
                integrator.setPrompt("Scan a QR Code");
                integrator.setCameraId(0);  // Use the rear camera (0) by default
                integrator.setBeepEnabled(true);
                integrator.initiateScan();
            }, 2000); // Delay for 2 seconds before initiating a new scan
        } else {
            // Handle when no QR code is detected
            Toast.makeText(this, "No QR code found", Toast.LENGTH_SHORT).show();
            // You can decide how to handle this situation, e.g., prompt the user to try again
        }
    }
}