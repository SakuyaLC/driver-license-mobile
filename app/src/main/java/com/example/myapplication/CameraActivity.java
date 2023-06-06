package com.example.myapplication;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class CameraActivity extends AppCompatActivity {

    private IntentIntegrator qrScan;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        qrScan = new com.google.zxing.integration.android.IntentIntegrator(this);
        qrScan.setOrientationLocked(true);
        qrScan.setPrompt("Наведите камеру на QR-код");
        qrScan.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        qrScan.setBeepEnabled(false);
        qrScan.setBarcodeImageEnabled(false);
        qrScan.setCameraId(0);

        qrScan.initiateScan();
    }

    // Обработка результатов сканирования QR-кода
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                // QR-код не найден
                Toast.makeText(this, "Сканирование отменено", Toast.LENGTH_LONG).show();
            } else {
                // QR-код найден
                String qrCode = result.getContents();
                Toast.makeText(this, "QR-код: " + qrCode, Toast.LENGTH_LONG).show();

                Intent loginIntentAccountPage = getIntent();

                Intent qrScanIntent = new Intent(CameraActivity.this, CheckActivity.class);
                qrScanIntent.putExtra("Result", qrCode);
                qrScanIntent.putExtra("Id", loginIntentAccountPage.getStringExtra("Id"));


                CameraActivity.this.startActivity(qrScanIntent);
                CameraActivity.this.finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
            Intent loginIntentAccountPage = getIntent();
            Intent loginIntentCamera = new Intent(CameraActivity.this, AccountPageEmployee.class);
            loginIntentCamera.putExtra("Id", loginIntentAccountPage.getStringExtra("Id"));

            CameraActivity.this.startActivity(loginIntentCamera);
            CameraActivity.this.finish();
        }
    }
}